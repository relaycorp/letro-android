package tech.relaycorp.letro.ui.onboarding.pair

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.custom.LetroButtonMaxWidthFilled
import tech.relaycorp.letro.ui.custom.LetroOutlinedTextField
import tech.relaycorp.letro.ui.theme.HorizontalScreenPadding
import tech.relaycorp.letro.ui.theme.ItemPadding
import tech.relaycorp.letro.ui.theme.LetroTheme
import tech.relaycorp.letro.ui.theme.VerticalScreenPadding

@Composable
fun PairWithPeopleRoute(
    navigateBack: () -> Unit,
    navigateToPairingRequestSentScreen: () -> Unit,
    viewModel: PairViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiStateFlow.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.navigateToPairingRequestSent.collect {
            navigateToPairingRequestSentScreen()
        }
    }

    PairWithPeopleScreen(
        navigateBack = navigateBack,
        id = uiState.value.address,
        onIdInput = viewModel::onAddressInput,
        alias = uiState.value.alias,
        onAliasInput = viewModel::onAliasInput,
        onRequestPairingClicked = viewModel::onRequestPairingClicked,
    )
}

@Composable
private fun PairWithPeopleScreen(
    navigateBack: () -> Unit,
    id: String,
    onIdInput: (String) -> Unit,
    alias: String,
    onAliasInput: (String) -> Unit,
    onRequestPairingClicked: () -> Unit,
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
                text = stringResource(id = R.string.general_pair_with_others),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Spacer(modifier = Modifier.height(VerticalScreenPadding))
        Text(
            text = stringResource(id = R.string.general_id),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(ItemPadding))
        LetroOutlinedTextField(
            value = id,
            onValueChange = onIdInput,
        )
        Spacer(modifier = Modifier.height(VerticalScreenPadding))
        Text(
            text = stringResource(id = R.string.onboarding_pair_with_people_alias),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(ItemPadding))
        LetroOutlinedTextField(
            value = alias,
            onValueChange = onAliasInput,
        )
        Spacer(modifier = Modifier.weight(1f))
        LetroButtonMaxWidthFilled(
            text = stringResource(id = R.string.onboarding_pair_with_people_button),
            onClick = onRequestPairingClicked,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UseExistingAccountPreview() {
    LetroTheme {
        PairWithPeopleScreen(
            navigateBack = {},
            id = "jamesbond@cuppa.uk",
            onIdInput = {},
            alias = "James Bond",
            onAliasInput = {},
            onRequestPairingClicked = {},
        )
    }
}
