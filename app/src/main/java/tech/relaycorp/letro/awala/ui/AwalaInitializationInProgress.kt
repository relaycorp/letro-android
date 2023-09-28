package tech.relaycorp.letro.awala.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.theme.LetroColor

@Composable
fun AwalaInitializationInProgress(
    texts: Array<String>,
    viewModel: AwalaInitializationInProgressViewModel = hiltViewModel(),
) {
    val currentTextIndex by viewModel.stringsIndexPointer.collectAsState()

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
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(id = R.string.we_setting_things_up),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = texts[currentTextIndex % texts.size],
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(24.dp))
            LinearProgressIndicator(
                trackColor = LetroColor.SurfaceContainer,
                color = MaterialTheme.colorScheme.primary,
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AwalaInstallationProgressView_Preview() {
    AwalaInitializationInProgress(
        arrayOf("Hello"),
        hiltViewModel(),
    )
}
