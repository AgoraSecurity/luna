package com.tarmiga.luna

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val notificationHelper = NotificationHelper(context)

        if (Intent.ACTION_BOOT_COMPLETED == action) {
            Log.d("NotificationReceiver", "Boot completed, rescheduling alarms")
            val prefs = context.getSharedPreferences("luna_prefs", Context.MODE_PRIVATE)
            val stateJson = prefs.getString("luna_state", null)
            stateJson?.let {
                notificationHelper.syncNotificationsFromState(it)
            }
        } else {
            Log.d("NotificationReceiver", "Alarm received, showing notification")
            notificationHelper.showNotification(intent)
        }
    }
}
