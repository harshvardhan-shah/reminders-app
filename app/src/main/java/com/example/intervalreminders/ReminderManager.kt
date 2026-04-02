package com.example.intervalreminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

object ReminderManager {
    fun scheduleNextReminder(
        context: Context,
        startHour: Int, startMinute: Int,
        endHour: Int, endMinute: Int,
        intervalMinutes: Int,
        vibrationType: String, playMusic: Boolean
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = Calendar.getInstance()
        var nextTime = Calendar.getInstance()
        
        val startCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
            set(Calendar.SECOND, 0)
        }
        
        val endCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, endHour)
            set(Calendar.MINUTE, endMinute)
            set(Calendar.SECOND, 0)
        }
        
        if (now.before(startCal)) {
            nextTime = startCal
        } else if (now.after(endCal)) {
            nextTime = startCal.apply { add(Calendar.DAY_OF_YEAR, 1) }
        } else {
            nextTime.add(Calendar.MINUTE, intervalMinutes)
            if (nextTime.after(endCal)) {
                nextTime = startCal.apply { add(Calendar.DAY_OF_YEAR, 1) }
            }
        }

        // Changed to target FiringActivity directly to bypass Android 10+ background restrictions
        val intent = Intent(context, FiringActivity::class.java).apply {
            putExtra("VIBRATION_TYPE", vibrationType)
            putExtra("PLAY_MUSIC", playMusic)
            putExtra("START_H", startHour)
            putExtra("START_M", startMinute)
            putExtra("END_H", endHour)
            putExtra("END_M", endMinute)
            putExtra("INTERVAL", intervalMinutes)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(nextTime.timeInMillis, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d("ReminderManager", "Scheduled for ${nextTime.time}")
        } catch (e: SecurityException) {
            Log.e("ReminderManager", "Missing EXACT_ALARM permission", e)
        }
    }
}