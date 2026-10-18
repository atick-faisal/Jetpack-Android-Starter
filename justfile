default:
    @just --list --unsorted

# Run every check CI runs, in CI order. Runs them all, then prints one summary.
verify:
    #!/usr/bin/env bash
    failed=""
    for check in spotless convention-plugins dependency-guard test lint-tests build android-lint badging docs; do
        echo "▶ just $check"
        {{just_executable()}} "$check" || failed="$failed $check"
    done
    if [ -n "$failed" ]; then echo "✗ failed:$failed"; exit 1; fi
    echo "✓ all checks passed"

# Formatting with ktlint (ci.yml › lint)
spotless:
    ./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache

# Convention plugins in build-logic (ci.yml › lint)
convention-plugins:
    ./gradlew :build-logic:convention:check

# Release classpath matches app/dependencies/ (ci.yml › build)
dependency-guard:
    ./gradlew :app:dependencyGuard

# Unit tests, all modules (ci.yml › build)
test:
    ./gradlew test

# Custom lint rule tests (ci.yml › build)
lint-tests:
    ./gradlew :lint:test

# Full build, including default-variant lint (ci.yml › build)
build:
    ./gradlew build

# Android Lint on the release variant (ci.yml › build)
android-lint:
    ./gradlew :app:lintRelease

# Release manifest/permissions match the badging golden file (ci.yml › build)
badging:
    ./gradlew :app:checkReleaseBadging

# Docs: script tests, real Dokka, strict MkDocs build, link check (docs.yml)
docs:
    .venv/bin/python -m unittest discover -s scripts -p 'test_*.py'
    ./gradlew dokkaGeneratePublicationHtml -PdocsRevision="$(git rev-parse HEAD)"
    .venv/bin/python scripts/prepare_docs.py
    .venv/bin/python -m mkdocs build --strict --config-file build/docs-mkdocs.yml
    .venv/bin/python scripts/check_docs.py --site site

# Auto-format (run before every commit)
format:
    ./gradlew spotlessApply --init-script gradle/init.gradle.kts --no-configuration-cache
