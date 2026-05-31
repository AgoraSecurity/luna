package com.tarmiga.luna

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationHelperInstrumentedTest {

    private lateinit var context: Context
    private lateinit var notificationHelper: NotificationHelper

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        notificationHelper = NotificationHelper(context)
    }

    @Test
    fun testCreateNotificationChannel() {
        notificationHelper.createNotificationChannel()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(NotificationHelper.CHANNEL_ID)
            assertTrue("Notification channel should be created", channel != null)
            assertTrue("Channel name should match", channel?.name == NotificationHelper.CHANNEL_NAME)
        }
    }

    @Test
    fun testShowNotification() {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationHelper.EXTRA_TYPE, NotificationType.TIP.name)
            putExtra(NotificationHelper.EXTRA_DAY, 2)
            putExtra(NotificationHelper.EXTRA_PHASE, PhaseType.MENSTRUAL.name)
            putExtra(NotificationHelper.EXTRA_INDEX, 0)
        }
        notificationHelper.showNotification(intent)
        
        // Ensures the code executes without crashing in the Android environment.
    }
    
    @Test
    fun testCancellationMethods() {
        // Just verify these don't crash
        notificationHelper.cancelTips()
        notificationHelper.cancelWarnings()
        notificationHelper.cancelPhaseStarts()
        notificationHelper.cancelLatePeriod()
    }

    @Test
    fun testSyncNotificationsFromState() {
        val json = """
            {
                "cycleStarts": ["2024-01-01"],
                "avgCycleLength": 28
            }
        """.trimIndent()
        notificationHelper.syncNotificationsFromState(json)
        // Ensures scheduling logic runs without crash
    }
}
