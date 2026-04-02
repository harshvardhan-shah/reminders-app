package com.example.intervalreminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun receive(context: Context, intent: Intent) {
        val firingIntent = Intent(context, FiringActivity::class.java).apply {
            putExtras(intent)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(firingIntent)
    }
}