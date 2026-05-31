# Luna App Icons & Visual Assets

This document summarizes the current set of icons and visual assets used in the Luna Android application.

## 1. App Launcher Icons
The app uses **Adaptive Icons**, which consist of a foreground and a background layer.

*   **Foreground Layer (`ic_launcher_foreground`)**:
    *   **Resource**: `app/src/main/res/drawable-<density>/ic_launcher_foreground.png`
    *   **Source**: Migrated from custom PNG assets.
    *   **Role**: Displays the Luna logo on the home screen and in the app drawer.
*   **Background Layer (`ic_launcher_background`)**:
    *   **Resource**: `app/src/main/res/drawable/ic_launcher_background.xml`
    *   **Role**: A solid color (`#F5F5F0`) that matches the app's overall theme.

## 2. Notification Icon
*   **Resource**: `app/src/main/res/drawable-<density>/ic_notification.png`
*   **Role**: Small icon displayed in the status bar and notification drawer.
*   **Design**: Specifically sized for notification requirements (24px to 96px depending on density).

## 3. Splash Screen
The splash screen is implemented using the Android 12+ Splash Screen API.

*   **Icon**: Uses the launcher foreground icon.
*   **Background Color**: `#F5F5F0` (matches app background).
*   **Theme Configuration**: Defined in `app/src/main/res/values/themes.xml` under `Theme.App.Starting`.

## 4. In-App UI Elements
Currently, the app's internal UI (WebView-based) primarily uses **Standard Emojis** for:
*   **Cycle Phases**: 🩸 Menstrual, 🌱 Follicular, ☀️ Ovulatory, 🌙 Luteal.
*   **Recommendations**: 🏃 Movement, 🥗 Nutrition, 🧠 Work & Focus, 💤 Rest, 👁 What to notice.
*   **Symptoms**: Various emojis for cramps, bloating, etc.

---

## Technical Maintenance
If you need to update these icons, replace the files in the `app/src/main/res/drawable-<density>/` folders. Android will automatically select the correct size based on the device's screen resolution.
