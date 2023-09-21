package tech.relaycorp.letro.awala.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import tech.relaycorp.letro.R
import tech.relaycorp.letro.main.MainViewModel
import tech.relaycorp.letro.ui.common.LetroButtonMaxWidthFilled
import tech.relaycorp.letro.ui.navigation.Route
import tech.relaycorp.letro.ui.theme.HorizontalScreenPadding
import tech.relaycorp.letro.utils.compose.rememberLifecycleEvent

@Composable
fun AwalaNotInstalledScreen(
    mainViewModel: MainViewModel,
    amusingTextsForInitializationWaiting: Array<String>,
    onInstallAwalaClick: () -> Unit,
    onAwalaStartedInitialization: () -> Unit,
    onAwalaStillNotInstalled: () -> Unit,
    awalaNotInstalledViewModel: AwalaNotInstalledViewModel = hiltViewModel(),
) {
    val lifecycleEvent = rememberLifecycleEvent()

    var currentTextIndex by remember { mutableStateOf(0) }
    val awalaInstallationState by awalaNotInstalledViewModel.awalaInstallationProgressUiState.collectAsState()

    LaunchedEffect(lifecycleEvent) {
        if (lifecycleEvent == Lifecycle.Event.ON_RESUME) {
            mainViewModel.onScreenResumed(Route.AwalaNotInstalled)
        }
    }

    LaunchedEffect(Unit) {
        awalaNotInstalledViewModel.awalaInstallationProgressUiState.collect {
            if (it != null) {
                onAwalaStartedInitialization()
            } else {
                onAwalaStillNotInstalled()
            }
        }
    }

    LaunchedEffect(Unit) {
        awalaNotInstalledViewModel.changeTextSignal.collect {
            currentTextIndex++
        }
    }

    val awalaInstallationStep = awalaInstallationState
    if (awalaInstallationStep == null) {
        InstallAwalaScreen(
            onInstallAwalaClick = onInstallAwalaClick,
        )
    } else {
        AwalaInitializationInProgress(
            text = amusingTextsForInitializationWaiting[currentTextIndex % amusingTextsForInitializationWaiting.size],
        )
    }
}

@Composable
private fun InstallAwalaScreen(
    onInstallAwalaClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = HorizontalScreenPadding,
                vertical = HorizontalScreenPadding,
            ),
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
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
                modifier = Modifier.height(24.dp),
            )
            Text(
                text = stringResource(id = R.string.onbaording_install_awala_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(
                modifier = Modifier.height(24.dp),
            )
            LetroButtonMaxWidthFilled(
                text = stringResource(id = R.string.onboarding_install_awala_button),
                onClick = onInstallAwalaClick,
            )
        }
    }
}
