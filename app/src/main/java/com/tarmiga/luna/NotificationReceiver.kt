package com.tarmiga.luna

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("NotificationReceiver", "onReceive: action=$action")
        
        // Log all extras for debugging
        intent.extras?.let { extras ->
            extras.keySet().forEach { key ->
                @Suppress("DEPRECATION")
                val value = extras.get(key)
                Log.d("NotificationReceiver", "Extra: $key = $value")
            }
        }

        val notificationHelper = NotificationHelper(context)

        if (Intent.ACTION_BOOT_COMPLETED == action) {
            Log.d("NotificationReceiver", "Boot completed, rescheduling alarms")
            val prefs = context.getSharedPreferences("luna_prefs", Context.MODE_PRIVATE)
            val stateJson = prefs.getString("luna_state", null)
            stateJson?.let {
                notificationHelper.syncNotificationsFromState(it)
            }
        } else {
            // This handles both scheduled alarms (which have no action set) 
            // and manual adb broadcasts for testing.
            Log.d("NotificationReceiver", "Notification intent received: $action")
            notificationHelper.showNotification(intent)
        }
    }
}
