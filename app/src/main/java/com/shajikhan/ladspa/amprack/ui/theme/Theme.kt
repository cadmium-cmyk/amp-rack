package com.shajikhan.ladspa.amprack.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Blue40,
    secondary = ThemeModernAccent,
    tertiary = Orange40,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE5E6EE),
    onSurface = Color(0xFFE5E6EE),
    onSurfaceVariant = Color(0xFFB0B4C6)
)

enum class AppTheme(val displayName: String, val accentColor: Color, val cardColor: Color) {
    MODERN("Modern", ThemeModernAccent, Color(0xFF1E212B)),
    TUBE_AMP("TubeAmp", ThemeTubeAmpAccent, Color(0xFF261D16)),
    CLASSIC("Classic", ThemeClassicAccent, Color(0xFF231E1E)),
    ANALOG("Analog", ThemeAnalogAccent, Color(0xFF152220)),
    HYBRID("Hybrid", ThemeHybridAccent, Color(0xFF231828)),
    ADWAITA("Adwaita", ThemeAdwaitaAccent, Color(0xFF1E242B))
}

@Composable
fun AmpRackTheme(
    selectedTheme: AppTheme = AppTheme.MODERN,
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme.copy(
        primary = selectedTheme.accentColor,
        secondary = selectedTheme.accentColor,
        surface = selectedTheme.cardColor
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DarkBackground.toArgb()
                window.navigationBarColor = DarkBackground.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
