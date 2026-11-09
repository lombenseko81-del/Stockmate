package com.zut.stockmate

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first

class LowStockWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

        return try {

            // Access the existing Room database
            val database = StockMateDatabase.getDatabase(applicationContext)

            // Retrieve the current inventory
            val inventory = database.inventoryDao().getAllItems().first()

            // Identify items at or below their low-stock limits
            val lowStockItems = inventory.filter {
                it.quantity <= it.lowStockLimit
            }

            // No notification is needed if everything is sufficiently stocked
            if (lowStockItems.isEmpty()) {
                return Result.success()
            }

            // Create the notification channel
            LowStockNotifications.createChannel(applicationContext)

            // Check whether notification permission has been granted
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.success()
            }

            // Build a list of low-stock products
            val itemDetails = lowStockItems.joinToString(separator = "\n") {
                "${it.name}: ${it.quantity} remaining (limit: ${it.lowStockLimit})"
            }

            // Create the notification
            val notification = NotificationCompat.Builder(
                applicationContext,
                LowStockNotifications.CHANNEL_ID
            )
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("StockMate: Low Stock Alert")
                .setContentText("${lowStockItems.size} item(s) need restocking.")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(itemDetails)
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            // Display the notification
            NotificationManagerCompat
                .from(applicationContext)
                .notify(LowStockNotifications.NOTIFICATION_ID, notification)

            Result.success()

        } catch (exception: Exception) {
            Result.retry()
        }
    }
}

object LowStockNotifications {

    const val CHANNEL_ID = "low_stock_channel"
    const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Low Stock Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when inventory reaches its low-stock limit"
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }
}
