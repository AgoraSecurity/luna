# Release Process

This document outlines the steps to build, sign, upload, and tag a new release of the Luna app.

## Steps

### 0. Update the version
- Update `versionCode` and `versionName` in `app/build.gradle.kts`.

### 1. Build and Sign
- Open the project in Android Studio.
- Go to **Build > Generate Signed Bundle / APK...**
- Select **Android App Bundle** and click **Next**.
- Select the `app` module.
- Provide your keystore information.
- Select the `release` build variant.
- Click **Finish**. Android Studio will generate the `.aab` file.

### 2. Upload to Play Console
- Log in to the [Google Play Console](https://play.google.com/apps/publish).
- Select the Luna app.
- Navigate to **Release > Production** (or the desired track).
- Create a new release and upload the `.aab` file generated in step 1.

### 3. Verify
- Once the upload is complete, check for any errors or warnings reported by the Play Console.
- If there are errors:
    - Fix the issues in the code.
    - Repeat from **Step 1**.

### 4. Tag the Release
Once the release is successfully uploaded and verified:
- Ensure you are on the `main` branch and have no uncommitted changes.
- Run the release script to create the git tag:
  ```bash
  ./scripts/release.sh
  ```
- This script will:
    - Pull the latest changes.
    - Extract the version from `app/build.gradle.kts`.
    - Create a git tag (e.g., `v1.0`).
    - Push the tag to the remote repository.

## Pre-requisites
- Ensure `scripts/release.sh` is executable: `chmod +x scripts/release.sh`.
