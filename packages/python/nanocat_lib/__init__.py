"""Importable Python library for .nanorc syntax highlighting (ANSI and HTML)."""

import importlib.machinery
import importlib.util
import os
import html as _html

_ROOT_CLI = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "..", "nanocat"))
_loader = importlib.machinery.SourceFileLoader("_nanocat_core", _ROOT_CLI)
_spec = importlib.util.spec_from_loader("_nanocat_core", _loader)
_core = importlib.util.module_from_spec(_spec)
_loader.exec_module(_core)

parse_nanorc_file = _core.parse_nanorc_file
load_all_syntaxes = _core.load_all_syntaxes
select_syntax = _core.select_syntax


def highlight(
    code: str,
    lang: str | None = None,
    filename: str | None = None,
    output_format: str = "ansi",
    nanorc_dirs: list[str] | None = None,
) -> str:
    """Highlight code using .nanorc definitions into 'ansi' or 'html'."""
    dirs = nanorc_dirs or [os.path.expanduser("~/.nano"), "/usr/share/nano"]
    syntaxes = _core.load_all_syntaxes(dirs)
    syntax = _core.select_syntax(syntaxes, lang, filename, code)
    if output_format == "html":
        return _highlight_html(code, syntax)
    return _core.highlight_text(code, syntax, use_color=True)


def _highlight_html(text: str, syntax: dict | None) -> str:
    if not text or not syntax:
        return f'<pre class="nanocat">{_html.escape(text)}</pre>'
    ansi_out = _core.highlight_text(text, syntax, use_color=True)
    # Convert ANSI foreground codes to CSS classes
    import re
    def repl(match: re.Match) -> str:
        codes = match.group(1)
        if codes == "0":
            return "</span>"
        return f'<span class="nc-{codes.replace(";", "-")}">'
    body = re.sub(r"\033\[([0-9;]+)m", repl, _html.escape(ansi_out))
    return f'<pre class="nanocat">{body}</pre>'
