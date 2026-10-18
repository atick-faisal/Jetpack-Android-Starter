"""Assemble real documentation inputs without modifying authored Markdown."""

import argparse
import json
import re
import shutil
import subprocess
from pathlib import Path
from urllib.parse import urlsplit

REPOSITORY = "https://github.com/atick-faisal/Jetpack-Android-Starter"


def revision_links(text, revision):
    return re.sub(
        re.escape(REPOSITORY) + r"/(blob|tree)/main/",
        lambda match: f"{REPOSITORY}/{match[1]}/{revision}/",
        text,
    )


def local_api_links(text, prefix=""):
    return re.sub(
        r"(\]\()https://atick\.dev/Jetpack-Android-Starter/api/([^)]*)(\))",
        lambda match: match[1] + prefix + "api/" + (match[2] or "index.html") + match[3],
        text,
    )


def relocate_links(text, source, revision, root=None):
    """Keep guides local and send repository-only files to the documented revision."""
    root = (root or Path.cwd()).resolve()

    def replace(match):
        destination = match[2]
        url = urlsplit(destination)
        if url.scheme or url.netloc or not url.path:
            return match[0]
        base = root if url.path.startswith("/") else root / source.parent
        target = (base / url.path.lstrip("/")).resolve()
        try:
            relative = target.relative_to(root)
        except ValueError:
            raise ValueError(f"Link escapes repository: {source}: {destination}")
        suffix = ("?" + url.query if url.query else "") + ("#" + url.fragment if url.fragment else "")
        if relative.parts[0] == "docs":
            link = "/".join(relative.parts[1:])
            if source != Path("README.md"):
                link = "../" + link
        else:
            link = f"{REPOSITORY}/blob/{revision}/{relative.as_posix()}"
        return match[1] + link + suffix + match[3]

    # Repository-authored links use inline Markdown destinations without spaces.
    return re.sub(r"(!?\[[^\]\n]*\]\()([^\s)]+)(\))", replace, text)


def prepare(root, revision):
    api = root / "build/dokka/html"
    if not (api / "index.html").is_file():
        raise ValueError("Missing real Dokka output: run ./gradlew dokkaGeneratePublicationHtml first")
    output = root / "build/docs-source"
    if output.exists():
        shutil.rmtree(output)
    shutil.copytree(root / "docs", output, ignore=shutil.ignore_patterns("api", "index.md"))
    shutil.copytree(api, output / "api")
    for page in output.rglob("*.md"):
        page.write_text(local_api_links(revision_links(page.read_text(), revision)), encoding="utf-8")
    for page in (output / "api").rglob("*.html"):
        text = revision_links(page.read_text(), revision)
        page.write_text(text, encoding="utf-8")
    readme = relocate_links((root / "README.md").read_text(), Path("README.md"), revision, root)
    readme = local_api_links(readme).replace(
        "](https://atick.dev/Jetpack-Android-Starter)", "](index.md)"
    )
    (output / "index.md").write_text(revision_links(readme, revision), encoding="utf-8")
    reference = output / "reference"
    reference.mkdir(exist_ok=True)
    for module in ("benchmarks", "lint"):
        source = Path(module) / "README.md"
        text = (root / source).read_text().replace(f"# Module :{module}", f"# {module.title()}", 1)
        (reference / f"{module}.md").write_text(
            local_api_links(revision_links(relocate_links(text, source, revision, root), revision), "../"), encoding="utf-8"
        )
    config = root / "build/docs-mkdocs.yml"
    config.write_text(
        "INHERIT: " + json.dumps(str(root / "mkdocs.yml")) + "\n"
        + "hooks: " + json.dumps([str(root / "scripts/docs_hooks.py")]) + "\n"
        + "docs_dir: " + json.dumps(str(output)) + "\n"
        + "site_dir: " + json.dumps(str(root / "site")) + "\n"
        + "repo_url: " + json.dumps(f"{REPOSITORY}/tree/{revision}") + "\n"
        + "edit_uri: " + json.dumps(f"{REPOSITORY}/edit/{revision}/docs/") + "\n",
        encoding="utf-8",
    )
    return config


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--revision", help="Documented commit (defaults to HEAD)")
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    if Path.cwd().resolve() != root:
        parser.error("Run from the repository root")
    revision = args.revision or subprocess.check_output(["git", "rev-parse", "HEAD"], text=True).strip()
    if not re.fullmatch(r"[0-9a-f]{40}", revision):
        parser.error("--revision must be a full commit SHA")
    print(prepare(root, revision))


if __name__ == "__main__":
    main()
