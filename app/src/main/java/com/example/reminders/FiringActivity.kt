package com.example.reminders

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight // <--- THIS WAS MISSING
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class FiringActivity : ComponentActivity() {
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Critical flags for Android 16 Lock Screen wakeup
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        val reminderId = intent.getStringExtra("REMINDER_ID") ?: "0"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(reminderId.hashCode()) // Clean up notification

        startExpressiveHaptics()

        setContent {
            AppTheme {
                FiringScreen(onDismiss = {
                    vibrator?.cancel()
                    finish()
                })
            }
        }
    }

    private fun startExpressiveHaptics() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        // Modern physics-based escalating vibration
        val pattern = longArrayOf(0, 30, 80, 40, 80, 50, 1000) 
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }
}

@Composable
fun FiringScreen(onDismiss: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var pressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = ""
    )

    var pulse by remember { mutableStateOf(1f) }
    val bgScale by animateFloatAsState(
        targetValue = pulse,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessVeryLow), label = ""
    )

    LaunchedEffect(Unit) {
        while (true) {
            pulse = 1.1f; delay(500); pulse = 1f; delay(500)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = bgScale; scaleY = bgScale }.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.25f)))
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("IT'S TIME", style = MaterialTheme.typography.displayLarge, fontSize = 64.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(120.dp))
            
            Surface(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                pressed = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                tryAwaitRelease()
                                pressed = false
                                onDismiss()
                            }
                        )
                    },
                shape = RoundedCornerShape(40.dp), // M3 Squircle
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("STOP", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}