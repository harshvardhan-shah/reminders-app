package com.example.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

object ReminderManager {
    fun scheduleNext(context: Context, reminder: Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Cancel existing intent first to prevent ghost alarms
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminder.id)
            putExtra("INTERVAL", reminder.intervalMinutes)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, reminder.id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        if (!reminder.isActive) return

        val now = Calendar.getInstance()
        val nextTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Logic for next exact occurrence
        if (now.after(nextTime)) {
            if (reminder.intervalMinutes > 0) {
                // Advance by intervals until we reach the future
                while (nextTime.before(now)) {
                    nextTime.add(Calendar.MINUTE, reminder.intervalMinutes)
                }
            } else {
                nextTime.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // Snap to valid day if daysOfWeek is configured
        if (reminder.daysOfWeek.isNotEmpty()) {
            while (!reminder.daysOfWeek.contains(nextTime.get(Calendar.DAY_OF_WEEK))) {
                nextTime.add(Calendar.DAY_OF_YEAR, 1)
                // Reset to base time on new day
                nextTime.set(Calendar.HOUR_OF_DAY, reminder.hour)
                nextTime.set(Calendar.MINUTE, reminder.minute)
            }
        }

        try {
            // setAlarmClock informs the OS (Pixel 9) that this is an official user alarm
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(nextTime.timeInMillis, pendingIntent), pendingIntent)
            Log.d("ReminderManager", "Scheduled ID: ${reminder.id} for ${nextTime.time}")
        } catch (e: SecurityException) {
            Log.e("ReminderManager", "EXACT_ALARM permission restricted by Android OS.")
        }
    }
}