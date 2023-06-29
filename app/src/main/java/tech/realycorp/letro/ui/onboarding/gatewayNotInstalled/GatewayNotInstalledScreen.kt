package tech.realycorp.letro.ui.onboarding.gatewayNotInstalled

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.realycorp.letro.R
import tech.realycorp.letro.ui.custom.LetroButton
import tech.realycorp.letro.ui.theme.HorizontalScreenPadding
import tech.realycorp.letro.ui.theme.LetroTheme

@Composable
fun GatewayNotInstalledScreen(
    onNavigateToGooglePlay: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = HorizontalScreenPadding,
                vertical = HorizontalScreenPadding,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_install_awala_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.onbaording_install_awala_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
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
