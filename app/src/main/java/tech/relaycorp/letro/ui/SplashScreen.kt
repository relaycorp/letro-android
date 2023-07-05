package tech.relaycorp.letro.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.theme.LetroTheme

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
private fun SplashPreview() {
    LetroTheme {
        SplashScreen()
    }
}
