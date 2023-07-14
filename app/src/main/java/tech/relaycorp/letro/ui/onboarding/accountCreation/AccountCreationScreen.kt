package tech.relaycorp.letro.ui.onboarding.accountCreation

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.custom.ButtonType
import tech.relaycorp.letro.ui.custom.HyperlinkText
import tech.relaycorp.letro.ui.custom.LetroButtonMaxWidthFilled
import tech.relaycorp.letro.ui.custom.LetroOutlinedTextField
import tech.relaycorp.letro.ui.theme.BoxCornerRadius
import tech.relaycorp.letro.ui.theme.HorizontalScreenPadding
import tech.relaycorp.letro.ui.theme.ItemPadding
import tech.relaycorp.letro.ui.theme.LargePadding
import tech.relaycorp.letro.ui.theme.LetroTheme
import tech.relaycorp.letro.ui.theme.VerticalScreenPadding
import tech.relaycorp.letro.utility.rememberLifecycleEvent

@Composable
fun AccountCreationRoute(
    onNavigateToAccountCreationWaitingScreen: () -> Unit,
    onUseExistingAccount: () -> Unit, // TODO Remove when real data is used
    viewModel: AccountCreationViewModel = hiltViewModel(),
) {
    val accountCreationUIState by viewModel.accountCreationUIState.collectAsState()

    val lifecycleEvent = rememberLifecycleEvent()
    LaunchedEffect(lifecycleEvent) {
        if (lifecycleEvent == Lifecycle.Event.ON_RESUME) {
            viewModel.onScreenResumed()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.goToLoadingScreen.collect { onNavigateToAccountCreationWaitingScreen() }
    }

    AccountCreationScreen(
        accountCreationUIState = accountCreationUIState,
        onCreateAccountClicked = viewModel::onCreateAccountClicked,
        onUseExistingAccountClicked = onUseExistingAccount,
        onUsernameInput = viewModel::onUsernameInput,
    )
}

@Composable
private fun AccountCreationScreen(
    accountCreationUIState: AccountCreationUIState,
    onCreateAccountClicked: () -> Unit,
    onUseExistingAccountClicked: () -> Unit,
    onUsernameInput: (String) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = HorizontalScreenPadding,
                    vertical = VerticalScreenPadding,
                ),
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth(),
                painter = painterResource(id = R.drawable.letro_logo),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = stringResource(id = R.string.general_id),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(ItemPadding))
            LetroOutlinedTextField(
                value = accountCreationUIState.username,
                onValueChange = onUsernameInput,
                placeHolderText = stringResource(id = R.string.onboarding_create_account_id_placeholder),
                suffixText = accountCreationUIState.domain,
            )
            HyperlinkText(
                fullText = stringResource(id = R.string.onboarding_create_account_terms_and_services),
                hyperLinks = mapOf(
                    stringResource(id = R.string.onboarding_create_account_terms_and_services_link_text)
                        to stringResource(id = R.string.url_letro_terms_and_conditions),
                ),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ItemPadding)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(BoxCornerRadius),
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.info),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(ItemPadding))
                Text(
                    text = stringResource(id = R.string.onboarding_create_account_no_internet_connection),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(modifier = Modifier.height(VerticalScreenPadding))
            LetroButtonMaxWidthFilled(
                text = stringResource(id = R.string.onboarding_create_account_button),
                onClick = onCreateAccountClicked,
            )
            Spacer(modifier = Modifier.height(LargePadding))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Divider(modifier = Modifier.fillMaxWidth())
                Text(
                    text = stringResource(id = R.string.onboarding_create_account_or),
                    modifier = Modifier
                        .padding(ItemPadding)
                        .background(MaterialTheme.colorScheme.surface),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(modifier = Modifier.height(LargePadding))
            LetroButtonMaxWidthFilled(
                text = stringResource(id = R.string.general_use_existing_account),
                buttonType = ButtonType.Outlined,
                onClick = onUseExistingAccountClicked,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountCreationPreview() {
    LetroTheme {
        AccountCreationScreen(AccountCreationUIState(), {}, {}, {})
    }
}
