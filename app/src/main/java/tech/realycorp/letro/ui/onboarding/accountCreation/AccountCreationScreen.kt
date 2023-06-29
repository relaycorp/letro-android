package tech.realycorp.letro.ui.onboarding.accountCreation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import tech.realycorp.letro.ui.theme.LetroTheme

@Composable
fun AccountCreationScreen() {
    Text(text = "Account Creation Temporary Screen")
}

@Preview(showBackground = true)
@Composable
fun AccountCreationPreview() {
    LetroTheme {
        AccountCreationScreen()
    }
}
