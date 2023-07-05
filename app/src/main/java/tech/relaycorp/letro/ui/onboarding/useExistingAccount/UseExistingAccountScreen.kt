package tech.relaycorp.letro.ui.onboarding.useExistingAccount

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.hilt.navigation.compose.hiltViewModel
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.custom.LetroButton
import tech.relaycorp.letro.ui.custom.LetroTextField
import tech.relaycorp.letro.ui.theme.HorizontalScreenPadding
import tech.relaycorp.letro.ui.theme.ItemPadding
import tech.relaycorp.letro.ui.theme.LetroTheme
import tech.relaycorp.letro.ui.theme.VerticalScreenPadding

@Composable
fun UseExistingAccountRoute(
    navigateBack: () -> Unit,
    navigateToAccountConfirmationScreen: () -> Unit,
    viewModel: UseExistingAccountViewModel = hiltViewModel(),
) {
    val domain by viewModel.domainNameUIFlow.collectAsState()
    val token by viewModel.tokenUIFlow.collectAsState()

    UseExistingAccountScreen(
        navigateBack = navigateBack,
        domain = domain,
        onDomainInput = {
            viewModel.onDomainNameInput(it)
        },
        token = token,
        onTokenInput = {
            viewModel.onTokenInput(it)
        },
        onConfirmClicked = navigateToAccountConfirmationScreen, // TODO Replace when real data is used
    )
}

@Composable
private fun UseExistingAccountScreen(
    navigateBack: () -> Unit,
    domain: String,
    onDomainInput: (String) -> Unit,
    token: String,
    onTokenInput: (String) -> Unit,
    onConfirmClicked: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(
            horizontal = HorizontalScreenPadding,
            vertical = VerticalScreenPadding,
        ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = navigateBack) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = stringResource(id = R.string.general_navigate_back),
                )
            }
            Text(
                text = stringResource(id = R.string.general_use_existing_account),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Spacer(modifier = Modifier.height(VerticalScreenPadding))
        Text(
            text = stringResource(id = R.string.onboarding_use_existing_account_domain),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(ItemPadding))
        LetroTextField(
            value = domain,
            onValueChange = onDomainInput,
        )
        Spacer(modifier = Modifier.height(VerticalScreenPadding))
        Text(
            text = stringResource(id = R.string.onboarding_use_existing_account_token),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(ItemPadding))
        LetroTextField(
            value = token,
            onValueChange = onTokenInput,
        )
        Spacer(modifier = Modifier.weight(1f))
        LetroButton(
            text = stringResource(id = R.string.onboarding_use_existing_account_button),
            onClick = onConfirmClicked,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UseExistingAccountPreview() {
    LetroTheme {
        UseExistingAccountScreen(
            navigateBack = {},
            domain = "bbc.com",
            onDomainInput = {},
            token = "12b46543-e26a-4284-87a1-8a25d4f79d65",
            onTokenInput = {},
            onConfirmClicked = {},
        )
    }
}
