package com.example.kaskita.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreenDark,
    onPrimary = BackgroundDark,
    primaryContainer = PrimaryGreenDarkContainer,
    onPrimaryContainer = TextPrimaryDark,
    secondary = SecondaryGoldDark,
    onSecondary = BackgroundDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    outline = BorderDark,
    error = ExpenseRedDark,
    onError = SurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = SurfaceLight,
    primaryContainer = PrimaryGreenLight,
    onPrimaryContainer = PrimaryGreen,
    secondary = SecondaryGold,
    onSecondary = SurfaceLight,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    outline = BorderLight,
    error = ExpenseRed,
    onError = SurfaceLight
)

@Composable
fun KasKitaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
