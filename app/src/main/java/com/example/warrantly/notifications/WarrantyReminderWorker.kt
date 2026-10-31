package com.example.warrantly.notifications

import android.annotation.SuppressLint
import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.warrantly.MainActivity

class WarrantyReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_PRODUCT_ID = "product_id"
        const val KEY_PRODUCT_NAME = "product_name"
        const val KEY_DAYS_BEFORE = "days_before"
        const val WORK_TAG = "warranty_reminder"
    }

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        val productId = inputData.getLong(KEY_PRODUCT_ID, -1L)
        val productName = inputData.getString(KEY_PRODUCT_NAME) ?: "your product"
        val daysBefore = inputData.getInt(KEY_DAYS_BEFORE, 0)

        if (productId == -1L) return Result.failure()

        // Check notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return Result.success() // Can't show notification; silently succeed
        }

        // Build a pending intent that opens MainActivity and passes the productId
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("productId", productId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            productId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationTitle = "Warranty expiring soon"
        val notificationBody =
            "Your $productName warranty is expiring soon. Tap to review details."

        val notification = NotificationCompat.Builder(
            context,
            NotificationChannels.WARRANTY_REMINDERS_CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notificationTitle)
            .setContentText(notificationBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Use productId + daysBefore to create a unique notification ID
        val notificationId = (productId * 1000 + daysBefore).toInt()

        NotificationManagerCompat.from(context).notify(notificationId, notification)

        return Result.success()
    }
}
