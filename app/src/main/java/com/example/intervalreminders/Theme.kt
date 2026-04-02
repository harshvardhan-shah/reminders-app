package com.example.intervalreminders

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val RobotoFlex = FontFamily(
    Font(googleFont = GoogleFont("Roboto Flex"), fontProvider = provider)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8C7FA),
    onPrimary = Color(0xFF062E6F),
    primaryContainer = Color(0xFF284777),
    onPrimaryContainer = Color(0xFFD6E3FF),
    background = Color(0xFF111114),
    surface = Color(0xFF111114),
    surfaceVariant = Color(0xFF44474E),
    onBackground = Color(0xFFE2E2E2),
    onSurface = Color(0xFFE2E2E2)
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = MaterialTheme.typography.copy(
            displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = RobotoFlex),
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = RobotoFlex),
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = RobotoFlex)
        ),
        content = content
    )
}