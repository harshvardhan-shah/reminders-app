package com.example.reminders

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndroid16Permissions()
        setContent { AppTheme { ExpressiveDashboard() } }
    }

    private fun checkAndroid16Permissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissions.isNotEmpty()) requestPermissionLauncher.launch(permissions.toTypedArray())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveDashboard() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showSheet by remember { mutableStateOf(false) }
    var reminders by remember { mutableStateOf(ReminderRepository.getReminders(context)) }

    fun checkAlarmPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!am.canScheduleExactAlarms()) {
                context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                return false
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!nm.canUseFullScreenIntent()) {
                context.startActivity(Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                    data = Uri.parse("package:${context.packageName}")
                })
                return false
            }
        }
        return true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (checkAlarmPermissions()) showSheet = true 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(bottom = 90.dp, end = 16.dp).size(64.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (reminders.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Alarm, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No reminders", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(reminders, key = { it.id }) { reminder ->
                        ExpressiveCard(reminder, onToggle = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val updated = reminder.copy(isActive = !reminder.isActive)
                            ReminderRepository.saveReminder(context, updated)
                            reminders = ReminderRepository.getReminders(context)
                            if (updated.isActive) ReminderManager.scheduleNext(context, updated)
                        })
                    }
                }
            }

            // M3 Glass Dock
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
                    .blur(24.dp) // Android 12+ hardware blur
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Alarm, contentDescription = "Reminders", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reminders", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showSheet) {
            AddReminderSheet(
                onDismiss = { showSheet = false },
                onSave = { h, m, days, interval ->
                    val newReminder = Reminder(hour = h, minute = m, daysOfWeek = days, intervalMinutes = interval)
                    ReminderRepository.saveReminder(context, newReminder)
                    ReminderManager.scheduleNext(context, newReminder)
                    reminders = ReminderRepository.getReminders(context)
                    showSheet = false
                }
            )
        }
    }
}

@Composable
fun ExpressiveCard(reminder: Reminder, onToggle: () -> Unit) {
    // Hardware accelerated physics
    val scale by animateFloatAsState(
        targetValue = if (reminder.isActive) 1f else 0.96f, 
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = ""
    )
    val bgColor by animateColorAsState(if (reminder.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, label = "")
    val textColor by animateColorAsState(if (reminder.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant, label = "")
    
    val amPm = if (reminder.hour >= 12) "PM" else "AM"
    val displayHour = if (reminder.hour % 12 == 0) 12 else reminder.hour % 12
    val minStr = reminder.minute.toString().padStart(2, '0')

    Card(
        modifier = Modifier
            .aspectRatio(0.70f) // Tall aspect ratio matches M3 Feeding Times standard
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable { onToggle() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 8.dp)) {
                Text(displayHour.toString().padStart(2, '0'), fontSize = 56.sp, fontWeight = if (reminder.isActive) FontWeight.Black else FontWeight.Thin, color = textColor, lineHeight = 56.sp, letterSpacing = (-2).sp)
                Column(modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)) {
                    Text(minStr, fontSize = 24.sp, fontWeight = FontWeight.Light, color = textColor)
                    Text(amPm, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                }
            }
            Switch(checked = reminder.isActive, onCheckedChange = { onToggle() }, modifier = Modifier.align(Alignment.End))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderSheet(onDismiss: () -> Unit, onSave: (Int, Int, Set<Int>, Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    var state = rememberTimePickerState(initialHour = 9, initialMinute = 0)
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
    var interval by remember { mutableStateOf(0) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 48.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            
            Text("Set Time", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(24.dp))
            
            TimePicker(state = state) // Native M3 Dial
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("Repeat Days", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEachIndexed { index, day ->
                    val isSelected = selectedDays.contains(index + 1)
                    val bubbleColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, label = "")
                    val bubbleText by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, label = "")
                    
                    Box(
                        modifier = Modifier.size(42.dp).clip(CircleShape).background(bubbleColor).clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedDays = if (isSelected) selectedDays - (index + 1) else selectedDays + (index + 1)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(day, color = bubbleText, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Repeat Interval: ${if (interval == 0) "None" else "$interval mins"}", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = interval.toFloat(),
                onValueChange = { interval = it.toInt() },
                valueRange = 0f..120f,
                steps = 7,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { onSave(state.hour, state.minute, selectedDays, interval) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Reminder", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}