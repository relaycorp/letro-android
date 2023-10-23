package tech.relaycorp.letro.awala.ui.error

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.relaycorp.letro.R
import tech.relaycorp.letro.awala.ui.initialization.AwalaInitializationInProgress
import tech.relaycorp.letro.ui.common.LetroButtonMaxWidthFilled
import tech.relaycorp.letro.ui.common.LetroTopTitle
import tech.relaycorp.letro.ui.navigation.Route
import tech.relaycorp.letro.utils.compose.DoOnLifecycleEvent

@Composable
fun AwalaInitializationError(
    @Route.AwalaInitializationError.Type type: Int,
    onOpenAwalaClick: (() -> Unit)? = null,
    viewModel: AwalaInitializationErrorViewModel = hiltViewModel(),
) {
    DoOnLifecycleEvent(
        onResume = { viewModel.onScreenResumed() },
        onDestroy = { viewModel.onScreenDestroyed() },
    )

    val showAwalaInitialization by viewModel.isAwalaInitializingShown.collectAsState()

    if (showAwalaInitialization) {
        AwalaInitializationInProgress()
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            LetroTopTitle()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 16.dp,
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(painter = painterResource(id = R.drawable.awala_initialization_error), contentDescription = null)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(id = if (type == Route.AwalaInitializationError.TYPE_NEED_TO_OPEN_AWALA) R.string.awala_isnt_fully_setup_yet else R.string.we_failed_to_set_up_awala),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(24.dp))
                when (type) {
                    Route.AwalaInitializationError.TYPE_NON_FATAL_ERROR -> {
                        LetroButtonMaxWidthFilled(
                            text = stringResource(id = R.string.try_again),
                            onClick = { viewModel.onTryAgainClick() },
                        )
                    }
                    Route.AwalaInitializationError.TYPE_NEED_TO_OPEN_AWALA -> {
                        LetroButtonMaxWidthFilled(
                            text = stringResource(id = R.string.open_awala),
                            onClick = { onOpenAwalaClick?.invoke() },
                        )
                    }
                    Route.AwalaInitializationError.TYPE_FATAL_ERROR -> {
                        Text(
                            text = stringResource(id = R.string.unable_setup_letro_text),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(id = R.string.reinstall_letro_error),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun AwalaInitializationFatalError_Preview() {
    AwalaInitializationError(
        type = Route.AwalaInitializationError.TYPE_FATAL_ERROR,
    )
}

@Composable
@Preview
private fun AwalaInitializationNonFatalError_Preview() {
    AwalaInitializationError(
        type = Route.AwalaInitializationError.TYPE_NON_FATAL_ERROR,
    )
}

@Composable
@Preview
private fun AwalaInitializationNeedOpenAwalaError_Preview() {
    AwalaInitializationError(
        type = Route.AwalaInitializationError.TYPE_NEED_TO_OPEN_AWALA,
    )
}
