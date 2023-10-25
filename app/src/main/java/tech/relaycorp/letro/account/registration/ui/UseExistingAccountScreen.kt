package tech.relaycorp.letro.account.registration.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.relaycorp.letro.R
import tech.relaycorp.letro.account.registration.UseExistingAccountViewModel
import tech.relaycorp.letro.ui.common.LetroActionBarWithBackAction
import tech.relaycorp.letro.ui.common.LetroButtonMaxWidthFilled
import tech.relaycorp.letro.ui.common.LetroInfoView
import tech.relaycorp.letro.ui.common.LetroOutlinedTextField
import tech.relaycorp.letro.ui.common.LetroTopTitle

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UseExistingAccountScreen(
    onBackClick: () -> Unit,
    showSnackbar: (Int) -> Unit,
    viewModel: UseExistingAccountViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        viewModel.showSnackbar.collect {
            showSnackbar(it)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        LetroTopTitle()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            LetroActionBarWithBackAction(
                title = stringResource(R.string.general_use_existing_account),
                onBackClick = { onBackClick() },
            )
            Spacer(modifier = Modifier.height(8.dp))
            LetroOutlinedTextField(
                value = uiState.domain,
                onValueChange = { viewModel.onDomainInput(it) },
                label = R.string.domain,
                hintText = stringResource(id = R.string.use_existing_account_domain_hint),
            )
            Spacer(modifier = Modifier.height(24.dp))
            LetroOutlinedTextField(
                value = uiState.awalaEndpoint,
                onValueChange = { viewModel.onEndpointInput(it) },
                label = R.string.awala_endpoint,
                labelFollowingText = R.string.optional_braces,
                hintText = stringResource(id = R.string.use_existing_account_awala_endpoint_hint),
            )
            Spacer(modifier = Modifier.height(24.dp))
            LetroOutlinedTextField(
                value = uiState.token,
                onValueChange = { viewModel.onTokenInput(it) },
                label = R.string.token,
                hintText = stringResource(id = R.string.use_existing_account_token_hint),
                singleLine = true,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(16.dp))
            LetroInfoView {
                Text(
                    text = stringResource(id = R.string.use_existing_account_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            LetroButtonMaxWidthFilled(
                text = stringResource(id = R.string.confirm_details),
                isEnabled = uiState.isProceedButtonEnabled,
                onClick = {
                    viewModel.onConfirmButtonClick()
                    keyboardController?.hide()
                },
                withProgressIndicator = uiState.isSendingMessage,
            )
        }
    }
}
