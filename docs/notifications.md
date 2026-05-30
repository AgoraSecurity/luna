# Notifications

Luna provides proactive notifications to help you track your cycle and understand your energy levels.

## Notification Types

### 1. Phase Warnings (2 days before)
Sent at 10:00 AM, two days before a new phase is expected to start.
- **Menstrual**: "Your period is coming in 2 days. Stock up on what you need and take it easy."
- **Follicular**: "Follicular phase in 2 days. Your energy is about to pick up. Good time to plan ahead."
- **Ovulatory**: "Ovulatory phase in 2 days. Peak energy incoming. Make the most of it."
- **Luteal**: "Luteal phase in 2 days. Your energy may start to dip. Wind down your schedule."

### 2. Phase Starts (Day of)
Sent at 10:00 AM on the day a phase begins.
- **Follicular**: "Follicular phase starts today. Your energy is returning. A good day to start something new."
- **Ovulatory**: "Ovulatory phase starts today. You're at your peak. Great day for big conversations or challenges."
- **Luteal**: "Luteal phase starts today. Things may feel heavier soon. Be gentle with yourself."
- *Note: Menstrual phase start is not notified as it is user-triggered.*

### 3. Late Period Warning
Sent 3 days after your period was expected to start if it hasn't been logged yet.
- **Title**: Your period is a few days late
- **Text**: Cycles can vary. If it doesn't arrive soon, it may be worth checking in.

### 4. Daily Tip + Log Reminder
Sent at 9:00 AM on 5 random days within each phase. Includes a "Log today" button.

#### Menstrual Phase Tips:
- **Your body is working hard right now.** / Rest is part of the process.
- **Heat can help with cramps.** / A warm compress or bath can make a difference.
- **Low energy is normal right now.** / It's not a sign of weakness.
- **Iron-rich foods can help during your period.** / Think lentils, spinach, or red meat.
- **Gentle movement can ease cramps.** / A short walk or some stretching can help.

#### Follicular Phase Tips:
- **Energy is picking up.** / A good time to tackle things you've been putting off.
- **Your mind is sharper this phase.** / Good time to learn something new.
- **Social energy is returning.** / A good time to reach out to people you've been meaning to see.
- **Your body recovers faster now.** / A good time to push a little harder in workouts.
- **You may notice your mood lifting.** / That's your body finding its rhythm again.

#### Ovulatory Phase Tips:
- **You're at your most energetic right now.** / Good time to take on something you've been avoiding.
- **This is a great time for difficult conversations.** / You're at your most articulate.
- **Your confidence tends to peak this phase.** / Trust it.
- **High energy is great.** / But don't forget to rest too.
- **Your skin tends to look its best right now.** / Enjoy it.

#### Luteal Phase Tips:
- **Cravings are normal right now.** / Your body needs more energy this phase.
- **If you're feeling irritable, it's hormonal.** / Be patient with yourself.
- **Slower workouts feel better this phase.** / Listen to your body.
- **Wind down your to-do list.** / Your energy is better spent finishing than starting right now.
- **Magnesium-rich foods can help with PMS symptoms.** / Think dark chocolate, nuts, or leafy greens.

## Technical Implementation

- **Trigger**: Notifications are re-calculated and re-scheduled every time the user logs "My period starts today" in the app.
- **Randomization**: For each phase, 5 unique days are selected randomly to show tips.
- **Scheduling**: Uses `AlarmManager` with `RTC_WAKEUP`.
- **Persistence**: Restores alarms after reboot via `BOOT_COMPLETED`.
- **Action Button**: Every notification includes a "Log today" button.
