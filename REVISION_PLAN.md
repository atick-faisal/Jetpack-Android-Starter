# Documentation Revision Plan

## Audit Summary

### Current State
- 21 documentation files in `docs/` directory
- Already using GitHub Flavored Markdown admonitions (`> [!NOTE]`, `> [!TIP]`, `> [!WARNING]`)
- 82 emoji usages that need standardization
- MkDocs configured with `gh-admonitions` plugin

### Issues Identified

#### 1. Admonition Inconsistencies
- Mix of emojis and GFM admonitions
- Need standardization across all docs

#### 2. Potential Content Overlaps
- **Architecture concepts** appear in:
  - `architecture.md` (main overview)
  - `philosophy.md` (design principles)
  - `state-management.md` (state-specific architecture)
  - `guide.md` (practical examples)

- **State management** appears in:
  - `state-management.md` (comprehensive deep dive)
  - `architecture.md` (overview section)
  - `quick-reference.md` (cheat sheet)
  - `guide.md` (practical examples)

- **Navigation** appears in:
  - `navigation.md` (comprehensive guide)
  - `guide.md` (setup examples)
  - `quick-reference.md` (quick patterns)

- **Dependency Injection** appears in:
  - `dependency-injection.md` (comprehensive guide)
  - `guide.md` (setup examples)
  - `quick-reference.md` (quick patterns)

#### 3. Terminology Variations
Need to audit for consistent use of:
- "Repository" vs "Data Source"
- "Screen Data" vs "UI State" vs "Screen State"
- "ViewModel" vs "View Model"
- "Composable" vs "Component"
- "updateStateWith" vs "update state with"

#### 4. Cross-Reference Issues
- Some guides reference non-existent sections
- Missing links between related guides
- Inconsistent link formats

## Standardization Strategy

### GitHub Flavored Markdown Admonitions

Use these five standard admonition types:

| Admonition | Usage | Example |
|------------|-------|---------|
| `> [!NOTE]` | General information, context, or clarifications | Version requirements, background info |
| `> [!TIP]` | Helpful suggestions, best practices, pro tips | Performance tips, shortcuts |
| `> [!IMPORTANT]` | Critical information that must be understood | Breaking changes, required steps |
| `> [!WARNING]` | Warnings about potential issues or pitfalls | Security concerns, data loss risks |
| `> [!CAUTION]` | Strong warnings about dangerous operations | Destructive commands, production impacts |

### Emoji Replacement Map

| Current Emoji | Replacement Admonition | Rationale |
|--------------|----------------------|-----------|
| 🚧 (construction) | `> [!NOTE]` with "Upcoming" prefix | Indicates future features |
| ⚠️ (warning sign) | `> [!WARNING]` | Direct replacement |
| 💡 (light bulb) | `> [!TIP]` | Suggestions and tips |
| 📝 (memo) | `> [!NOTE]` | General information |
| ✅ (check mark) | Remove, use bold text | Indicates completion |
| ❌ (cross mark) | Use in code examples | Anti-pattern markers |
| 🎯 (target) | `> [!IMPORTANT]` | Key objectives |

## Revision Tasks

### Phase 1: Style Guide & Standards
1. **Create admonition style guide**
   - Define when to use each type
   - Provide examples for each
   - Add to documentation

2. **Create terminology guide**
   - Standardize technical terms
   - Define preferred variants
   - Document exceptions

### Phase 2: Content Consolidation
1. **Architecture documentation**
   - `architecture.md` → High-level overview only
   - `philosophy.md` → Design principles and rationale
   - `state-management.md` → Comprehensive state management
   - Remove duplications, add cross-references

2. **Practical guides**
   - `guide.md` → Complete feature implementation walkthrough
   - `quick-reference.md` → Quick lookup for common patterns
   - `components.md`, `data-flow.md`, `navigation.md`, `dependency-injection.md` → Deep dives
   - Ensure clear separation of concerns

3. **Troubleshooting & FAQ**
   - `troubleshooting.md` → Problem-solution pairs
   - `faq.md` → Question-answer pairs
   - Ensure no duplication, add cross-references

### Phase 3: Admonition Standardization
For each documentation file:
1. Replace all emojis with appropriate admonitions
2. Ensure consistent admonition formatting
3. Review admonition content for clarity
4. Add context where needed

### Phase 4: Terminology Standardization
1. Use "Screen Data" consistently for UI state classes
2. Use "updateStateWith" (camelCase) for function references
3. Use "Repository" for data layer (not "Data Source" unless specifically referring to NetworkDataSource/LocalDataSource)
4. Use "Composable" for UI components (not "Component" unless specifically referring to Android components)

### Phase 5: Cross-Reference Audit
1. Verify all internal links work
2. Add missing cross-references
3. Use consistent link format: `[Link Text](file.md#section)`
4. Add "See Also" sections where helpful

### Phase 6: Quality Check
1. Run through each guide as a new developer would
2. Ensure logical flow and completeness
3. Check for outdated information
4. Verify all code examples are accurate

## Implementation Order

1. ✅ Create revision plan (this document)
2. Create style guide document
3. Standardize admonitions in core concept docs
4. Consolidate overlapping content
5. Standardize terminology across all docs
6. Audit and fix cross-references
7. Final quality check
8. Update CHECKLIST.local.md and CHANGELOG.local.md
9. Create commit

## Files to Revise (Priority Order)

### High Priority (Core Concepts)
1. `architecture.md`
2. `philosophy.md`
3. `state-management.md`
4. `components.md`
5. `data-flow.md`
6. `navigation.md`
7. `dependency-injection.md`

### Medium Priority (Practical Guides)
8. `getting-started.md`
9. `guide.md`
10. `quick-reference.md`
11. `troubleshooting.md`
12. `faq.md`

### Lower Priority (Tools & Configuration)
13. `dependency.md`
14. `plugins.md`
15. `spotless.md`
16. `firebase.md`
17. `github.md`
18. `fastlane.md`

### Documentation-Only
19. `performance.md`
20. `tips.md`
21. `license.md` (skip - no changes needed)

## Success Criteria

- [ ] Zero emojis in documentation (except in code examples where appropriate)
- [ ] All admonitions use GitHub Flavored Markdown syntax
- [ ] No content duplication between guides
- [ ] Consistent terminology throughout
- [ ] All internal links verified
- [ ] Clear separation of concerns between guides
- [ ] Each guide has a clear target audience and purpose
- [ ] Professional, consistent tone throughout
