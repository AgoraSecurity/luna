# Luna — developer task runner.
# Thin wrapper around Gradle so day-to-day commands stay short and discoverable.
# Run `make` (no args) to list available targets.

GRADLE := ./gradlew

# Resolve adb: respect PATH, else fall back to the standard Android SDK location.
# Override ANDROID_HOME on Linux (commonly $HOME/Android/Sdk).
ANDROID_HOME ?= $(HOME)/Library/Android/sdk
ADB ?= $(shell command -v adb 2>/dev/null || echo $(ANDROID_HOME)/platform-tools/adb)

APP_ID := com.tarmiga.luna
MAIN_ACTIVITY := $(APP_ID)/.MainActivity

.DEFAULT_GOAL := help
.PHONY: help setup hooks format fmt format-check lint detekt android-lint test build release install launch run devices ci clean deps tasks

help: ## Show this help
	@awk 'BEGIN {FS = ":.*?## "; printf "\nUsage: make <target>\n\nTargets:\n"} \
		/^[a-zA-Z_-]+:.*?## / {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

# ── Setup ──────────────────────────────────────────────────────────────────

setup: hooks ## Initial project setup (installs git hooks)

hooks: ## Install the pre-commit hook
	$(GRADLE) installGitHooks

# ── Formatting ────────────────────────────────────────────────────────────

format: ## Auto-format Kotlin sources (ktfmt, Google style)
	$(GRADLE) ktfmtFormat

fmt: format ## Alias for `format`

format-check: ## Verify Kotlin formatting without modifying files
	$(GRADLE) ktfmtCheck

# ── Static analysis ───────────────────────────────────────────────────────

lint: detekt android-lint ## Run all static analyzers (detekt + Android Lint)

detekt: ## Detekt — Kotlin code smells
	$(GRADLE) detekt

android-lint: ## Android Lint (debug variant)
	$(GRADLE) lintDebug

# ── Build & test ──────────────────────────────────────────────────────────

test: ## Run JVM unit tests
	$(GRADLE) testDebugUnitTest

build: ## Build the debug APK
	$(GRADLE) assembleDebug

release: ## Build the release APK
	$(GRADLE) assembleRelease

install: ## Install the debug APK on a connected device / emulator
	$(GRADLE) installDebug

# ── Device & deploy ───────────────────────────────────────────────────────

launch: ## Launch the app on the connected device
	$(ADB) shell am start -n $(MAIN_ACTIVITY)

run: install launch ## Install + launch (works over USB or wireless adb)

devices: ## List connected adb devices (USB + wireless)
	$(ADB) devices

# ── Pipelines ─────────────────────────────────────────────────────────────

ci: ## Run the exact pipeline GitHub Actions runs
	$(GRADLE) ktfmtCheck detekt lintDebug testDebugUnitTest assembleDebug

# ── Housekeeping ──────────────────────────────────────────────────────────

clean: ## Delete build outputs
	$(GRADLE) clean

deps: ## Print the :app dependency tree
	$(GRADLE) :app:dependencies

tasks: ## List all Gradle tasks
	$(GRADLE) tasks
