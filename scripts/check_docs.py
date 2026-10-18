"""Check local documentation links in authored files and assembled HTML."""

import argparse
from html.parser import HTMLParser
import re
import subprocess
from pathlib import Path
from urllib.parse import unquote, urlsplit

import markdown

from prepare_docs import REPOSITORY

class Links(HTMLParser):
    def __init__(self, text):
        super().__init__(convert_charrefs=True)
        self.targets = set()
        self.links = []
        self.text = []
        self.feed(text)

    def handle_data(self, text):
        self.text.append(text)

    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if attrs.get("id"):
            self.targets.add(attrs["id"])
        if tag == "a" and attrs.get("name"):
            self.targets.add(attrs["name"])
        for name in ("href", "src"):
            if attrs.get(name):
                self.links.append(attrs[name])


def parsed(path):
    text = path.read_text(encoding="utf-8")
    if path.suffix == ".md":
        text = markdown.markdown(text, extensions=["toc", "tables", "fenced_code", "md_in_html"])
    return Links(text)


def check(paths, root, site=False, repository_root=None, revision=None):
    repository_root = repository_root or root
    cache = {}
    errors = []
    count = 0

    def page_links(path):
        if path not in cache:
            cache[path] = parsed(path)
        return cache[path]

    for path in paths:
        page = page_links(path)
        for link in page.links:
            url = urlsplit(link)
            if url.scheme or url.netloc:
                # Verify active repository links against this checkout, not old evidence pins.
                match = re.match(re.escape(REPOSITORY) + r"/(?:blob|tree|raw)/([^/]+)/(.+)", link)
                if not match or match[1] not in {"main", revision}:
                    continue
                url = urlsplit(match[2])
                target = repository_root / unquote(url.path)
                if target.is_dir() and not url.fragment:
                    count += 1
                    continue
            elif not url.path:
                target = path
            else:
                target = (root if url.path.startswith("/") else path.parent) / unquote(url.path).lstrip("/")
            target = target.resolve()
            if target.is_dir():
                if not site and not url.fragment:
                    count += 1
                    continue
                target = target / ("index.html" if site else "README.md")
            if not target.exists():
                errors.append(f"{path.relative_to(root)}: missing {link}")
                continue
            count += 1
            if url.fragment and target.suffix in (".md", ".html"):
                other = page_links(target)
                fragment = unquote(url.fragment)
                # GitHub source line links are meaningful for source files, not document headings.
                if (
                    not re.fullmatch(r"L\d+(?:-L\d+)?", fragment)
                    and fragment not in other.targets
                    and url.fragment not in other.targets
                ):
                    errors.append(f"{path.relative_to(root)}: missing fragment {link}")
    return count, errors


def check_introductions(site, root):
    """Catch successful API builds that silently omit module README content."""
    modules = re.findall(
        r"^\s*dokka\(projects\.([\w.]+)\)",
        (root / "build.gradle.kts").read_text(),
        re.MULTILINE,
    )
    errors = []

    def normalize(parts):
        return re.sub(r"\s+", " ", "".join(parts)).strip()

    for module in modules + ["benchmarks", "lint"]:
        source = root / module.replace(".", "/") / "README.md"
        page = (
            site / "reference" / module / "index.html"
            if module in {"benchmarks", "lint"}
            else site / "api" / module.replace(".", "/") / "index.html"
        )
        if not page.is_file():
            errors.append(f"Missing module reference: {page.relative_to(site)}")
            continue
        introduction = source.read_text().split("\n\n", 2)[1]
        expected = Links(markdown.markdown(introduction))
        if normalize(expected.text) not in normalize(parsed(page).text):
            errors.append(f"Missing README introduction: {page.relative_to(site)}")
    return errors


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--site", type=Path, help="Also check a built site")
    args = parser.parse_args()
    root = Path.cwd().resolve()
    tracked = subprocess.check_output(["git", "ls-files", "-z", "*.md"], text=True).split("\0")
    paths = [root / name for name in tracked if name and (root / name).exists()]
    # Include newly authored compatibility pages before they are staged.
    paths = sorted(
        (set(paths) | set((root / "docs").glob("*.md"))) - {root / "docs/index.md"}
    )
    revision = subprocess.check_output(["git", "rev-parse", "HEAD"], text=True).strip()
    count, errors = check(paths, root, revision=revision)
    if args.site:
        site = args.site.resolve()
        if not (site / "index.html").is_file():
            parser.error("--site must contain the real built homepage")
        # Dokka fetches only api/navigation.html via pathToRoot. Per-module copies are
        # intermediate fragments whose relative links are not standalone browser pages.
        html = [
            p for p in site.rglob("*.html")
            if p.name != "navigation.html" or p == site / "api/navigation.html"
        ]
        site_count, site_errors = check(
            sorted(html), site, site=True, repository_root=root, revision=revision
        )
        count += site_count
        errors += site_errors + check_introductions(site, root)
    for error in errors:
        print(error)
    print(f"Checked {count} local link destinations; {len(errors)} errors")
    raise SystemExit(bool(errors))


if __name__ == "__main__":
    main()
