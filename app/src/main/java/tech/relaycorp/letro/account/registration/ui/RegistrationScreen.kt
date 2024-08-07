@file:OptIn(ExperimentalComposeUiApi::class)

package tech.relaycorp.letro.account.registration.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collect
import tech.relaycorp.letro.R
import tech.relaycorp.letro.account.registration.RegistrationViewModel
import tech.relaycorp.letro.base.utils.SnackbarString
import tech.relaycorp.letro.ui.common.ButtonType
import tech.relaycorp.letro.ui.common.LetroButtonMaxWidthFilled
import tech.relaycorp.letro.ui.common.LetroInfoView
import tech.relaycorp.letro.ui.common.LetroOutlinedTextField
import tech.relaycorp.letro.ui.common.text.HyperlinkText
import tech.relaycorp.letro.ui.theme.HorizontalScreenPadding
import tech.relaycorp.letro.ui.theme.LetroTheme

@Composable
fun RegistrationScreen(
    onUseExistingAccountClick: () -> Unit,
    showSnackbar: (SnackbarString) -> Unit,
    showOpenAwalaSnackbar: () -> Unit, // TODO: refactor it by merging snackbars' with and without action showing logic
    onBackClick: (() -> Unit)? = null,
    viewModel: RegistrationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        viewModel.showSnackbar.collect {
            showSnackbar(it)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.showOpenAwalaSnackbar.collect {
            showOpenAwalaSnackbar()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(
                    horizontal = HorizontalScreenPadding,
                    vertical = if (onBackClick == null) 64.dp else 28.dp,
                ),
        ) {
            if (onBackClick != null) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = stringResource(id = R.string.general_navigate_back),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clickable { onBackClick() },
                )
                Spacer(modifier = Modifier.height(48.dp))
            }
            Image(
                modifier = Modifier
                    .fillMaxWidth(),
                painter = painterResource(id = R.drawable.letro_logo),
                contentDescription = null,
            )
            Spacer(
                modifier = Modifier.height(32.dp),
            )
            LetroOutlinedTextField(
                value = uiState.username,
                onValueChange = { viewModel.onUsernameInput(it) },
                hintText = stringResource(id = R.string.onboarding_create_account_id_placeholder),
                suffixText = "@${uiState.domain}",
                isError = uiState.isError,
                label = R.string.general_id,
            )
            Spacer(
                modifier = Modifier.height(6.dp),
            )

            val inputSuggestionColor by animateColorAsState(
                targetValue = if (uiState.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                label = "InputSuggestionColor",
            )
            Text(
                text = stringResource(id = uiState.inputSuggestionText),
                style = MaterialTheme.typography.bodySmall,
                color = inputSuggestionColor,
            )
            Spacer(
                modifier = Modifier.height(16.dp),
            )
            LetroInfoView {
                HyperlinkText(
                    fullText = stringResource(id = R.string.onboarding_create_account_terms_and_services),
                    hyperLinks = mapOf(
                        stringResource(id = R.string.terms_and_conditions)
                            to stringResource(id = R.string.url_letro_terms_and_conditions),
                    ),
                )
            }
            Spacer(
                modifier = Modifier.height(32.dp),
            )
            LetroButtonMaxWidthFilled(
                text = stringResource(id = R.string.onboarding_create_account_button),
                onClick = {
                    viewModel.onCreateAccountClick()
                    keyboardController?.hide()
                },
                isEnabled = uiState.isCreateAccountButtonEnabled,
                withProgressIndicator = uiState.isSendingMessage,
            )
            Spacer(
                modifier = Modifier.height(24.dp),
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                Text(
                    text = stringResource(id = R.string.onboarding_create_account_or),
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(
                modifier = Modifier.height(24.dp),
            )
            LetroButtonMaxWidthFilled(
                text = stringResource(id = R.string.general_use_existing_account),
                buttonType = ButtonType.Outlined,
                onClick = { onUseExistingAccountClick() },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountCreationPreview() {
    LetroTheme {
        RegistrationScreen(
            hiltViewModel(),
            {},
            {},
        )
    }
}
