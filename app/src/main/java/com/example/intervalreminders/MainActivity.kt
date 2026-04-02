package com.example.intervalreminders

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
        
        setContent {
            AppTheme {
                MainScreen(onSchedule = { sh, sm, eh, em, interval, vibe, music ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
                        if (!alarmManager.canScheduleExactAlarms()) {
                            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                            return@MainScreen
                        }
                    }
                    ReminderManager.scheduleNextReminder(this, sh, sm, eh, em, interval, vibe, music)
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onSchedule: (Int, Int, Int, Int, Int, String, Boolean) -> Unit) {
    var startHour by remember { mutableStateOf("09") }
    var endHour by remember { mutableStateOf("17") }
    var interval by remember { mutableStateOf("60") }
    var vibrationType by remember { mutableStateOf("Heartbeat") }
    var playMusic by remember { mutableStateOf(false) }

    val vibes = listOf("Heartbeat", "Rapid", "Long Pulse")
    var expanded by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Interval Reminders") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Time Window", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedTextField(
                            value = startHour, onValueChange = { startHour = it },
                            label = { Text("Start HH") }, modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        OutlinedTextField(
                            value = endHour, onValueChange = { endHour = it },
                            label = { Text("End HH") }, modifier = Modifier.weight(1f).padding(start = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = interval, onValueChange = { interval = it },
                        label = { Text("Interval (Minutes)") }, modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Alert Type", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = vibrationType, onValueChange = {}, readOnly = true,
                            label = { Text("Vibration Beat") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            vibes.forEach { selection ->
                                DropdownMenuItem(text = { Text(selection) }, onClick = { vibrationType = selection; expanded = false })
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Play Alarm Music", modifier = Modifier.padding(top = 12.dp))
                        Switch(checked = playMusic, onCheckedChange = { playMusic = it })
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    val sh = startHour.toIntOrNull() ?: 9
                    val eh = endHour.toIntOrNull() ?: 17
                    val inter = interval.toIntOrNull() ?: 60
                    onSchedule(sh, 0, eh, 0, inter, vibrationType, playMusic)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Start Reminders", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}