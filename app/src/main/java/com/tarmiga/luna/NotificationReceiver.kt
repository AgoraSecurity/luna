package com.tarmiga.luna

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationReceiver", "Alarm received, showing notification")
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification()
    }
}
