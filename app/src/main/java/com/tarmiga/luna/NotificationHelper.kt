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
            val requestCode = ID_DAILY_TIP + (phase.ordinal * 5) + index
            schedulePhaseAlarm(date, NotificationType.DAILY_REMINDER, phase, index, requestCode)
        }
    }

    private fun schedulePhaseAlarm(
        date: LocalDate, 
        type: NotificationType, 
        phase: PhaseType?, 
        tipIndex: Int = -1,
        forcedRequestCode: Int = -1
    ) {
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

        // Use consistent request code
        val requestCode = if (forcedRequestCode != -1) {
            forcedRequestCode
        } else {
            when (type) {
                NotificationType.PHASE_WARNING -> ID_PHASE_WARNING + (phase?.ordinal ?: 0)
                NotificationType.PHASE_START -> ID_PHASE_START + (phase?.ordinal ?: 0)
                NotificationType.PERIOD_LATE -> ID_PERIOD_LATE
                else -> type.hashCode() + (phase?.hashCode() ?: 0) + date.hashCode()
            }
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
        // Clean phase warnings (1000-1003)
        PhaseType.entries.forEach { cancelAlarm(ID_PHASE_WARNING + it.ordinal) }
        // Clean phase starts (2000-2003)
        PhaseType.entries.forEach { cancelAlarm(ID_PHASE_START + it.ordinal) }
        // Clean late period (3000)
        cancelAlarm(ID_PERIOD_LATE)
        // Clean tips (4000-4019)
        for (i in 0 until 20) {
            cancelAlarm(ID_DAILY_TIP + i)
        }
    }

    fun showNotification(intent: Intent) {
        val typeStr = intent.getStringExtra(EXTRA_TYPE)
        val type = try {
            typeStr?.let { NotificationType.valueOf(it) }
        } catch (e: IllegalArgumentException) {
            Log.e("NotificationHelper", "Invalid notification type: $typeStr", e)
            null
        }

        val phaseStr = intent.getStringExtra(EXTRA_PHASE)
        val phase = try {
            phaseStr?.let { PhaseType.valueOf(it) }
        } catch (e: IllegalArgumentException) {
            Log.e("NotificationHelper", "Invalid phase type: $phaseStr", e)
            null
        }

        if (type != null) {
            val tipIndex = intent.getIntExtra(EXTRA_TIP_INDEX, 0)
            val content = NotificationContent.get(type, phase, tipIndex)
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
}
