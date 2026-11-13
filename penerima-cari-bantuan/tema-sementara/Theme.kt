package com.example.sempat.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Warna tetap (TIDAK mengikuti wallpaper HP) dan hanya mode terang
private val SempatColorScheme = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = TealMuda,
    onPrimaryContainer = TealGelap,
    secondary = Orange,
    onSecondary = Color.White,
    secondaryContainer = TealMuda,          // pil penanda tab aktif di bar bawah
    onSecondaryContainer = TealGelap,
    background = Latar,
    onBackground = TeksGelap,
    surface = Color.White,
    onSurface = TeksGelap,
    surfaceVariant = TealMuda,
    onSurfaceVariant = AbuTeks,
    outline = Teal,
    outlineVariant = GarisHalus,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,         // warna bar bawah
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Latar
)

@Composable
fun SempatTheme(content: @Composable () -> Unit) {
    // Status bar terang dengan ikon gelap
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Latar.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = SempatColorScheme,
        typography = Typography,
        content = content
    )
}