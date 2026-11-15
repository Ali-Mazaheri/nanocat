using System.Net;
using System.Text;
using System.Text.RegularExpressions;

namespace Nanocat;

public record NanorcRule(bool IsBlock, string Color, Regex Start, Regex? End = null);
public record NanorcSyntax(string Name, List<Regex> Extensions, List<NanorcRule> Rules);

public static class Highlighter
{
    private static readonly Dictionary<string, string> AnsiFg = new(StringComparer.OrdinalIgnoreCase)
    {
        ["black"] = "30", ["red"] = "31", ["green"] = "32", ["yellow"] = "33",
        ["blue"] = "34", ["magenta"] = "35", ["cyan"] = "36", ["white"] = "37",
        ["brightblack"] = "90", ["brightred"] = "91", ["brightgreen"] = "92",
        ["brightyellow"] = "93", ["brightblue"] = "94", ["brightmagenta"] = "95",
        ["brightcyan"] = "96", ["brightwhite"] = "97", ["orange"] = "38;5;208"
    };

    public static string PosixToDotNetRegex(string pattern) =>
        pattern.Replace(@"\<", @"\b").Replace(@"\>", @"\b")
               .Replace("[[:space:]]", @"\s").Replace("[:space:]", @"\s")
               .Replace("[[:digit:]]", "[0-9]").Replace("[:digit:]", "[0-9]");

    private static string ColorToAnsi(string spec)
    {
        var codes = new List<string>();
        foreach (var part in spec.Split(',', StringSplitOptions.TrimEntries))
        {
            if (part.Equals("bold", StringComparison.OrdinalIgnoreCase)) codes.Add("1");
            else if (AnsiFg.TryGetValue(part, out var fg)) codes.Add(fg);
        }
        return codes.Count > 0 ? $"\x1b[{string.Join(';', codes)}m" : "";
    }

    private static List<string> ExtractQuoted(string input)
    {
        var list = new List<string>();
        foreach (Match m in Regex.Matches(input, "\"((?:\\\\\"|[^\"])*)\""))
            list.Add(m.Groups[1].Value.Replace("\\\"", "\""));
        return list;
    }

    public static List<NanorcSyntax> ParseNanorc(string content)
    {
        var syntaxes = new List<NanorcSyntax>();
        NanorcSyntax? current = null;

        foreach (var raw in content.Split('\n'))
        {
            var line = raw.Trim();
            if (string.IsNullOrEmpty(line) || line.StartsWith('#')) continue;

            if (line.StartsWith("syntax "))
            {
                var q = ExtractQuoted(line[7..]);
                current = new NanorcSyntax(
                    q.Count > 0 ? q[0] : "unknown",
                    q.Skip(1).Select(p => new Regex(PosixToDotNetRegex(p))).ToList(),
                    new List<NanorcRule>()
                );
                syntaxes.Add(current);
            }
            else if (current != null && (line.StartsWith("color ") || line.StartsWith("icolor ")))
            {
                bool icase = line.StartsWith("icolor ");
                var rest = line[(icase ? 7 : 6)..].Trim();
                int sp = rest.IndexOfAny([' ', '\t']);
                if (sp < 0) continue;
                var colorSpec = rest[..sp];
                var body = rest[sp..].Trim();
                var opts = RegexOptions.Multiline | (icase ? RegexOptions.IgnoreCase : RegexOptions.None);
                var q = ExtractQuoted(body);

                if (body.StartsWith("start=") && q.Count >= 2)
                {
                    current.Rules.Add(new NanorcRule(
                        true, colorSpec,
                        new Regex(PosixToDotNetRegex(q[0]), opts),
                        new Regex(PosixToDotNetRegex(q[1]), opts)
                    ));
                }
                else
                {
                    foreach (var pat in q)
                        current.Rules.Add(new NanorcRule(false, colorSpec, new Regex(PosixToDotNetRegex(pat), opts)));
                }
            }
        }
        return syntaxes;
    }

    public static string Highlight(string code, NanorcSyntax syntax, bool html = false)
    {
        int n = code.Length;
        var styles = new string[n];

        foreach (var rule in syntax.Rules)
        {
            if (!rule.IsBlock)
            {
                foreach (Match m in rule.Start.Matches(code))
                    for (int i = m.Index; i < m.Index + m.Length; i++) styles[i] = rule.Color;
            }
            else if (rule.End != null)
            {
                int pos = 0;
                while (pos < n)
                {
                    var ms = rule.Start.Match(code, pos);
                    if (!ms.Success) break;
                    var me = rule.End.Match(code, ms.Index + ms.Length);
                    int endPos = me.Success ? me.Index + me.Length : n;
                    for (int i = ms.Index; i < endPos; i++) styles[i] = rule.Color;
                    pos = Math.Max(endPos, ms.Index + 1);
                }
            }
        }

        var sb = new StringBuilder();
        string cur = "";
        for (int i = 0; i < n; i++)
        {
            string st = styles[i] ?? "";
            if (st != cur)
            {
                if (!string.IsNullOrEmpty(cur)) sb.Append(html ? "</span>" : "\x1b[0m");
                if (!string.IsNullOrEmpty(st))
                    sb.Append(html ? $"<span class=\"nc-{st.Replace(',', '-')}\">" : ColorToAnsi(st));
                cur = st;
            }
            sb.Append(html ? WebUtility.HtmlEncode(code[i].ToString()) : code[i]);
        }
        if (!string.IsNullOrEmpty(cur)) sb.Append(html ? "</span>" : "\x1b[0m");
        return sb.ToString();
    }
}
