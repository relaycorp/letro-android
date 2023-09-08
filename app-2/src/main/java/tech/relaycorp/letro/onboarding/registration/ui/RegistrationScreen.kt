package tech.relaycorp.letro.onboarding.registration.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.relaycorp.letro.R
import tech.relaycorp.letro.onboarding.registration.RegistrationViewModel
import tech.relaycorp.letro.ui.common.ButtonType
import tech.relaycorp.letro.ui.common.HyperlinkText
import tech.relaycorp.letro.ui.common.LetroButtonMaxWidthFilled
import tech.relaycorp.letro.ui.common.LetroOutlinedTextField
import tech.relaycorp.letro.ui.theme.HorizontalScreenPadding
import tech.relaycorp.letro.ui.theme.LetroTheme

@Composable
fun RegistrationScreen(
    onUseExistingAccountClick: () -> Unit,
    viewModel: RegistrationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = HorizontalScreenPadding,
                    vertical = 64.dp,
                ),
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth(),
                painter = painterResource(id = R.drawable.letro_logo),
                contentDescription = null,
            )
            Spacer(
                modifier = Modifier.height(32.dp)
            )
            Text(
                text = stringResource(id = R.string.general_id),
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(
                modifier = Modifier.height(6.dp)
            )
            LetroOutlinedTextField(
                value = uiState.username,
                onValueChange = { viewModel.onUsernameInput(it) },
                placeHolderText = stringResource(id = R.string.onboarding_create_account_id_placeholder),
                suffixText = uiState.domain,
                isError = uiState.isError,
            )
            Spacer(
                modifier = Modifier.height(6.dp)
            )
            Text(
                text = stringResource(id = uiState.inputSuggestionText),
                style = MaterialTheme.typography.bodySmall,
                color = if (uiState.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
            Spacer(
                modifier = Modifier.height(16.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.info),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    contentDescription = null,
                )
                Spacer(
                    modifier = Modifier.width(8.dp)
                )
                HyperlinkText(
                    fullText = stringResource(id = R.string.onboarding_create_account_terms_and_services),
                    hyperLinks = mapOf(
                        stringResource(id = R.string.onboarding_create_account_terms_and_services_link_text)
                                to stringResource(id = R.string.url_letro_terms_and_conditions),
                    ),
                )
            }
            Spacer(
                modifier = Modifier.height(32.dp)
            )
            LetroButtonMaxWidthFilled(
                text = stringResource(id = R.string.onboarding_create_account_button),
                onClick = {
                    viewModel.onCreateAccountClick()
                },
                isEnabled = uiState.isCreateAccountButtonEnabled,
            )
            Spacer(
                modifier = Modifier.height(24.dp)
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Divider(modifier = Modifier.fillMaxWidth())
                Text(
                    text = stringResource(id = R.string.onboarding_create_account_or),
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(
                modifier = Modifier.height(24.dp)
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
        )
    }
}
