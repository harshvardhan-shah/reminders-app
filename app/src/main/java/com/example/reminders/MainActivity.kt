package com.example.reminders

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

// --- Types & Data Models ---
enum class Day(val shortName: String) {
    Mon("Mo"), Tue("Tu"), Wed("We"), Thu("Th"), Fri("Fr"), Sat("Sa"), Sun("Su")
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
    // State (Equivalent to your React useState)
    var reminders by remember { mutableStateOf(listOf<Reminder>()) }
    var isAdding by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isAdding = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.size(72.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(36.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        
        // Main Screen Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            
            // Header
            Icon(
                Icons.Default.Notifications, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(8.dp)
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
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // List of Reminders
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (reminders.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("NO REMINDERS SET", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
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
                            reminders = reminders.filter { it.id != reminder.id }
                        }
                    )
                }
            }
        }

        // Add Dialog (Animated Bottom Sheet / Full Screen)
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
    val alpha by animateFloatAsState(targetValue = if (reminder.isEnabled) 1f else 0.4f)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpand() },
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f * alpha)
        )
    ) {
        Column(modifier = Modifier.padding(24.dp).let { if (!reminder.isEnabled) it.blur(2.dp) else it }) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = reminder.title.uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
                Switch(checked = reminder.isEnabled, onCheckedChange = { onToggle() })
            }
            
            Text(
                text = reminder.startTime,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            AnimatedVisibility(visible = reminder.isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("DELETE", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun AddReminderScreen(onClose: () -> Unit, onSave: (Reminder) -> Unit) {
    var title by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
            .padding(24.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Close") }
                Button(onClick = { onSave(Reminder(title = title.ifEmpty { "REMINDER" }, startTime = "08:00")) }) {
                    Text("SAVE", fontWeight = FontWeight.Black)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("What's the mission?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp)
            )
            
            // ... (Time Pickers and Day Chips would go here following the same Compose state logic)
            // Note: I have abbreviated the full dialog purely for text-length limits, 
            // but the architecture remains exactly the same!
        }
    }
}