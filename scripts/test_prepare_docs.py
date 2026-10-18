"""Regression checks for documentation assembly; uses only the standard library."""

from pathlib import Path
import tempfile
import unittest

from prepare_docs import REPOSITORY, local_api_links, prepare, relocate_links, revision_links

REVISION = "a" * 40


class PreparationTest(unittest.TestCase):
    def test_homepage_links_preserve_fragments_and_external_urls(self):
        text = "[Guide](docs/guide.md#testing) [Root](LICENSE) [Web](https://example.org/docs/#x) [Here](#x)"
        result = relocate_links(text, Path("README.md"), REVISION)
        self.assertEqual(result, f"[Guide](guide.md#testing) [Root]({REPOSITORY}/blob/{REVISION}/LICENSE) [Web](https://example.org/docs/#x) [Here](#x)")

    def test_repository_root_paths_remain_local_guides(self):
        self.assertEqual(relocate_links("[Guide](/docs/guide.md#testing)", Path("README.md"), REVISION), "[Guide](guide.md#testing)")

    def test_published_api_links_stay_inside_the_preview(self):
        self.assertEqual(local_api_links("[API](https://atick.dev/Jetpack-Android-Starter/api/core/ui/index.html#x)"), "[API](api/core/ui/index.html#x)")

    def test_tooling_links_relocate_to_parent_guide(self):
        self.assertEqual(relocate_links("[Guide](../docs/guide.md#testing)", Path("lint/README.md"), REVISION), "[Guide](../guide.md#testing)")

    def test_history_pins_are_preserved(self):
        historical = f"{REPOSITORY}/blob/{'b' * 40}/docs/guide.md"
        self.assertEqual(revision_links(historical, REVISION), historical)
        self.assertEqual(revision_links(f"{REPOSITORY}/blob/main/LICENSE", REVISION), f"{REPOSITORY}/blob/{REVISION}/LICENSE")

    def test_missing_api_fails_before_replacing_output(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            stale = root / "build/docs-source/stale.html"
            stale.parent.mkdir(parents=True)
            stale.write_text("keep")
            with self.assertRaisesRegex(ValueError, "Missing real Dokka"):
                prepare(root, REVISION)
            self.assertTrue(stale.exists())

    def test_preparation_removes_stale_api_pages(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "build/dokka/html").mkdir(parents=True)
            (root / "build/dokka/html/index.html").write_text("real")
            (root / "docs").mkdir()
            (root / "README.md").write_text("Homepage")
            for module in ("benchmarks", "lint"):
                (root / module).mkdir()
                (root / module / "README.md").write_text("Reference")
            stale = root / "build/docs-source/api/deleted.html"
            stale.parent.mkdir(parents=True)
            stale.write_text("stale")
            prepare(root, REVISION)
            self.assertFalse(stale.exists())
            self.assertEqual((stale.parent / "index.html").read_text(), "real")
            self.assertEqual((root / "build/docs-source/reference/lint.md").read_text(), "Reference")


if __name__ == "__main__":
    unittest.main()
