package tech.relaycorp.letro.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Primary80,
    onPrimary = Grey100,
    // TODO Maybe uncomment if the designs change
//    secondary = SecondarySkyBlue,
//    onSecondary = Color.White,
//    tertiary = SecondaryPink,
//    onTertiary = Color.White,
//    surface = Grey100,
    surface = Error1,
    onSurface = Grey20,
    surfaceVariant = Grey80,
    onSurfaceVariant = Grey40,
    primaryContainer = Grey80,
    onPrimaryContainer = Primary20, // Currently used for the "disabled" state
    secondaryContainer = Grey60,
    background = Grey100,
    onBackground = Grey20,
    outline = Grey50,
    outlineVariant = Grey60,
    error = Error2,
    onError = Grey100,
)

private val LightColorScheme = lightColorScheme(
    primary = Primary100,
    onPrimary = Color.White,
    // TODO Maybe uncomment if the designs change
//    secondary = SecondarySkyBlue,
//    onSecondary = Color.White,
//    tertiary = SecondaryPink,
//    onTertiary = Color.White,
    primaryContainer = Grey80,
    onPrimaryContainer = Grey50, // Currently used for the "disabled" state
    secondaryContainer = Grey20,
    surface = Color.White,
    onSurface = BlueGrey100,
    surfaceVariant = Primary100,
    onSurfaceVariant = Primary40,
    background = Color.White,
    onBackground = BlueGrey100,
    outline = BlueGrey60,
    outlineVariant = BlueGrey10,
    error = Error1,
    onError = Color.White,
)

@Composable
fun LetroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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


@Composable
private fun LetroThemeContent() {
    Column {
        Button(onClick = { /*TODO*/ }) {
            Text("Button")
        }
        Spacer(modifier = Modifier.size(ItemPadding))

        TextField(value = "", onValueChange = {})
        Spacer(modifier = Modifier.size(ItemPadding))

        TextField(value = "text field", onValueChange = {})
        Spacer(modifier = Modifier.size(ItemPadding))

        TextField(value = "error", onValueChange = {}, isError = true)
        Spacer(modifier = Modifier.size(ItemPadding))

        OutlinedTextField(value = "", onValueChange = {})
        Spacer(modifier = Modifier.size(ItemPadding))

        OutlinedTextField(value = "outlined text field", onValueChange = {})
        Spacer(modifier = Modifier.size(ItemPadding))

        OutlinedTextField(value = "error", onValueChange = {}, isError = true)
        Spacer(modifier = Modifier.size(ItemPadding))
    }
}

@Preview(showSystemUi = true)
@Composable
private fun LetroThemeLightPreview() {
    LetroTheme(darkTheme = false) {
        LetroThemeContent()
    }
}

@Preview(showSystemUi = true)
@Composable
private fun LetroThemeDarkPreview() {
    LetroTheme(darkTheme = true) {
        LetroThemeContent()
    }
}

