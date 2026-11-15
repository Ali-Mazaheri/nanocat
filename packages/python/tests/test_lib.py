import os
import sys
import tempfile
import unittest

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
from nanocat_lib import highlight


class TestPythonLib(unittest.TestCase):
    def test_highlight_ansi_and_html(self):
        rc = 'syntax "demo" "\\.demo$"\ncolor green "\\<return\\>"\n'
        with tempfile.TemporaryDirectory() as tmpdir:
            rc_file = os.path.join(tmpdir, "demo.nanorc")
            with open(rc_file, "w", encoding="utf-8") as f:
                f.write(rc)
            ansi = highlight("return 1;", lang="demo", output_format="ansi", nanorc_dirs=[tmpdir])
            html = highlight("return 1;", lang="demo", output_format="html", nanorc_dirs=[tmpdir])
            self.assertIn("\033[32mreturn\033[0m", ansi)
            self.assertIn('<span class="nc-32">return</span>', html)


if __name__ == "__main__":
    unittest.main()
