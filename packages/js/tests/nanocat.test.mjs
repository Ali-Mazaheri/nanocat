import test from "node:test";
import assert from "node:assert/strict";
import { parseNanorc, highlight } from "../dist/index.js";

test("parseNanorc and highlight ANSI and HTML", () => {
  const rc = `
syntax "demo" "\\.demo$"
color green "\\<(func|return)\\>"
color brightblue start="/\\*" end="\\*/"
`;
  const syntaxes = parseNanorc(rc);
  assert.equal(syntaxes.length, 1);

  const code = "func main() { /* return */ return }";
  const ansi = highlight(code, syntaxes[0], "ansi");
  const html = highlight(code, syntaxes[0], "html");

  assert.match(ansi, /\x1b\[32mfunc\x1b\[0m/);
  assert.match(ansi, /\x1b\[94m\/\* return \*\/\x1b\[0m/);
  assert.match(html, /<span class="nc-green">func<\/span>/);
  assert.match(html, /<span class="nc-brightblue">\/\* return \*\/<\/span>/);
});
