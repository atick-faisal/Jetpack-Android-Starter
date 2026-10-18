# Module :lint

**Purpose:** Two custom lint checks, published to every module that depends on `core:ui` via
`lintPublish(projects.lint)` (`core/ui/build.gradle.kts`) — no per-module wiring needed.

## Key APIs

| API | What it does |
|---|---|
| `DesignSystemDetector` | ERROR-severity — flags a raw Material 3 composable (`Button`, `TextField`, `*TopAppBar`, …) that has a `Jetpack*` equivalent, resolved by symbol not by name |
| `TestMethodNameDetector` | WARNING-severity — flags a `@Test` method whose name starts with `test`; the annotation already says what it is |
| `JetpackIssueRegistry` | Registers both checks; declares the `Vendor` metadata lint shows in its output |

```kotlin
// lint/src/main/kotlin/dev/atick/lint/JetpackIssueRegistry.kt
override val issues = listOf(
    DesignSystemDetector.ISSUE,
    TestMethodNameDetector.PREFIX,
)
```

## Gotchas

- `TestMethodNameDetector` checks methods annotated with `org.junit.Test` whose names start with
  lowercase `test`; it does not enforce a sentence or `given_when_then` format.
- Run `./gradlew :lint:test` after changing a detector. The component guide covers adding a wrapper
  and updating detector coverage; custom lint runs in consuming modules through Android lint.

## Related Documentation

- [Components § Enforcement](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/components.md#enforcement) — the full `DesignSystemDetector` rationale and its method-name table
