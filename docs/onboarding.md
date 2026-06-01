# Onboarding and Permissions

This document describes the onboarding flow in Luna and how notification permissions are handled.

## Onboarding Flow

The onboarding consists of two main steps:
1. **Date Selection**: The user selects the start date of their last period. This is required to initialize the cycle tracking.
2. **Permission Request**: The user is asked to enable notifications to stay informed about their phase changes.

## Permission Handling

### Contextual Request
Previously, Luna requested notification permission as soon as the app started in `MainActivity`. To improve user experience, this request has been moved to the second step of the onboarding process, where the context for why notifications are needed is clearly explained.

### Status Check
The `PermissionStep` in `OnboardingScreen` checks the current permission status using `ContextCompat.checkSelfPermission`. 

### UI States
The UI adapts based on the permission status:

| Permission Status | Status Text | Buttons Visible |
|---|---|---|
| **Granted** | Status: Done | Finish |
| **Pending/Denied** | Status: Permission pending | Enable Notifications, Maybe Later |

- **Enable Notifications**: Launches the system permission dialog (on Android 13+).
- **Maybe Later**: Continues to the app without granting permission.
- **Finish**: Completes onboarding and takes the user to the main WebView screen.

## Automated Testing

UI states are verified by `OnboardingPermissionTest.kt` using Compose Test Rule.

- `testPermissionGrantedUI`: Verifies that when permission is granted, the "Done" status and "Finish" button are shown, and other buttons are hidden.
- `testPermissionPendingUI`: Verifies that when permission is pending, the "Permission pending" status and request buttons are shown.
