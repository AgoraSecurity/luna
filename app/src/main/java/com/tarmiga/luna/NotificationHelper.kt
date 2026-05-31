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
        const val EXTRA_INDEX = "tip_index"
        const val EXTRA_DAY = "notification_day"

        // Base IDs for RequestCodes
        private const val ID_PHASE_WARNING = 1000
        private const val ID_PHASE_START = 2000
        private const val ID_PERIOD_LATE = 3000
        private const val ID_DAILY_TIP = 4000
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

        cancelAllAlarms()

        // Day 2: Menstrual, Tip (9 AM)
        scheduleCalendarAlarm(latestStart, 2, NotificationType.TIP, PhaseType.MENSTRUAL, 0)
        // Day 4: Menstrual, Coming up (10 AM)
        scheduleCalendarAlarm(latestStart, 4, NotificationType.PHASE_WARNING, PhaseType.MENSTRUAL)
        // Day 6: Follicular, Phase change (11 AM)
        scheduleCalendarAlarm(latestStart, 6, NotificationType.PHASE_START, PhaseType.FOLLICULAR)
        // Day 8: Follicular, Tip (9 AM)
        scheduleCalendarAlarm(latestStart, 8, NotificationType.TIP, PhaseType.FOLLICULAR, 0)
        // Day 10: Follicular, Tip (9 AM)
        scheduleCalendarAlarm(latestStart, 10, NotificationType.TIP, PhaseType.FOLLICULAR, 1)
        // Day 12: Follicular, Coming up (10 AM)
        scheduleCalendarAlarm(latestStart, 12, NotificationType.PHASE_WARNING, PhaseType.FOLLICULAR)
        // Day 14: Ovulatory, Phase change (11 AM)
        scheduleCalendarAlarm(latestStart, 14, NotificationType.PHASE_START, PhaseType.OVULATORY)
        // Day 16: Ovulatory, Tip (9 AM)
        scheduleCalendarAlarm(latestStart, 16, NotificationType.TIP, PhaseType.OVULATORY, 0)
        // Day 17: Ovulatory, Coming up (10 AM)
        scheduleCalendarAlarm(latestStart, 17, NotificationType.PHASE_WARNING, PhaseType.OVULATORY)
        // Day 19: Luteal, Phase change (11 AM)
        scheduleCalendarAlarm(latestStart, 19, NotificationType.PHASE_START, PhaseType.LUTEAL)
        // Day 21: Luteal, Tip (9 AM)
        scheduleCalendarAlarm(latestStart, 21, NotificationType.TIP, PhaseType.LUTEAL, 0)
        // Day 23: Luteal, Tip (9 AM)
        scheduleCalendarAlarm(latestStart, 23, NotificationType.TIP, PhaseType.LUTEAL, 1)
        // Day 25: Luteal, Tip (9 AM)
        scheduleCalendarAlarm(latestStart, 25, NotificationType.TIP, PhaseType.LUTEAL, 2)
        // Day 27: Luteal, Coming up (10 AM)
        scheduleCalendarAlarm(latestStart, 27, NotificationType.PHASE_WARNING, PhaseType.LUTEAL)
        // Day 31: Late Period (8 AM)
        scheduleCalendarAlarm(latestStart, 31, NotificationType.PERIOD_LATE)
    }

    private fun scheduleCalendarAlarm(
        latestStart: LocalDate,
        day: Int,
        type: NotificationType,
        phase: PhaseType? = null,
        index: Int = 0
    ) {
        val date = latestStart.plusDays((day - 1).toLong())
        val now = LocalDate.now()
        if (date.isBefore(now)) return

        val hour = when (type) {
            NotificationType.TIP -> 9
            NotificationType.PHASE_WARNING -> 10
            NotificationType.PHASE_START -> 11
            NotificationType.PERIOD_LATE -> 8
        }

        val triggerTime = date.atTime(hour, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_TYPE, type.name)
            putExtra(EXTRA_DAY, day)
            phase?.let { putExtra(EXTRA_PHASE, it.name) }
            putExtra(EXTRA_INDEX, index)
        }

        val requestCode = when (type) {
            NotificationType.TIP -> ID_DAILY_TIP + day
            NotificationType.PHASE_WARNING -> ID_PHASE_WARNING + day
            NotificationType.PHASE_START -> ID_PHASE_START + day
            NotificationType.PERIOD_LATE -> ID_PERIOD_LATE
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }

    private fun cancelAlarm(id: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun cancelAllAlarms() {
        for (day in 1..40) {
            cancelAlarm(ID_DAILY_TIP + day)
            cancelAlarm(ID_PHASE_WARNING + day)
            cancelAlarm(ID_PHASE_START + day)
        }
        cancelAlarm(ID_PERIOD_LATE)
    }

    fun showNotification(intent: Intent) {
        val typeStr = intent.getStringExtra(EXTRA_TYPE)
        val type = try {
            typeStr?.let { NotificationType.valueOf(it) }
        } catch (e: IllegalArgumentException) {
            Log.e("NotificationHelper", "Invalid notification type: $typeStr", e)
            null
        }

        val day = intent.getIntExtra(EXTRA_DAY, -1)
        val phaseStr = intent.getStringExtra(EXTRA_PHASE)
        val phase = phaseStr?.let {
            try {
                PhaseType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                Log.e("NotificationHelper", "Invalid phase type: $it", e)
                null
            }
        }
        val index = intent.getIntExtra(EXTRA_INDEX, 0)

        if (type != null && day != -1) {
            val content = NotificationContent.get(type, phase, index)
            if (content != null) {
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(content.title)
                    .setContentText(content.text)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .addAction(0, "Log today", pendingIntent)

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notificationId = type.hashCode() + day
                notificationManager.notify(notificationId, builder.build())
            }
        }
    }
}
