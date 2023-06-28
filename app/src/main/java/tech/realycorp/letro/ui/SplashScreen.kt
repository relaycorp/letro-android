package tech.realycorp.letro.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import tech.realycorp.letro.ui.theme.LetroTheme

@Composable
fun SplashScreen() {
    Text(text = "Temporary Splash Screen")
}

@Preview(showBackground = true)
@Composable
fun SplashPreview() {
    LetroTheme {
        SplashScreen()
    }
}
