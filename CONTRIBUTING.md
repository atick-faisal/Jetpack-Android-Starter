## Feeling Awesome! Thanks for thinking about this.

You can contribute us by filing issues, bugs and PRs. You can also take a look at active issues and fix them.

If you want to discuss on something then feel free to present your opinions, views or any other relevant comment on [discussions](https://github.com/atick-faisal/Jetpack-Android-Starter/discussions).

### Code contribution

- Open issue regarding proposed change.
- If your proposed change is approved, Fork this repo and do changes.
- Open PR against latest *development* branch. Add nice description in PR.
- You're done!

### Code contribution checklist

- New code addition/deletion should not break existing flow of a system.
- All tests should be passed.
- Verify `./gradlew build` is passing before raising a PR.
- Reformat code with Spotless before raising a PR: `./gradlew spotlessApply --init-script gradle/init.gradle.kts`
  (the init script flag is required — see [Build & Tooling](docs/build-and-tooling.md)).

### Comments and docs

Code comments explain *why*, not *what* — the signature already says what. Match the plain-paragraph,
no-headings, no-bullets style used in `build-logic/` and `core/navigation/`, e.g.
`core/navigation/NavigationState.kt:48-50`. Links are bare URLs inline, not markdown syntax. Issue
tracking uses `// Tracking: GitHub Issue #NNN`.

A small, fixed vocabulary marks rationale and traps, expressed two ways — an emoji marker as the
first line of a code comment, or a GitHub alert blockquote in markdown. Learning one teaches the
other, since they mean the same thing everywhere:

| Alert | Emoji | Severity | Use when |
|---|---|---|---|
| `> [!NOTE]` | ℹ️ | Context | Background the reader needs, implying no action |
| `> [!TIP]` | 💡 | Rationale | The code looks wrong or arbitrary until you know why |
| `> [!IMPORTANT]` | ❗ | Contract | A contract the caller must uphold, easy to violate silently |
| `> [!WARNING]` | ⚠️ | Trap | Changing this without knowing will introduce a bug |
| `> [!CAUTION]` | ☠️ | Severe | Leaks credentials, corrupts data, or ships an unsigned build |

In code: one marker per comment block, first line only, never inside a KDoc `@param`/`@return`/
`@throws`. Most rationale comments carry no marker at all — reserve one for the cases above.

In markdown: use ALL-CAPS qualifiers exactly as shown (`NOTE`, `TIP`, `IMPORTANT`, `WARNING`,
`CAUTION` — no other type is recognized) and keep pages to one or two alerts, per GitHub's own
guidance.

> [!WARNING]
> Never write native `!!! type` or `??? type` admonition syntax — it renders as literal broken text
> on GitHub. Every markdown file in this repo is read on GitHub as well as through MkDocs, so `> [!TYPE]`
> blockquotes are the only form that works in both places.