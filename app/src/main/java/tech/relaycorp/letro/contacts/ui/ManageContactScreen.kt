package tech.relaycorp.letro.contacts.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import tech.relaycorp.letro.R
import tech.relaycorp.letro.contacts.ManageContactViewModel
import tech.relaycorp.letro.contacts.PairWithOthersUiState
import tech.relaycorp.letro.onboarding.actionTaking.ActionTakingScreen
import tech.relaycorp.letro.onboarding.actionTaking.ActionTakingScreenUIStateModel
import tech.relaycorp.letro.ui.common.LetroButtonMaxWidthFilled
import tech.relaycorp.letro.ui.common.LetroInfoView
import tech.relaycorp.letro.ui.common.LetroOutlinedTextField
import tech.relaycorp.letro.ui.theme.HorizontalScreenPadding
import tech.relaycorp.letro.ui.theme.LetroTheme

@Composable
fun ManageContactScreen(
    onBackClick: () -> Unit,
    onEditContactCompleted: (String) -> Unit,
    viewModel: ManageContactViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onEditContactCompleted.collect {
            onEditContactCompleted(it)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.goBackSignal.collect {
            onBackClick()
        }
    }

    if (!uiState.showRequestSentScreen) {
        ManageContactView(
            onBackClick,
            uiState,
            viewModel,
        )
    } else {
        ActionTakingScreen(
            actionTakingScreenUIStateModel = ActionTakingScreenUIStateModel.PairingRequestSent(
                boldPartOfMessage = uiState.veraidId,
                onGotItClicked = {
                    viewModel.onGotItClick()
                },
            ),
        )
    }
}

@Composable
private fun ManageContactView(
    onBackClick: () -> Unit,
    uiState: PairWithOthersUiState,
    viewModel: ManageContactViewModel,
) {
    val errorCaption = uiState.pairingErrorCaption

    Column(
        modifier = Modifier.padding(
            horizontal = HorizontalScreenPadding,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(
                    vertical = 18.dp,
                ),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = stringResource(id = R.string.general_navigate_back),
                Modifier.clickable {
                    onBackClick()
                },
            )
            Spacer(
                modifier = Modifier.width(16.dp),
            )
            Text(
                text = stringResource(id = uiState.manageContactTexts.title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(
            modifier = Modifier.height(8.dp),
        )
        LetroOutlinedTextField(
            value = uiState.veraidId,
            onValueChange = viewModel::onIdChanged,
            label = R.string.general_id,
            hintText = stringResource(id = R.string.new_contact_id_hint),
            isError = errorCaption != null,
            isEnabled = uiState.isVeraIdInputEnabled,
        ) {
            if (errorCaption != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(id = errorCaption.message),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        if (uiState.isSentRequestAgainHintVisible) {
            Spacer(
                modifier = Modifier.height(8.dp),
            )
            LetroInfoView {
                Text(
                    text = stringResource(id = R.string.pair_request_was_already_sent_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Spacer(
            modifier = Modifier.height(24.dp),
        )
        LetroOutlinedTextField(
            value = uiState.alias ?: "",
            onValueChange = viewModel::onAliasChanged,
            label = R.string.onboarding_pair_with_people_alias,
            hintText = stringResource(id = R.string.new_contact_alias_hint),
        )
        Spacer(
            modifier = Modifier.height(32.dp),
        )
        LetroButtonMaxWidthFilled(
            text = stringResource(id = uiState.manageContactTexts.button),
            onClick = { viewModel.onUpdateContactButtonClick() },
            isEnabled = uiState.isActionButtonEnabled,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UseExistingAccountPreview() {
    LetroTheme {
        ManageContactScreen(
            onBackClick = {},
            onEditContactCompleted = {},
            viewModel = hiltViewModel(),
        )
    }
}
