package com.example.warrantly.notifications

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT

object NotificationChannels {
    const val WARRANTY_REMINDERS_CHANNEL_ID = "warranty_reminders"
    const val WARRANTY_REMINDERS_CHANNEL_NAME = "Warranty Reminders"

    fun createChannels(context: Context) {
        val channel = NotificationChannelCompat.Builder(
            WARRANTY_REMINDERS_CHANNEL_ID,
            IMPORTANCE_DEFAULT
        )
            .setName(WARRANTY_REMINDERS_CHANNEL_NAME)
            .setDescription("Reminders about upcoming warranty expirations")
            .build()

        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }
}
