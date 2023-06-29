package tech.realycorp.letro.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import tech.realycorp.letro.R
import tech.realycorp.letro.ui.theme.LetroTheme

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.letro_icon),
            contentDescription = null,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashPreview() {
    LetroTheme {
        SplashScreen()
    }
}
