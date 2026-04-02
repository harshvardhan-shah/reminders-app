package com.example.reminders

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

// Material 3 Expressive Dark Colors
private val ExpressiveDark = darkColorScheme(
    primary = Color(0xFF00E5FF), // Bright Cyan for + button and Active toggles
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF004D40), // Active Card Background
    onPrimaryContainer = Color(0xFF80DEEA),
    background = Color(0xFF050505), // Pitch Black background
    surface = Color(0xFF050505),
    surfaceVariant = Color(0xFF161618), // Inactive Card background
    onSurfaceVariant = Color(0xFF757575), // Inactive text
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF)
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ExpressiveDark,
        typography = MaterialTheme.typography.copy(
            displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = RobotoFlex),
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = RobotoFlex),
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = RobotoFlex)
        ),
        content = content
    )
}