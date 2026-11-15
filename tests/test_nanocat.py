import importlib.machinery
import importlib.util
import os
import tempfile
import unittest

SCRIPT_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "nanocat"))
loader = importlib.machinery.SourceFileLoader("nanocat_mod", SCRIPT_PATH)
spec = importlib.util.spec_from_loader("nanocat_mod", loader)
nanocat = importlib.util.module_from_spec(spec)
loader.exec_module(nanocat)


class TestNanocat(unittest.TestCase):
    def test_posix_to_python_regex(self):
        converted = nanocat.posix_to_python_regex(r"\<func\>[[:space:]]+[[:digit:]]+")
        self.assertEqual(converted, r"\bfunc\b\s+[0-9]+")

    def test_color_to_ansi(self):
        self.assertEqual(nanocat.color_to_ansi("bold,brightwhite,blue"), "\033[1;97;44m")
        self.assertEqual(nanocat.color_to_ansi("orange"), "\033[38;5;208m")

    def test_parse_and_highlight(self):
        sample_rc = '''
syntax "demo" "\\.demo$"
color green "\\<(func|return)\\>"
color brightblue start="/\\*" end="\\*/"
'''
        with tempfile.NamedTemporaryFile("w", suffix=".nanorc", delete=False) as tf:
            tf.write(sample_rc)
            rc_path = tf.name

        try:
            syntaxes = nanocat.parse_nanorc_file(rc_path)
            self.assertEqual(len(syntaxes), 1)
            code = "func main() { /* func */ return }"
            out = nanocat.highlight_text(code, syntaxes[0], use_color=True)
            self.assertIn("\033[32mfunc\033[0m", out)
            self.assertIn("\033[94m/* func */\033[0m", out)
        finally:
            os.remove(rc_path)

    def test_line_numbers_range_and_git_markers(self):
        text = "line1\nline2\nline3\nline4\n"
        out = nanocat.highlight_text(
            text,
            syntax=None,
            use_color=False,
            show_numbers=True,
            line_range=(2, 3),
            git_markers={2: "+", 3: "~"},
        )
        self.assertEqual(out, "2 + line2\n3 ~ line3\n")

    def test_soft_wrap(self):
        text = "abcdefghijklmno\n"
        out = nanocat.highlight_text(
            text,
            syntax=None,
            use_color=False,
            show_numbers=True,
            wrap_width=8,
        )
        lines = out.splitlines()
        self.assertEqual(len(lines), 2)
        self.assertTrue(lines[0].startswith("1 "))
        self.assertTrue(lines[1].startswith("  "))


if __name__ == "__main__":
    unittest.main()


class TestAliases(unittest.TestCase):
    def test_lang_aliases(self):
        syntaxes = [{"name": "Python", "file": "/tmp/python.nanorc", "ext_patterns": [], "header_patterns": [], "raw_rules": [], "rules": []}]
        matched = nanocat.select_syntax(syntaxes, "py", None, "")
        self.assertIsNotNone(matched)
        self.assertEqual(matched["name"], "Python")
