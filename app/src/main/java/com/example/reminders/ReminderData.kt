package com.example.reminders

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val hour: Int,
    val minute: Int,
    val daysOfWeek: Set<Int>, // 1=Sun, 2=Mon...7=Sat
    val intervalMinutes: Int, // 0 means no repetition that day
    val isActive: Boolean = true
)

object ReminderRepository {
    private const val PREFS = "reminders_prefs"
    private const val KEY = "reminders_list"
    private val gson = Gson()

    fun getReminders(context: Context): List<Reminder> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Reminder>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveReminder(context: Context, reminder: Reminder) {
        val current = getReminders(context).toMutableList()
        val index = current.indexOfFirst { it.id == reminder.id }
        if (index >= 0) current[index] = reminder else current.add(reminder)
        
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY, gson.toJson(current)).apply()
    }
}