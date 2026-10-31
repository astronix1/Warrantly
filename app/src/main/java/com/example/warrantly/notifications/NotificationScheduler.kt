package com.example.warrantly.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.warrantly.domain.model.Product
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    /**
     * Schedule reminder notifications for a product.
     * Any existing reminders for this product are cancelled before scheduling new ones.
     */
    fun scheduleReminders(product: Product, daysBefore: List<Int>) {
        // Cancel all existing reminders for this product first
        cancelReminders(product.id)

        val warrantyEndDate = product.warrantyEndDate ?: return
        val nowMs = System.currentTimeMillis()

        val workManager = WorkManager.getInstance(context)

        for (days in daysBefore) {
            // Trigger date = warrantyEndDate - daysBefore days (in milliseconds)
            val triggerMs = warrantyEndDate - days.toLong() * 24L * 60L * 60L * 1000L
            val delayMs = triggerMs - nowMs

            // Skip if the trigger date is already in the past
            if (delayMs <= 0) continue

            val inputData = Data.Builder()
                .putLong(WarrantyReminderWorker.KEY_PRODUCT_ID, product.id)
                .putString(WarrantyReminderWorker.KEY_PRODUCT_NAME, product.name)
                .putInt(WarrantyReminderWorker.KEY_DAYS_BEFORE, days)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<WarrantyReminderWorker>()
                .setInputData(inputData)
                .addTag(WarrantyReminderWorker.WORK_TAG)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .build()

            workManager.enqueueUniqueWork(
                workName(product.id, days),
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    /**
     * Cancel all reminders for a given product across all daysBefore values.
     * We cancel by tag prefix using the product-level unique work names.
     */
    fun cancelReminders(productId: Long) {
        val workManager = WorkManager.getInstance(context)
        // Cancel known reminder intervals. We use the same convention as workName().
        // Since WorkManager doesn't support wildcard cancellation by prefix, we cancel
        // common day values plus rely on REPLACE policy when rescheduling.
        val knownDays = listOf(1, 3, 7, 14, 30, 60, 90)
        for (days in knownDays) {
            workManager.cancelUniqueWork(workName(productId, days))
        }
    }

    /** Cancel every warranty reminder work across all products. */
    fun cancelAll() {
        WorkManager.getInstance(context).cancelAllWorkByTag(WarrantyReminderWorker.WORK_TAG)
    }

    private fun workName(productId: Long, daysBefore: Int): String =
        "warranty_reminder_${productId}_${daysBefore}"
}
