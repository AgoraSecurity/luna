package com.tarmiga.luna

import android.app.NotificationManager
import android.content.Context
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
        // This won't throw NPE in androidTest because the framework is present
        notificationHelper.showNotification()
        
        // We can't easily verify the notification is actually visible on screen without UI Automator,
        // but this test ensures the code executes without crashing in the Android environment.
    }
    
    @Test
    fun testScheduleNotification() {
        // Ensures the alarm scheduling logic runs without crash
        notificationHelper.scheduleNotification(1000)
    }
}
