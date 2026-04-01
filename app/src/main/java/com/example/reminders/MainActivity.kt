package com.example.reminders

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

// --- Types & Data Models ---
enum class Day(val shortName: String, val single: String) {
    Mon("Mon", "M"), Tue("Tue", "T"), Wed("Wed", "W"), 
    Thu("Thu", "T"), Fri("Fri", "F"), Sat("Sat", "S"), Sun("Sun", "S")
}

enum class NotificationType { None, Soft, Medium, Heavy, Pulse, Double }

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val startTime: String,
    val endTime: String? = null,
    val intervalMinutes: Int? = null,
    val repeatDays: List<Day> = Day.values().toList(),
    var isEnabled: Boolean = true,
    var isExpanded: Boolean = false,
    val notificationType: NotificationType = NotificationType.Medium
)

// --- Material 3 Expressive Theme ---
val M3DarkColors = darkColorScheme(
    primary = Color(0xFFD0E4FF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF253140),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC3C7CF),
    background = Color(0xFF1A1C1E),
    error = Color(0xFFFFB4AB)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = M3DarkColors) {
                RemindersApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun RemindersApp() {
    var reminders by remember { mutableStateOf(listOf<Reminder>()) }
    var isAdding by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        
        // Main Screen Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            
            // Header
            Icon(
                Icons.Default.Notifications, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            )
            Text(
                text = "Reminders",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "${reminders.count { it.isEnabled }} active patterns",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // List of Reminders
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 120.dp) // Space for floating bar
            ) {
                if (reminders.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.size(64.dp))
                                Text("NO REMINDERS SET", color = Color.Gray.copy(alpha = 0.4f), fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
                
                items(reminders) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onToggle = { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            reminders = reminders.map { if (it.id == reminder.id) it.copy(isEnabled = !it.isEnabled) else it }
                        },
                        onExpand = {
                            reminders = reminders.map { if (it.id == reminder.id) it.copy(isExpanded = !it.isExpanded) else it }
                        },
                        onDelete = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            reminders = reminders.filter { it.id != reminder.id }
                        }
                    )
                }
            }
        }

        // Floating Bottom Bar (Replicates PWA style)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Pill shaped info bar
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("REMINDERS", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                }

                // Plus Button
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isAdding = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
                }
            }
        }

        // Add Dialog (Full Screen)
        AnimatedVisibility(
            visible = isAdding,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            AddReminderScreen(
                onClose = { isAdding = false },
                onSave = { newReminder ->
                    reminders = listOf(newReminder) + reminders
                    isAdding = false
                }
            )
        }
    }
}

@Composable
fun ReminderCard(
    reminder: Reminder, 
    onToggle: () -> Unit, 
    onExpand: () -> Unit,
    onDelete: () -> Unit
) {
    val alpha = if (reminder.isEnabled) 1f else 0.3f
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpand() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f * alpha)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f * alpha))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header: Title and Switch
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = reminder.title.uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                )
                Switch(checked = reminder.isEnabled, onCheckedChange = { onToggle() })
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // Times
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = reminder.startTime,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                )
                if (!reminder.endTime.isNullOrEmpty()) {
                    Text(text = " —> ", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f * alpha), modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        text = reminder.endTime,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f * alpha)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.White.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Days and Interval
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Day.values().forEach { day ->
                        Text(
                            text = day.single,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = if (reminder.repeatDays.contains(day)) MaterialTheme.colorScheme.primary.copy(alpha = alpha) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f * alpha)
                        )
                    }
                }
                if (reminder.intervalMinutes != null) {
                    Text(
                        text = "${reminder.intervalMinutes}M INTERVAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = alpha),
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Expanded Actions
            AnimatedVisibility(visible = reminder.isExpanded) {
                Column(modifier = Modifier.padding(top = 24.dp)) {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DELETE PATTERN", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(onClose: () -> Unit, onSave: (Reminder) -> Unit) {
    var title by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("") }
    var intervalMinutes by remember { mutableStateOf("") }
    var notificationType by remember { mutableStateOf(NotificationType.Medium) }
    var repeatDays by remember { mutableStateOf(Day.values().toList()) }
    
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp).padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) { Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurface) }
                
                Text("CREATE", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                
                Button(
                    onClick = { 
                        onSave(Reminder(
                            title = title.ifEmpty { "Reminder" },
                            startTime = startTime.ifEmpty { "08:00" },
                            endTime = endTime.takeIf { it.isNotEmpty() },
                            intervalMinutes = intervalMinutes.toIntOrNull(),
                            repeatDays = repeatDays,
                            notificationType = notificationType
                        )) 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("SAVE", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            
            // Scrollable Content
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Label Section
                item {
                    Text("REMINDER LABEL", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("What's the mission?", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    )
                }

                // Schedule Section
                item {
                    Text("TIME WINDOW (24H)", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("From") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("To (Opt)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = intervalMinutes,
                        onValueChange = { intervalMinutes = it },
                        label = { Text("Repeat every X minutes") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                // Haptic Feedback Section
                item {
                    Text("HAPTIC FEEDBACK", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.tertiary, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Simple Grid replacement using Rows
                    val types = NotificationType.values().toList()
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            types.take(3).forEach { type ->
                                HapticButton(type, notificationType == type, Modifier.weight(1f)) {
                                    notificationType = type
                                    if (type != NotificationType.None) vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            types.drop(3).forEach { type ->
                                HapticButton(type, notificationType == type, Modifier.weight(1f)) {
                                    notificationType = type
                                    if (type != NotificationType.None) vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                                }
                            }
                        }
                    }
                }

                // Repeat Cycle Section
                item {
                    Text("REPEAT CYCLE", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Day.values().forEach { day ->
                            val isSelected = repeatDays.contains(day)
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        repeatDays = if (isSelected) repeatDays.filter { it != day } else repeatDays + day
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.shortName.substring(0, 2),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HapticButton(type: NotificationType, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(56.dp)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = type.name,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}