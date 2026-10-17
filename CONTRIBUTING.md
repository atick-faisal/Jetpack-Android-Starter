## Feeling Awesome! Thanks for thinking about this.

You can contribute us by filing issues, bugs and PRs. You can also take a look at active issues and fix them.

If you want to discuss on something then feel free to present your opinions, views or any other relevant comment on [discussions](https://github.com/atick-faisal/Jetpack-Android-Starter/discussions).

### Code contribution

- Open issue regarding proposed change.
- If your proposed change is approved, Fork this repo and do changes.
- Open PR against latest `main` branch. Add nice description in PR.
- You're done!

### Code contribution checklist

- New code addition/deletion should not break existing flow of a system.
- All tests should be passed. Run `./gradlew check` before committing.
- Verify `./gradlew build` is passing before raising a PR.
- Reformat code with Spotless before raising a PR: `./gradlew spotlessApply --init-script gradle/init.gradle.kts --no-configuration-cache`
  (the init script flag is required — see [Build & Tooling](docs/build-and-tooling.md)).

### Comments and docs

Write comments and documentation that help the reader understand behavior or finish a task.
Comments can explain behavior, contracts, units, lifetime, ordering, constraints, or a reason
supported by evidence. Use the shortest explanation that preserves the useful fact; move longer
background into a guide and link to it. Lists, Markdown links, and KDoc tags such as `@param`,
`@return`, and `@throws` are welcome where they improve readability.

For example, this comment explains a filter that a function name alone does not convey:

```kotlin
// Returns this user's jetpacks, excluding rows marked as deleted.
```

That describes the current `JetpackDao.getJetpacks()` query. Explain limitations directly, and
check claims against the implementation. Avoid invented motives, blame, self-congratulation,
unsupported guarantees, blanket bans on reasonable alternatives, and repeated accounts of fixed bugs.

Tutorials should give the steps, expected results, and relevant failure cases. Use current code
when teaching an existing pattern. For example, this excerpt from
[LocalDataSourceImpl](core/room/src/main/kotlin/dev/atick/core/room/data/LocalDataSourceImpl.kt)
shows how the list flow is delegated to Room and its upstream work moved to the IO dispatcher:

```kotlin
override fun getJetpacks(userId: String): Flow<List<JetpackEntity>> =
    jetpackDao.getJetpacks(userId).flowOn(ioDispatcher)
```

This is a method excerpt; its containing class, imports, constructor, and other methods are omitted.
Label shortened excerpts and explain their omissions. Label hypothetical additions as proposed
extensions and describe what the reader must add; do not present them as existing APIs.

Choose document length and structure for the reader's task. There are no numeric line or alert
budgets, required emoji markers, or required mappings between code comments and Markdown alerts.
Keep warnings that help readers avoid concrete mistakes, such as signing a release with the debug
key or losing local data during a destructive migration.

For Markdown alerts, use GitHub-compatible blockquotes with `NOTE`, `TIP`, `IMPORTANT`, `WARNING`,
or `CAUTION`, for example:

> [!WARNING]
> A release built without `keystore.properties` falls back to the debug signing key. Configure
> release signing before distributing the app; see [Release build setup](docs/getting-started.md#release-build-setup).

Use this blockquote syntax for alerts read on GitHub and MkDocs. Native `!!! type` and `??? type`
admonitions render as literal text on GitHub.
