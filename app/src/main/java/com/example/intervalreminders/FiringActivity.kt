package com.example.intervalreminders

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

class FiringActivity : ComponentActivity() {
    private var vibrator: Vibrator? = null
    private var ringtone: android.media.Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        val vibrationType = intent.getStringExtra("VIBRATION_TYPE") ?: "Heartbeat"
        val playMusic = intent.getBooleanExtra("PLAY_MUSIC", false)
        
        startAlarm(vibrationType, playMusic)

        setContent {
            AppTheme {
                FiringScreen(onDismiss = { dismissAlarm() })
            }
        }
    }

    private fun startAlarm(vibrationType: String, playMusic: Boolean) {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = when (vibrationType) {
            "Heartbeat" -> longArrayOf(0, 150, 100, 150, 800)
            "Rapid" -> longArrayOf(0, 50, 50, 50, 50, 50)
            "Long Pulse" -> longArrayOf(0, 1000, 500, 1000, 500)
            else -> longArrayOf(0, 500, 500)
        }
        
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 1))

        if (playMusic) {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(this, uri)
            ringtone?.play()
        }
    }

    private fun dismissAlarm() {
        vibrator?.cancel()
        ringtone?.stop()
        
        ReminderManager.scheduleNextReminder(
            context = this,
            startHour = intent.getIntExtra("START_H", 9),
            startMinute = intent.getIntExtra("START_M", 0),
            endHour = intent.getIntExtra("END_H", 17),
            endMinute = intent.getIntExtra("END_M", 0),
            intervalMinutes = intent.getIntExtra("INTERVAL", 60),
            vibrationType = intent.getStringExtra("VIBRATION_TYPE") ?: "Heartbeat",
            playMusic = intent.getBooleanExtra("PLAY_MUSIC", false)
        )
        finish()
    }
}

@Composable
fun FiringScreen(onDismiss: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "Button Scale"
    )

    var pulse by remember { mutableStateOf(1f) }
    val bgScale by animateFloatAsState(
        targetValue = pulse,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessVeryLow),
        label = "BG Pulse"
    )

    LaunchedEffect(Unit) {
        while (true) {
            pulse = 1.05f; delay(500)
            pulse = 1f; delay(500)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(300.dp).scale(bgScale)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), CircleShape)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Reminder", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(48.dp))
            Surface(
                modifier = Modifier.size(120.dp).scale(scale)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                pressed = true; tryAwaitRelease(); pressed = false
                                onDismiss()
                            }
                        )
                    },
                shape = CircleShape, color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("STOP", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}