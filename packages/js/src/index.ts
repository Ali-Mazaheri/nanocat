export interface NanorcRule {
  type: "single" | "block";
  color: string;
  start: RegExp;
  end?: RegExp;
}

export interface NanorcSyntax {
  name: string;
  extensions: RegExp[];
  rules: NanorcRule[];
}

const ANSI_FG: Record<string, string> = {
  black: "30", red: "31", green: "32", yellow: "33",
  blue: "34", magenta: "35", cyan: "36", white: "37",
  brightblack: "90", brightred: "91", brightgreen: "92",
  brightyellow: "93", brightblue: "94", brightmagenta: "95",
  brightcyan: "96", brightwhite: "97", orange: "38;5;208",
};

function posixToJsRegex(pat: string): string {
  return pat
    .replace(/\\</g, "\\b")
    .replace(/\\>/g, "\\b")
    .replace(/\[\[:space:\]\]|\[:space:\]/g, "\\s")
    .replace(/\[\[:digit:\]\]|\[:digit:\]/g, "[0-9]")
    .replace(/\[\[:alpha:\]\]|\[:alpha:\]/g, "[A-Za-z]")
    .replace(/\[\[:alnum:\]\]|\[:alnum:\]/g, "[A-Za-z0-9]");
}

function colorToAnsi(spec: string): string {
  const parts = spec.toLowerCase().split(",").map((s) => s.trim());
  const codes: string[] = [];
  for (const p of parts) {
    if (p === "bold") codes.push("1");
    else if (ANSI_FG[p]) codes.push(ANSI_FG[p]);
  }
  return codes.length ? `\x1b[${codes.join(";")}m` : "";
}

function extractQuoted(line: string): string[] {
  const out: string[] = [];
  const re = /"((?:\\"|[^"])*)"/g;
  let m: RegExpExecArray | null;
  while ((m = re.exec(line)) !== null) {
    out.push(m[1].replace(/\\"/g, '"'));
  }
  return out;
}

export function parseNanorc(content: string): NanorcSyntax[] {
  const syntaxes: NanorcSyntax[] = [];
  let current: NanorcSyntax | null = null;

  for (const raw of content.split(/\r?\n/)) {
    const line = raw.trim();
    if (!line || line.startsWith("#")) continue;

    if (line.startsWith("syntax ")) {
      const quoted = extractQuoted(line.slice(7));
      current = {
        name: quoted[0] ?? "unknown",
        extensions: quoted.slice(1).map((p) => new RegExp(posixToJsRegex(p))),
        rules: [],
      };
      syntaxes.push(current);
    } else if (current && (line.startsWith("color ") || line.startsWith("icolor "))) {
      const icase = line.startsWith("icolor ");
      const rest = line.slice(icase ? 7 : 6).trim();
      const spaceIdx = rest.search(/\s/);
      if (spaceIdx === -1) continue;
      const colorSpec = rest.slice(0, spaceIdx);
      const body = rest.slice(spaceIdx).trim();
      const flags = icase ? "gm" : "gm";
      const quoted = extractQuoted(body);

      if (body.startsWith("start=") && quoted.length >= 2) {
        current.rules.push({
          type: "block",
          color: colorSpec,
          start: new RegExp(posixToJsRegex(quoted[0]), icase ? "gim" : "gm"),
          end: new RegExp(posixToJsRegex(quoted[1]), icase ? "gim" : "gm"),
        });
      } else {
        for (const pat of quoted) {
          current.rules.push({
            type: "single",
            color: colorSpec,
            start: new RegExp(posixToJsRegex(pat), icase ? "gim" : "gm"),
          });
        }
      }
    }
  }
  return syntaxes;
}

export function highlight(
  code: string,
  syntax: NanorcSyntax,
  format: "ansi" | "html" = "ansi"
): string {
  const n = code.length;
  const styles = new Array<string>(n).fill("");

  for (const rule of syntax.rules) {
    if (rule.type === "single") {
      rule.start.lastIndex = 0;
      let m: RegExpExecArray | null;
      while ((m = rule.start.exec(code)) !== null) {
        if (m[0].length === 0) { rule.start.lastIndex++; continue; }
        for (let i = m.index; i < m.index + m[0].length; i++) styles[i] = rule.color;
      }
    } else if (rule.type === "block" && rule.end) {
      let pos = 0;
      while (pos < n) {
        rule.start.lastIndex = pos;
        const ms = rule.start.exec(code);
        if (!ms) break;
        rule.end.lastIndex = ms.index + ms[0].length;
        const me = rule.end.exec(code);
        const endPos = me ? me.index + me[0].length : n;
        for (let i = ms.index; i < endPos; i++) styles[i] = rule.color;
        pos = Math.max(endPos, ms.index + 1);
      }
    }
  }

  let out = "";
  let cur = "";
  for (let i = 0; i < n; i++) {
    const st = styles[i];
    if (st !== cur) {
      if (cur) out += format === "ansi" ? "\x1b[0m" : "</span>";
      if (st) {
        out += format === "ansi"
          ? colorToAnsi(st)
          : `<span class="nc-${st.replace(/[^a-z0-9]/gi, "-")}">`;
      }
      cur = st;
    }
    const ch = code[i];
    out += format === "html"
      ? ch.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
      : ch;
  }
  if (cur) out += format === "ansi" ? "\x1b[0m" : "</span>";
  return out;
}
