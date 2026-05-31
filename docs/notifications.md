# Notifications

Luna provides proactive notifications to help you track your cycle and understand your energy levels.

## Notification Types and Schedule

Notifications are sent based on a fixed 31-day calendar starting from Day 1 (the day you log your period).

| Day | Type | Time | Notification (Title/Body) |
|-----|------|------|---------------------------|
| 2 | Tip | 9:00 AM | Your body is working hard right now. / Rest is part of the process. |
| 4 | Coming up | 10:00 AM | Follicular phase in 2 days. / Your energy is about to pick up. Good time to plan ahead. |
| 6 | Phase change | 11:00 AM | Follicular phase starts today. / Your mind is sharper this phase. Good time to learn something new. |
| 8 | Tip | 9:00 AM | Social energy is returning. / A good time to reach out to people you've been meaning to see. |
| 10 | Tip | 9:00 AM | Your body recovers faster now. / A good time to push a little harder in workouts. |
| 12 | Coming up | 10:00 AM | Ovulatory phase in 2 days. / Peak energy incoming. Make the most of it. |
| 14 | Phase change | 11:00 AM | Ovulatory phase starts today. / Your confidence tends to peak this phase. Trust it. |
| 16 | Tip | 9:00 AM | High energy is great. / But don't forget to rest too. |
| 17 | Coming up | 10:00 AM | Luteal phase in 2 days. / Things may feel heavier soon. Be gentle with yourself. |
| 19 | Phase change | 11:00 AM | Luteal phase starts today. / Cravings are normal right now. Your body needs more energy this phase. |
| 21 | Tip | 9:00 AM | Wind down your to-do list. / Your energy is better spent finishing than starting right now. |
| 23 | Tip | 9:00 AM | Slower workouts feel better this phase. / Listen to your body. |
| 25 | Tip | 9:00 AM | Magnesium-rich foods can help with PMS symptoms. / Think dark chocolate, nuts, or leafy greens. |
| 27 | Coming up | 10:00 AM | Your period is coming in 2 days. / Stock up on what you need and take it easy. |
| 31 | Late Period | 8:00 AM | Your period is a few days late / Cycles can vary. If it doesn't arrive soon, it may be worth checking in. |

## Technical Implementation

- **Trigger**: Notifications are re-calculated and re-scheduled every time the user logs "My period starts today" in the app.
- **Request Codes (Type-Specific)**:
    - `1000 + day`: Phase Warnings (Coming up)
    - `2000 + day`: Phase Starts (Phase change)
    - `3000`: Late Period Warning
    - `4000 + day`: Tips
- **Granular Cancellation**: Alarms are cleared by type when rescheduling:
    - `cancelTips()`: Clears codes `4001-4040`
    - `cancelWarnings()`: Clears codes `1001-1040`
    - `cancelPhaseStarts()`: Clears codes `2001-2040`
    - `cancelLatePeriod()`: Clears code `3000`
- **Late Period**: The Day 31 notification is automatically canceled if a user logs a new period before that day.
- **Persistence**: Restores alarms after reboot via `BOOT_COMPLETED`.
- **Action Button**: Every notification includes a "Log today" button.

## Testing

To verify notifications manually via ADB:
1.  **Grant Permission**: Ensure notification permission is granted.
2.  **Trigger Specific Day Tip**:
    `adb shell am broadcast -a com.tarmiga.luna.TEST_NOTIF --es notification_type TIP --ei notification_day 2 --es phase_type MENSTRUAL --ei tip_index 0 -p com.tarmiga.luna`
3.  **Trigger Phase Warning**:
    `adb shell am broadcast -a com.tarmiga.luna.TEST_NOTIF --es notification_type PHASE_WARNING --ei notification_day 4 --es phase_type MENSTRUAL -p com.tarmiga.luna`
4.  **Trigger Late Period**:
    `adb shell am broadcast -a com.tarmiga.luna.TEST_NOTIF --es notification_type PERIOD_LATE --ei notification_day 31 -p com.tarmiga.luna`
