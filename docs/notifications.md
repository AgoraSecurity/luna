# Notifications

This document describes the notification system in Luna.

## Overview

Luna uses standard Android notifications to engage users. For the initial implementation, a "Test notification" is scheduled to fire 1 minute after the app starts.

## Components

### 1. `NotificationHelper`
A utility class that manages:
- **Notification Channels**: Required for Android 8.0 (API 26) and higher.
- **Scheduling**: Uses `AlarmManager` to schedule a broadcast at a specific time.
- **Displaying**: Builds and shows the actual notification using `NotificationCompat`.

### 2. `NotificationReceiver`
A `BroadcastReceiver` that listens for the alarm intent scheduled by `NotificationHelper`. When triggered, it calls `NotificationHelper.showNotification()`.

### 3. `MainActivity`
Handles:
- **Permission Requests**: Requests `POST_NOTIFICATIONS` permission on Android 13+ (API 33+).
- **Initialization**: Creates the notification channel and triggers the initial scheduling on app startup.

## Permissions

The app requires the following permission in `AndroidManifest.xml`:
- `android.permission.POST_NOTIFICATIONS`: Required for Android 13+ to display notifications.

## Testing

1.  Launch the app on an Android device or emulator.
2.  Grant notification permission when prompted.
3.  The app will schedule a notification for 1 minute in the future.
4.  You can close the app (swipe it away from recents).
5.  After ~1 minute, the "Test notification" should appear.

## Implementation Details

- **Alarm Type**: `AlarmManager.RTC_WAKEUP`. This ensures the notification fires even if the device is asleep.
- **Channel ID**: `luna_test_notifications`
- **Notification ID**: `1001`
