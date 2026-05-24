# Luna

Android app (`com.tarmiga.luna`) — WebView shell with a Compose theme layer.

[![CI](https://github.com/tarmiga/Luna/actions/workflows/ci.yml/badge.svg)](https://github.com/tarmiga/Luna/actions/workflows/ci.yml)

> Update the badge URL if your GitHub org/repo name differs.

## Requirements

- JDK 21 (Gradle toolchain)
- Android SDK (via Android Studio)

## Local setup

```bash
git clone <your-repo-url>
cd Luna
./gradlew assembleDebug   # installs the pre-commit hook on first build
```

The pre-commit hook runs `ktfmtCheck` before each commit. Fix formatting with:

```bash
./gradlew ktfmtFormat
```

## Quality commands

| Command | Purpose |
| --- | --- |
| `./gradlew ktfmtCheck` | Verify Kotlin formatting (Google / 2-space style) |
| `./gradlew ktfmtFormat` | Auto-format Kotlin sources |
| `./gradlew detekt` | Static analysis / code smells |
| `./gradlew lintDebug` | Android Lint (debug variant) |
| `./gradlew testDebugUnitTest` | JVM unit tests |
| `./gradlew assembleDebug` | Debug APK |

Run the full CI suite locally:

```bash
./gradlew ktfmtCheck detekt lintDebug testDebugUnitTest assembleDebug
```

## CI

GitHub Actions (`.github/workflows/ci.yml`) runs on every PR and on pushes to `main`. Failed runs upload `build/reports/**` as artifacts.

Dependabot opens weekly PRs for Gradle and GitHub Actions updates.
