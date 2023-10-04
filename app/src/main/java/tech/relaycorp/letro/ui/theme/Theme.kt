package tech.relaycorp.letro.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = lightColorScheme(
    primary = Primary3,
    onPrimary = Primary1,
    secondary = Secondary5,
    onSecondary = Secondary1,
    error = Error2,
    errorContainer = Error4,
    surface = NeutralVariant1,
    onSurface = NeutralVariant8,
    surfaceVariant = NeutralVariant3,
    onSurfaceVariant = NeutralVariant6,
    primaryContainer = NeutralVariant4,
    onPrimaryContainer = NeutralVariant5,
    secondaryContainer = NeutralVariant2,
    outline = NeutralVariant5,
    outlineVariant = NeutralVariant4,
    background = NeutralVariant1,
    onBackground = NeutralVariant8,
    onError = Neutral8,
)

private val LightColorScheme = lightColorScheme(
    primary = Primary2,
    onPrimary = Neutral8,
    secondary = Secondary2,
    onSecondary = Secondary1,
    error = Error1,
    errorContainer = Error3,
    surface = Neutral8,
    onSurface = Neutral2,
    surfaceVariant = Neutral7,
    onSurfaceVariant = Neutral3,
    primaryContainer = Neutral6,
    onPrimaryContainer = Neutral4,
    secondaryContainer = Primary2,
    outline = Neutral5,
    outlineVariant = NeutralVariant8,
    background = Neutral8,
    onBackground = Neutral2,
    onError = Neutral8,
)

@Composable
fun LetroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
