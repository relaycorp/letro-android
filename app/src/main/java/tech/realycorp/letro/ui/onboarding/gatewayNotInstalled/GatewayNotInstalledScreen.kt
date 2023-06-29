package tech.realycorp.letro.ui.onboarding.gatewayNotInstalled

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import tech.realycorp.letro.R
import tech.realycorp.letro.ui.custom.LetroButton
import tech.realycorp.letro.ui.theme.LetroTheme

@Composable
fun GatewayNotInstalledScreen(
    onNavigateToGooglePlay: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_install_awala_title),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(id = R.string.onbaording_install_awala_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        LetroButton(
            text = stringResource(id = R.string.onboarding_install_awala_button),
            onClick = onNavigateToGooglePlay,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GatewayNotInstalledPreview() {
    LetroTheme {
        GatewayNotInstalledScreen {}
    }
}
