package tech.relaycorp.letro.awala.ui.error

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.common.LetroButtonMaxWidthFilled
import tech.relaycorp.letro.ui.theme.LetroColor

@Composable
fun AwalaInitializationError(
    viewModel: AwalaInitializationErrorViewModel = hiltViewModel(),
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = LetroColor.SurfaceContainerHigh,
                )
                .padding(
                    vertical = 16.dp,
                ),
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                color = LetroColor.OnSurfaceContainerHigh,
            )
        }
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
                text = stringResource(id = R.string.we_failed_to_set_up_awala),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(24.dp))
            LetroButtonMaxWidthFilled(
                text = stringResource(id = R.string.try_again),
                onClick = { viewModel.onTryAgainClick() },
            )
        }
    }
}

@Composable
@Preview
private fun AwalaInitializationError_Preview() {
    AwalaInitializationError()
}
