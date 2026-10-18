"""Point generated guide pages at their authored repository sources."""


def on_page_context(context, page, config, nav):
    sources = {
        "index.md": "README.md",
        "reference/benchmarks.md": "benchmarks/README.md",
        "reference/lint.md": "lint/README.md",
    }
    source = page.file.src_uri
    if source in sources and page.edit_url:
        page.edit_url = page.edit_url.removesuffix("docs/" + source) + sources[source]
    return context
