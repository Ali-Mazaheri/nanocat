package io.github.alimazaheri.nanocat;

import java.util.List;

public class NanocatTest {
    public static void main(String[] args) {
        String rc = "syntax \"demo\" \"\\.demo$\"\ncolor green \"\\<return\\>\"\n";
        List<Nanocat.Syntax> syntaxes = Nanocat.parseNanorc(rc);
        assert syntaxes.size() == 1 : "Expected 1 syntax";

        String ansi = Nanocat.highlight("return 1;", syntaxes.get(0), false);
        String html = Nanocat.highlight("return 1;", syntaxes.get(0), true);

        assert ansi.contains("\u001B[32mreturn\u001B[0m") : "Missing ANSI green";
        assert html.contains("<span class=\"nc-green\">return</span>") : "Missing HTML span";
        System.out.println("Java unit tests passed!");
    }
}
