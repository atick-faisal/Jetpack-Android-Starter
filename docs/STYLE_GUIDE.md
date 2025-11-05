# Documentation Style Guide

This guide defines the standards and conventions for writing documentation in the Jetpack Android Starter project.

---

## Table of Contents

1. [Admonitions](#admonitions)
2. [Terminology](#terminology)
3. [Code Examples](#code-examples)
4. [Cross-References](#cross-references)
5. [Formatting](#formatting)
6. [Tone and Voice](#tone-and-voice)

---

## Admonitions

### Overview

Use GitHub Flavored Markdown admonitions to highlight important information. **Never use emojis** in place of admonitions.

### Supported Types

| Admonition | Purpose | When to Use |
|------------|---------|-------------|
| `> [!NOTE]` | General information, context, clarifications | Version requirements, background info, additional context |
| `> [!TIP]` | Helpful suggestions, best practices | Performance tips, shortcuts, optimization advice |
| `> [!IMPORTANT]` | Critical information | Required steps, must-know information, breaking changes |
| `> [!WARNING]` | Potential issues or pitfalls | Common mistakes, compatibility issues, deprecated features |
| `> [!CAUTION]` | Strong warnings about dangerous operations | Data loss risks, security concerns, irreversible actions |

### Examples

#### NOTE - General Information

```markdown
> [!NOTE]
> This project uses a two-layer architecture (UI + Data) instead of the traditional three-layer
> pattern. A domain layer can be added later if your app requires complex business logic.
```

#### TIP - Helpful Suggestions

```markdown
> [!TIP]
> Use `suspendRunCatching` in repositories to handle errors consistently across your data layer.
```

#### IMPORTANT - Critical Information

```markdown
> [!IMPORTANT]
> Always run `./gradlew spotlessApply` before committing to ensure code formatting compliance.
```

#### WARNING - Potential Issues

```markdown
> [!WARNING]
> Don't mix different versions of the same library family. Use BOMs when available to ensure
> compatibility.
```

#### CAUTION - Dangerous Operations

```markdown
> [!CAUTION]
> Never commit your `keystore.properties` or `google-services.json` files to the repository.
> These contain sensitive credentials.
```

### Formatting Guidelines

1. **Always use blank lines** before and after admonitions:
   ```markdown
   Regular paragraph text.

   > [!NOTE]
   > Admonition content here.

   More regular text.
   ```

2. **Multi-line admonitions** use `>` prefix on each line:
   ```markdown
   > [!TIP]
   > For best performance:
   > - Use `remember` for expensive calculations
   > - Minimize recompositions
   > - Profile with Layout Inspector
   ```

3. **Keep content concise** - If admonition is > 5 lines, consider restructuring

4. **No nested admonitions** - Don't put admonitions inside admonitions

### Emoji Replacement

| Old Emoji | New Admonition | Example |
|-----------|----------------|---------|
| 🚧 | `> [!NOTE]` with context | Upcoming features, work in progress |
| ⚠️ | `> [!WARNING]` | Warnings and cautions |
| 💡 | `> [!TIP]` | Tips and suggestions |
| 📝 | `> [!NOTE]` | General notes |
| 🎯 | `> [!IMPORTANT]` | Key objectives |
| ✅ ❌ | Use in code examples only | Anti-patterns, correct/incorrect code |

---

## Terminology

### Consistent Terms

Use these preferred terms throughout documentation:

| Preferred | Avoid | Context |
|-----------|-------|---------|
| Screen Data | UI State, Screen State | Data class for screen state (e.g., `HomeScreenData`) |
| Repository | Data Source | Unless specifically referring to `NetworkDataSource` or `LocalDataSource` |
| ViewModel | View Model | Always one word, camelCase in code |
| Composable | Component | UI functions (avoid "Component" unless referring to Android components) |
| `updateStateWith` | update state with | Function name in code references |
| type-safe navigation | type safe navigation | Hyphenated when used as adjective |
| two-layer architecture | two layer architecture | Hyphenated when used as adjective |

### Technical Terms

- **UiState** - Always capitalize 'U' and 'S' when referring to the wrapper class
- **StatefulComposable** - PascalCase for the composable function
- **OneTimeEvent** - PascalCase for the event class
- **NavController** - PascalCase, no space
- **StateFlow** - PascalCase, no space
- **ViewModel** - PascalCase, one word
- **suspendRunCatching** - camelCase for the function

### Naming Patterns

- **Feature modules**: lowercase with hyphens (e.g., `feature/auth`, `feature/home`)
- **Core modules**: lowercase with hyphens (e.g., `core/network`, `core/ui`)
- **Gradle modules**: use `:` prefix (e.g., `:core:network`, `:feature:auth`)
- **Files**: PascalCase for Kotlin files (e.g., `HomeViewModel.kt`)
- **Packages**: lowercase (e.g., `dev.atick.feature.home`)

---

## Code Examples

### Formatting

1. **Always specify language** for syntax highlighting:
   ```markdown
   ```kotlin
   fun example() { }
   ```
   ```

2. **Include comments** for complex code:
   ```kotlin
   // Load user profile from repository
   fun loadProfile(userId: String) {
       _uiState.updateStateWith {
           repository.getUser(userId)
       }
   }
   ```

3. **Use real examples** from the codebase when possible

4. **Mark anti-patterns**:
   ```kotlin
   // ❌ DON'T: Manage multiple state flows
   class BadViewModel : ViewModel() {
       private val _loading = MutableStateFlow(false)
       private val _error = MutableStateFlow<String?>(null)
   }

   // ✅ DO: Use single UiState
   class GoodViewModel : ViewModel() {
       private val _uiState = MutableStateFlow(UiState(ScreenData()))
   }
   ```

5. **File path comments** for context:
   ```kotlin
   // feature/home/src/main/kotlin/dev/atick/feature/home/ui/HomeViewModel.kt
   @HiltViewModel
   class HomeViewModel @Inject constructor(
       private val repository: HomeRepository
   ) : ViewModel() {
       // ...
   }
   ```

### Code Blocks in Lists

When including code in numbered or bulleted lists, indent the code block:

```markdown
1. **Step one**: Do something

   ```kotlin
   val example = "code"
   ```

2. **Step two**: Do something else
```

---

## Cross-References

### Internal Links

1. **Format**: `[Link Text](file.md)` or `[Link Text](file.md#section)`

2. **Examples**:
   ```markdown
   See the [Architecture Overview](architecture.md) for details.

   Learn about [State Management](state-management.md#update-functions-explained).
   ```

3. **Section anchors**: Use lowercase with hyphens:
   ```markdown
   [Update Functions](state-management.md#update-functions-explained)
   ```

### External Links

1. **Always provide context**:
   ```markdown
   Refer to the [official Android Architecture Guidelines](https://developer.android.com/topic/architecture)
   for more details.
   ```

2. **Use reference-style links** for repeated URLs:
   ```markdown
   [Kotlin Docs][kotlin-docs]
   [Kotlin Serialization][kotlin-docs]

   [kotlin-docs]: https://kotlinlang.org/docs/
   ```

### API Documentation Links

Link to Dokka-generated API docs when referencing specific APIs:

```markdown
For detailed API documentation, see [`StatefulComposable.kt`](../core/ui/src/main/kotlin/dev/atick/core/ui/utils/StatefulComposable.kt).
```

### Related Content

Include "See Also" or "Further Reading" sections:

```markdown
## Further Reading

- [State Management](state-management.md) - Deep dive into state patterns
- [Architecture Overview](architecture.md) - Understand the app structure
- [Adding Features](guide.md) - Step-by-step implementation guide
```

---

## Formatting

### Headers

1. **Use ATX-style headers** (with `#`):
   ```markdown
   # H1 - Document Title
   ## H2 - Major Section
   ### H3 - Subsection
   #### H4 - Minor Subsection
   ```

2. **One H1 per document** - The document title

3. **Consistent capitalization** - Title case for H1-H2, sentence case for H3-H4:
   ```markdown
   # Architecture Overview    ✅ Title case
   ## Core Concepts          ✅ Title case
   ### The UiState wrapper   ✅ Sentence case
   ```

### Lists

1. **Use `-` for unordered lists**:
   ```markdown
   - First item
   - Second item
   - Third item
   ```

2. **Use `1.` for ordered lists** (auto-numbering):
   ```markdown
   1. First step
   2. Second step
   3. Third step
   ```

3. **Consistent indentation** - 2 or 4 spaces for nested lists

### Emphasis

1. **Bold** for emphasis: `**important**`
2. **Italic** for terminology: `*repository pattern*`
3. **Code** for inline code: `` `updateState` ``
4. **Don't overuse** - Keep it readable

### Tables

1. **Use GitHub Flavored Markdown tables**:
   ```markdown
   | Column 1 | Column 2 | Column 3 |
   |----------|----------|----------|
   | Value 1  | Value 2  | Value 3  |
   ```

2. **Align headers** for readability in source

3. **Keep tables simple** - Max 4-5 columns

---

## Tone and Voice

### Writing Style

1. **Direct and concise** - Get to the point quickly
2. **Active voice** - "Create a ViewModel" not "A ViewModel should be created"
3. **Present tense** - "The function returns" not "The function will return"
4. **Second person** - "You can use" not "One can use" or "We can use"

### Examples

#### Good ✅

```markdown
The `updateStateWith` function handles loading states automatically. You don't need to
manually set `loading = true` or handle error states.

To create a new feature:
1. Define your screen data class
2. Create a ViewModel with UiState
3. Build your UI with StatefulComposable
```

#### Poor ❌

```markdown
It should be noted that the `updateStateWith` function has been designed to automatically
handle loading states. One would not need to manually set the loading state to true or
handle various error states that might occur.

The process of creating a new feature would typically involve defining a screen data class,
after which a ViewModel containing UiState should be created, followed by building the UI
using StatefulComposable.
```

### Audience

Write for developers who:
- Have Android development experience
- Are learning this template's patterns
- Want clear, actionable guidance

### Technical Depth

- **Getting Started**: High-level, minimal jargon
- **Core Concepts**: Moderate depth, explain key concepts
- **Deep Dives**: Technical detail, assume familiarity with basics
- **Reference**: Complete technical detail, API-focused

---

## Document Structure

### Standard Template

```markdown
# Document Title

Brief introduction (1-2 paragraphs explaining what this document covers).

---

## Table of Contents

1. [Section 1](#section-1)
2. [Section 2](#section-2)

---

## Section 1

Content...

---

## Section 2

Content...

---

## Summary

Key takeaways (bullet points or short paragraphs).

## Further Reading

- [Related Doc 1](link.md)
- [Related Doc 2](link.md)
```

### Section Organization

1. **Start with overview** - What problem does this solve?
2. **Core concepts** - Key ideas and terminology
3. **Examples** - Practical, working code
4. **Advanced patterns** - Edge cases and optimizations
5. **Best practices** - Do's and don'ts
6. **Summary** - Quick recap
7. **Further reading** - Related content

---

## File Organization

### Directory Structure

```
docs/
├── index.md                    # Landing page
├── getting-started.md          # Quick start
├── architecture.md             # High-level overview
├── philosophy.md               # Design principles
├── state-management.md         # Deep dive
├── components.md               # Deep dive
├── data-flow.md                # Deep dive
├── navigation.md               # Deep dive
├── dependency-injection.md     # Deep dive
├── guide.md                    # Practical tutorial
├── quick-reference.md          # Cheat sheet
├── troubleshooting.md          # Problem-solution pairs
├── faq.md                      # Question-answer pairs
├── dependency.md               # Tool config
├── plugins.md                  # Tool config
├── spotless.md                 # Tool config
├── firebase.md                 # Tool config
├── github.md                   # Tool config
├── fastlane.md                 # Tool config
├── performance.md              # Best practices
├── tips.md                     # Best practices
└── license.md                  # Legal
```

### Naming Conventions

- **Lowercase with hyphens**: `state-management.md` ✅
- **No underscores**: `state_management.md` ❌
- **Descriptive names**: `navigation.md` ✅ not `nav.md` ❌
- **Avoid abbreviations**: `dependency-injection.md` ✅ not `di.md` ❌

---

## Version Control

### Commit Messages

When updating documentation:

```bash
docs: revise state management guide for consistency

- Standardize admonitions to GitHub Flavored Markdown
- Remove content overlap with architecture.md
- Add cross-references to related guides
- Fix terminology inconsistencies
```

### Review Checklist

Before committing documentation changes:

- [ ] All admonitions use GitHub Flavored Markdown syntax
- [ ] No emojis (except in code examples)
- [ ] Terminology consistent with style guide
- [ ] All internal links verified
- [ ] Code examples tested and accurate
- [ ] Grammar and spelling checked
- [ ] Follows document structure template
- [ ] Cross-references added where helpful

---

## Maintenance

### Regular Reviews

Documentation should be reviewed:
- After major version updates
- When adding new features
- Quarterly for accuracy

### Updating This Guide

This style guide is a living document. When adding new conventions:
1. Update this guide first
2. Apply to existing docs
3. Document the change in CHANGELOG.local.md
