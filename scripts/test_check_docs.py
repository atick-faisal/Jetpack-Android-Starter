"""Ensure the link gate detects failures instead of merely parsing valid pages."""

from pathlib import Path
import tempfile
import unittest

from check_docs import check, check_introductions


class LinkCheckTest(unittest.TestCase):
    def test_missing_file_fragment_and_image_fail(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            page = root / "index.html"
            page.write_text('<a href="gone.html">Missing</a><a href="#absent">Fragment</a><img src="gone.png">')
            _, errors = check([page], root, site=True)
            self.assertEqual(len(errors), 3)
            self.assertTrue(any("missing fragment" in error for error in errors))

    def test_encoded_dokka_anchors_and_directory_links_resolve(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            page = root / "index.html"
            page.write_text('<a id="123%2FFunctions"></a><a href="#123%2FFunctions">Member</a><a href="child/">Child</a>')
            (root / "child").mkdir()
            (root / "child/index.html").write_text("Child")
            count, errors = check([page], root, site=True)
            self.assertEqual(count, 2)
            self.assertEqual(errors, [])

    def test_missing_module_introduction_fails_even_with_valid_api_page(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "build.gradle.kts").write_text("dokka(projects.core.example)")
            (root / "core/example").mkdir(parents=True)
            (root / "core/example/README.md").write_text("# Module :core:example\n\nUseful **module** introduction.")
            site = root / "site"
            page = site / "api/core/example/index.html"
            page.parent.mkdir(parents=True)
            page.write_text("<h1>API page without introduction</h1>")
            for module in ("benchmarks", "lint"):
                (root / module).mkdir()
                (root / module / "README.md").write_text("# Module\n\nTooling introduction.")
                tooling = site / "reference" / module / "index.html"
                tooling.parent.mkdir(parents=True)
                tooling.write_text("<p>Tooling introduction.</p>")
            self.assertEqual(len(check_introductions(site, root)), 1)
            page.write_text("<p>Useful <strong>module</strong> introduction.</p>")
            self.assertEqual(check_introductions(site, root), [])

    def test_markdown_fragment_and_external_url(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            page = root / "README.md"
            (root / "sources").mkdir()
            page.write_text("# Example\n\n[Here](#example) [Directory](sources/) [External](https://example.org/missing)")
            count, errors = check([page], root)
            self.assertEqual(count, 2)
            self.assertEqual(errors, [])


if __name__ == "__main__":
    unittest.main()
