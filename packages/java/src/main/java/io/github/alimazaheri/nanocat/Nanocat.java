package io.github.alimazaheri.nanocat;

import java.util.*;
import java.util.regex.*;

public final class Nanocat {
    public record Rule(boolean isBlock, String color, Pattern start, Pattern end) {}
    public record Syntax(String name, List<Pattern> extensions, List<Rule> rules) {}

    private static final Map<String, String> ANSI_FG = Map.ofEntries(
        Map.entry("black", "30"), Map.entry("red", "31"), Map.entry("green", "32"),
        Map.entry("yellow", "33"), Map.entry("blue", "34"), Map.entry("magenta", "35"),
        Map.entry("cyan", "36"), Map.entry("white", "37"), Map.entry("brightblack", "90"),
        Map.entry("brightred", "91"), Map.entry("brightgreen", "92"),
        Map.entry("brightyellow", "93"), Map.entry("brightblue", "94"),
        Map.entry("brightmagenta", "95"), Map.entry("brightcyan", "96"),
        Map.entry("brightwhite", "97"), Map.entry("orange", "38;5;208")
    );

    public static String posixToJavaRegex(String pattern) {
        return pattern.replace("\\<", "\\b").replace("\\>", "\\b")
            .replace("[[:space:]]", "\\s").replace("[:space:]", "\\s")
            .replace("[[:digit:]]", "[0-9]").replace("[:digit:]", "[0-9]");
    }

    private static String colorToAnsi(String spec) {
        List<String> codes = new ArrayList<>();
        for (String part : spec.toLowerCase(Locale.ROOT).split(",")) {
            String trimmed = part.trim();
            if ("bold".equals(trimmed)) codes.add("1");
            else if (ANSI_FG.containsKey(trimmed)) codes.add(ANSI_FG.get(trimmed));
        }
        return codes.isEmpty() ? "" : "\u001B[" + String.join(";", codes) + "m";
    }

    private static List<String> extractQuoted(String input) {
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("\"((?:\\\\\"|[^\"])*)\"").matcher(input);
        while (m.find()) {
            list.add(m.group(1).replace("\\\"", "\""));
        }
        return list;
    }

    public static List<Syntax> parseNanorc(String content) {
        List<Syntax> syntaxes = new ArrayList<>();
        Syntax current = null;

        for (String raw : content.split("\\R")) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            if (line.startsWith("syntax ")) {
                List<String> q = extractQuoted(line.substring(7));
                List<Pattern> exts = new ArrayList<>();
                for (int i = 1; i < q.size(); i++) {
                    exts.add(Pattern.compile(posixToJavaRegex(q.get(i))));
                }
                current = new Syntax(q.isEmpty() ? "unknown" : q.get(0), exts, new ArrayList<>());
                syntaxes.add(current);
            } else if (current != null && (line.startsWith("color ") || line.startsWith("icolor "))) {
                boolean icase = line.startsWith("icolor ");
                String rest = line.substring(icase ? 7 : 6).trim();
                int sp = rest.indexOf(' ');
                if (sp < 0) continue;
                String colorSpec = rest.substring(0, sp);
                String body = rest.substring(sp).trim();
                int flags = Pattern.MULTILINE | (icase ? Pattern.CASE_INSENSITIVE : 0);
                List<String> q = extractQuoted(body);

                if (body.startsWith("start=") && q.size() >= 2) {
                    current.rules().add(new Rule(
                        true, colorSpec,
                        Pattern.compile(posixToJavaRegex(q.get(0)), flags),
                        Pattern.compile(posixToJavaRegex(q.get(1)), flags)
                    ));
                } else {
                    for (String pat : q) {
                        current.rules().add(new Rule(false, colorSpec, Pattern.compile(posixToJavaRegex(pat), flags), null));
                    }
                }
            }
        }
        return syntaxes;
    }

    public static String highlight(String code, Syntax syntax, boolean html) {
        int n = code.length();
        String[] styles = new String[n];
        Arrays.fill(styles, "");

        for (Rule rule : syntax.rules()) {
            if (!rule.isBlock()) {
                Matcher m = rule.start().matcher(code);
                while (m.find()) {
                    for (int i = m.start(); i < m.end(); i++) styles[i] = rule.color();
                }
            } else if (rule.end() != null) {
                int pos = 0;
                while (pos < n) {
                    Matcher ms = rule.start().matcher(code);
                    if (!ms.find(pos)) break;
                    Matcher me = rule.end().matcher(code);
                    int endPos = me.find(ms.end()) ? me.end() : n;
                    for (int i = ms.start(); i < endPos; i++) styles[i] = rule.color();
                    pos = Math.max(endPos, ms.start() + 1);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        String cur = "";
        for (int i = 0; i < n; i++) {
            String st = styles[i];
            if (!st.equals(cur)) {
                if (!cur.isEmpty()) sb.append(html ? "</span>" : "\u001B[0m");
                if (!st.isEmpty()) {
                    sb.append(html ? "<span class=\"nc-" + st.replace(',', '-') + "\">" : colorToAnsi(st));
                }
                cur = st;
            }
            char ch = code.charAt(i);
            if (html && ch == '<') sb.append("&lt;");
            else if (html && ch == '>') sb.append("&gt;");
            else if (html && ch == '&') sb.append("&amp;");
            else sb.append(ch);
        }
        if (!cur.isEmpty()) sb.append(html ? "</span>" : "\u001B[0m");
        return sb.toString();
    }
}
