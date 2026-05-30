# Luna

Android app (`com.tarmiga.luna`) — WebView shell with a Compose theme layer.

[![CI](https://github.com/agorasecurity/Luna/actions/workflows/ci.yml/badge.svg)](https://github.com/agorasecurity/Luna/actions/workflows/ci.yml)

## Google Developer Account

Account ID: 5379539333161672670

Package name: com.tarmiga.luna

## Requirements

- JDK 21 (Gradle toolchain)
- Android SDK (via Android Studio)

## Local setup

```bash
git clone <your-repo-url>
cd Luna
make setup    # installs the git pre-commit hook
make build    # build the debug APK
```

The pre-commit hook runs `ktfmtCheck` before each commit. Fix formatting with `make fmt`.

## Make targets

Run `make` (no args) for the full list. Most-used:

| Target | Purpose | Underlying Gradle task |
| --- | --- | --- |
| `make fmt` | Auto-format Kotlin sources | `ktfmtFormat` |
| `make format-check` | Verify formatting | `ktfmtCheck` |
| `make lint` | Detekt + Android Lint | `detekt lintDebug` |
| `make test` | JVM unit tests | `testDebugUnitTest` |
| `make build` | Debug APK | `assembleDebug` |
| `make install` | Push debug APK to a connected device | `installDebug` |
| `make run` | Install + launch on a connected device (USB or wireless) | `installDebug` + `adb shell am start` |
| `make devices` | List connected adb devices | `adb devices` |
| `make ci` | Exact pipeline GitHub Actions runs | `ktfmtCheck detekt lintDebug testDebugUnitTest assembleDebug` |
| `make clean` | Wipe build outputs | `clean` |

Gradle still works directly if you prefer it — see [`Makefile`](Makefile) for the mapping.

### Wireless deploy

Pair your phone over Wi-Fi once via **Android Studio → Device Manager → Pair using Wi-Fi**, or via `adb pair <ip>:<pairing-port>`. After that:

```bash
make devices   # confirm the phone shows up as connected
make run       # install + launch
```

If `adb` isn't on your `PATH`, the Makefile falls back to `$ANDROID_HOME/platform-tools/adb` (defaults to the standard macOS Android Studio location). Override on Linux with `ANDROID_HOME=$HOME/Android/Sdk make run`.

## CI

GitHub Actions (`.github/workflows/ci.yml`) runs on every PR and on pushes to `main`. Failed runs upload `build/reports/**` as artifacts. Dependabot opens weekly PRs for Gradle and GitHub Actions updates.

For the full pipeline reference (tool choices, Detekt rules, troubleshooting), see [`docs/ci-cd.md`](docs/ci-cd.md).

## Documentation

- [`docs/ci-cd.md`](docs/ci-cd.md) — CI/CD pipeline reference
