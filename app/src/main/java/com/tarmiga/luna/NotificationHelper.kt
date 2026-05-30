package com.tarmiga.luna

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Calendar

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "luna_notifications"
        const val CHANNEL_NAME = "Luna Notifications"
        
        const val EXTRA_TYPE = "notification_type"
        const val EXTRA_PHASE = "phase_type"
        const val EXTRA_TIP_INDEX = "tip_index"
        
        const val DAILY_REMINDER_ID = 5000
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Luna phase and reminder notifications"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun syncNotificationsFromState(json: String) {
        try {
            val stateObj = JSONObject(json)
            val cycleStartsArr = stateObj.getJSONArray("cycleStarts")
            val starts = mutableListOf<String>()
            for (i in 0 until cycleStartsArr.length()) {
                starts.add(cycleStartsArr.getString(i))
            }
            val avgCycleLength = stateObj.optInt("avgCycleLength", 28)
            val state = CycleState(starts, avgCycleLength)
            
            scheduleAlarmsForState(state)
        } catch (e: JSONException) {
            Log.e("NotificationHelper", "Error parsing state for notifications", e)
        }
    }

    private fun scheduleAlarmsForState(state: CycleState) {
        val latestStart = state.getLatestStart() ?: return
        val len = state.avgCycleLength

        cancelAllAlarms()

        // Phase Warnings (2 days before)
        schedulePhaseAlarm(latestStart.plusDays(4 - 2), NotificationType.PHASE_WARNING, PhaseType.FOLLICULAR)
        schedulePhaseAlarm(latestStart.plusDays(12 - 2), NotificationType.PHASE_WARNING, PhaseType.OVULATORY)
        schedulePhaseAlarm(latestStart.plusDays((len - 14).toLong() - 1), NotificationType.PHASE_WARNING, PhaseType.LUTEAL)
        schedulePhaseAlarm(latestStart.plusDays(len.toLong() - 1), NotificationType.PHASE_WARNING, PhaseType.MENSTRUAL)

        // Phase Starts (Day of)
        schedulePhaseAlarm(latestStart.plusDays(6 - 1), NotificationType.PHASE_START, PhaseType.FOLLICULAR)
        schedulePhaseAlarm(latestStart.plusDays(14 - 1), NotificationType.PHASE_START, PhaseType.OVULATORY)
        schedulePhaseAlarm(latestStart.plusDays((len - 13).toLong() - 1), NotificationType.PHASE_START, PhaseType.LUTEAL)

        // Period Late (3 days after expected start)
        schedulePhaseAlarm(latestStart.plusDays(len.toLong() + 3), NotificationType.PERIOD_LATE, null)

        // Random Daily Tips (5 per phase)
        scheduleRandomTipsForPhase(latestStart, 1, 5, PhaseType.MENSTRUAL)
        scheduleRandomTipsForPhase(latestStart, 6, 13, PhaseType.FOLLICULAR)
        scheduleRandomTipsForPhase(latestStart, 14, (len - 14), PhaseType.OVULATORY)
        scheduleRandomTipsForPhase(latestStart, (len - 13), len, PhaseType.LUTEAL)
    }

    private fun scheduleRandomTipsForPhase(latestStart: LocalDate, startDay: Int, endDay: Int, phase: PhaseType) {
        val duration = endDay - startDay + 1
        if (duration <= 0) return
        
        val numTips = 5
        val days = (0 until duration).shuffled().take(numTips)
        
        days.forEachIndexed { index, dayOffset ->
            val date = latestStart.plusDays((startDay + dayOffset - 1).toLong())
            schedulePhaseAlarm(date, NotificationType.DAILY_REMINDER, phase, index)
        }
    }

    private fun schedulePhaseAlarm(date: LocalDate, type: NotificationType, phase: PhaseType?, tipIndex: Int = -1) {
        val now = LocalDate.now()
        if (date.isBefore(now)) return

        // Set alarm for 10:00 AM on the target date (or 9:00 AM for daily reminder)
        val hour = if (type == NotificationType.DAILY_REMINDER) 9 else 10
        val triggerTime = date.atTime(hour, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_TYPE, type.name)
            phase?.let { putExtra(EXTRA_PHASE, it.name) }
            if (tipIndex != -1) putExtra(EXTRA_TIP_INDEX, tipIndex)
        }

        // Unique request code based on type, phase, tipIndex and date
        val requestCode = type.hashCode() + (phase?.hashCode() ?: 0) + tipIndex + date.hashCode()
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }

    fun scheduleDailyReminder() {
        // No longer using fixed daily repeating alarm. 
        // Tips are randomized and scheduled per-cycle in scheduleAlarmsForState.
    }

    private fun cancelAllAlarms() {
        // In a real app, we might track request codes, but for now we'll rely on FLAG_UPDATE_CURRENT
        // and unique identifiers to manage them. 
        // Actual cancellation logic would require keeping track of all scheduled request codes.
    }

    fun showNotification(intent: Intent) {
        val typeStr = intent.getStringExtra(EXTRA_TYPE)
        Log.d("NotificationHelper", "showNotification: typeStr=$typeStr")
        
        val type = try {
            typeStr?.let { NotificationType.valueOf(it) }
        } catch (e: IllegalArgumentException) {
            Log.e("NotificationHelper", "Invalid notification type: $typeStr", e)
            null
        }

        val phaseStr = intent.getStringExtra(EXTRA_PHASE)
        Log.d("NotificationHelper", "showNotification: phaseStr=$phaseStr")
        
        val phase = try {
            phaseStr?.let { PhaseType.valueOf(it) }
        } catch (e: IllegalArgumentException) {
            Log.e("NotificationHelper", "Invalid phase type: $phaseStr", e)
            null
        }

        if (type != null) {
            val tipIndex = intent.getIntExtra(EXTRA_TIP_INDEX, 0)
            val content = getNotificationContent(type, phase, tipIndex)
            Log.d("NotificationHelper", "Content found: ${content != null}")

            if (content != null) {
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(content.title)
                    .setContentText(content.text)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .addAction(0, "Log today", pendingIntent)

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notificationId = type.hashCode() + (phase?.hashCode() ?: 0)
                notificationManager.notify(notificationId, builder.build())
            }
        }
    }

    private data class Content(val title: String, val text: String)

    private fun getNotificationContent(type: NotificationType, phase: PhaseType?, tipIndex: Int): Content? {
        return when (type) {
            NotificationType.PHASE_WARNING -> when (phase) {
                PhaseType.MENSTRUAL -> Content("Luna", "Your period is coming in 2 days. Stock up on what you need and take it easy.")
                PhaseType.FOLLICULAR -> Content("Luna", "Follicular phase in 2 days. Your energy is about to pick up. Good time to plan ahead.")
                PhaseType.OVULATORY -> Content("Luna", "Ovulatory phase in 2 days. Peak energy incoming. Make the most of it.")
                PhaseType.LUTEAL -> Content("Luna", "Luteal phase in 2 days. Your energy may start to dip. Wind down your schedule.")
                null -> null
            }
            NotificationType.PHASE_START -> when (phase) {
                PhaseType.FOLLICULAR -> Content("Follicular phase starts today", "Your energy is returning. A good day to start something new.")
                PhaseType.OVULATORY -> Content("Ovulatory phase starts today", "You're at your peak. Great day for big conversations or challenges.")
                PhaseType.LUTEAL -> Content("Luteal phase starts today", "Things may feel heavier soon. Be gentle with yourself.")
                else -> null
            }
            NotificationType.PERIOD_LATE -> Content("Your period is a few days late", "Cycles can vary. If it doesn't arrive soon, it may be worth checking in.")
            NotificationType.DAILY_REMINDER -> getDailyTip(phase, tipIndex)
        }
    }

    private fun getDailyTip(phase: PhaseType?, index: Int): Content? {
        val idx = index.coerceIn(0, 4)
        return when (phase) {
            PhaseType.MENSTRUAL -> listOf(
                Content("Your body is working hard right now.", "Rest is part of the process."),
                Content("Heat can help with cramps.", "A warm compress or bath can make a difference."),
                Content("Low energy is normal right now.", "It's not a sign of weakness."),
                Content("Iron-rich foods can help during your period.", "Think lentils, spinach, or red meat."),
                Content("Gentle movement can ease cramps.", "A short walk or some stretching can help.")
            )[idx]
            PhaseType.FOLLICULAR -> listOf(
                Content("Energy is picking up.", "A good time to tackle things you've been putting off."),
                Content("Your mind is sharper this phase.", "Good time to learn something new."),
                Content("Social energy is returning.", "A good time to reach out to people you've been meaning to see."),
                Content("Your body recovers faster now.", "A good time to push a little harder in workouts."),
                Content("You may notice your mood lifting.", "That's your body finding its rhythm again.")
            )[idx]
            PhaseType.OVULATORY -> listOf(
                Content("You're at your most energetic right now.", "Good time to take on something you've been avoiding."),
                Content("This is a great time for difficult conversations.", "You're at your most articulate."),
                Content("Your confidence tends to peak this phase.", "Trust it."),
                Content("High energy is great.", "But don't forget to rest too."),
                Content("Your skin tends to look its best right now.", "Enjoy it.")
            )[idx]
            PhaseType.LUTEAL -> listOf(
                Content("Cravings are normal right now.", "Your body needs more energy this phase."),
                Content("If you're feeling irritable, it's hormonal.", "Be patient with yourself."),
                Content("Slower workouts feel better this phase.", "Listen to your body."),
                Content("Wind down your to-do list.", "Your energy is better spent finishing than starting right now."),
                Content("Magnesium-rich foods can help with PMS symptoms.", "Think dark chocolate, nuts, or leafy greens.")
            )[idx]
            null -> Content("Luna", "Time to log your day. How are you feeling?")
        }
    }
}
