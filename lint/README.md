# Module :lint

**Purpose:** Two custom lint checks, published to every module that depends on `core:ui` via
`lintPublish(projects.lint)` (`core/ui/build.gradle.kts:78`) — no per-module wiring needed.

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

- `TestMethodNameDetector` deliberately does not port Now in Android's `given_when_then` naming
  check — this repo uses Kotlin backtick sentence names for tests, and that rule would flag all of
  them.

## Related Documentation

- [Components § Enforcement](../docs/components.md#enforcement) — the full `DesignSystemDetector` rationale and its method-name table
