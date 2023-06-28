package tech.realycorp.letro.ui.onboarding.gatewayNotInstalled

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import tech.realycorp.letro.R
import tech.realycorp.letro.ui.custom.LetroButton
import tech.realycorp.letro.ui.theme.LetroTheme

@Composable
fun GatewayNotInstalledScreen(
    onNavigateToGooglePlay: () -> Unit,
) {
    Column {
        Text(text = stringResource(id = R.string.onboarding_awala_not_installed))
        Text(text = stringResource(id = R.string.onbaording_install_awala))
        LetroButton(
            text = stringResource(id = R.string.onboarding_download_awala),
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
