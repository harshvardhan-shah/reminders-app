package com.example.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra("REMINDER_ID") ?: return
        val interval = intent.getIntExtra("INTERVAL", 0)

        // Automatically chain the next interval/day
        val reminders = ReminderRepository.getReminders(context)
        reminders.find { it.id == reminderId }?.let { ReminderManager.scheduleNext(context, it) }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "expressive_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                setBypassDnd(true) // Crucial for Pixel devices
                description = "Full screen alarms"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val firingIntent = Intent(context, FiringActivity::class.java).apply {
            putExtra("REMINDER_ID", reminderId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context, reminderId.hashCode(), firingIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("It's Time!")
            .setContentText("Your scheduled reminder is active.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true) // Force-wakes Android 16
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        notificationManager.notify(reminderId.hashCode(), notification)
    }
}