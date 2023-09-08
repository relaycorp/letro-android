package tech.relaycorp.letro.awala

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import tech.relaycorp.letro.R
import tech.relaycorp.letro.main.MainViewModel
import tech.relaycorp.letro.ui.common.LetroButtonMaxWidthFilled
import tech.relaycorp.letro.ui.navigation.Route
import tech.relaycorp.letro.ui.theme.HorizontalScreenPadding
import tech.relaycorp.letro.ui.theme.LetroTheme
import tech.relaycorp.letro.utils.compose.rememberLifecycleEvent

@Composable
fun AwalaNotInstalledScreen(
    mainViewModel: MainViewModel,
    onInstallAwalaClick: () -> Unit,
) {
    val lifecycleEvent = rememberLifecycleEvent()
    LaunchedEffect(lifecycleEvent) {
        if (lifecycleEvent == Lifecycle.Event.ON_RESUME) {
            mainViewModel.onScreenResumed(Route.AwalaNotInstalled)
        }
    }
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
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(
            modifier = Modifier.height(24.dp)
        )
        Text(
            text = stringResource(id = R.string.onbaording_install_awala_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(
            modifier = Modifier.height(24.dp)
        )
        LetroButtonMaxWidthFilled(
            text = stringResource(id = R.string.onboarding_install_awala_button),
            onClick = onInstallAwalaClick,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GatewayNotInstalledPreview() {
    LetroTheme {
        AwalaNotInstalledScreen(hiltViewModel()) {}
    }
}
