package tech.relaycorp.letro.ui

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import tech.relaycorp.letro.ui.theme.LetroTheme

@Composable
fun MessagingScreen() {
    MessagingView()
}

@Composable
fun MessagingView() {
    Button(onClick = {
        // TODO
    }) {
        Text(text = "Send message")
    }
}

@Preview(showBackground = true)
@Composable
fun MessagingPreview() {
    LetroTheme {
        MessagingView()
    }
}
