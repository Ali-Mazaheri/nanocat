using Nanocat;
using Xunit;

namespace Nanocat.Tests;

public class HighlighterTests
{
    [Fact]
    public void Highlight_ProducesAnsiAndHtml()
    {
        var rc = "syntax \"demo\" \"\\.demo$\"\ncolor green \"\\<return\\>\"\n";
        var syntaxes = Highlighter.ParseNanorc(rc);
        Assert.Single(syntaxes);

        var ansi = Highlighter.Highlight("return 0;", syntaxes, html: false);
        var html = Highlighter.Highlight("return 0;", syntaxes, html: true);

        Assert.Contains("\x1b[32mreturn\x1b[0m", ansi);
        Assert.Contains("<span class=\"nc-green\">return</span>", html);
    }
}
