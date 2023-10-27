@file:Suppress("NAME_SHADOWING")

package tech.relaycorp.letro.contacts.ui

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import tech.relaycorp.letro.R
import tech.relaycorp.letro.base.utils.SnackbarString
import tech.relaycorp.letro.contacts.ManageContactScreenContent
import tech.relaycorp.letro.contacts.ManageContactViewModel
import tech.relaycorp.letro.contacts.PairWithOthersUiState
import tech.relaycorp.letro.ui.actionTaking.ActionTakingScreen
import tech.relaycorp.letro.ui.actionTaking.ActionTakingScreenUIStateModel
import tech.relaycorp.letro.ui.common.LetroActionBarWithBackAction
import tech.relaycorp.letro.ui.common.LetroButtonMaxWidthFilled
import tech.relaycorp.letro.ui.common.LetroInfoView
import tech.relaycorp.letro.ui.common.LetroOutlinedTextField
import tech.relaycorp.letro.ui.theme.HorizontalScreenPadding
import tech.relaycorp.letro.utils.permission.rememberNotificationPermissionStateCompat

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ManageContactScreen(
    onBackClick: () -> Unit,
    onEditContactCompleted: (String) -> Unit,
    showGoToSettingsPermissionSnackbar: () -> Unit,
    showSnackbar: (SnackbarString) -> Unit,
    viewModel: ManageContactViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val notificationsPermissionState = rememberNotificationPermissionStateCompat(
        onPermissionResult = {
            viewModel.onNotificationPermissionResult(it)
        },
    )

    LaunchedEffect(Unit) {
        viewModel.showSnackbar.collect {
            showSnackbar(it)
        }
    }

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

    LaunchedEffect(Unit) {
        viewModel.showPermissionGoToSettingsSignal.collect {
            showGoToSettingsPermissionSnackbar()
        }
    }

    AnimatedContent(targetState = uiState.content, label = "ManageContactScreen") { content ->
        when (content) {
            ManageContactScreenContent.MANAGE_CONTACT -> {
                ManageContactView(
                    onBackClick,
                    uiState,
                    viewModel,
                )
            }
            ManageContactScreenContent.REQUEST_SENT -> {
                if (!notificationsPermissionState.status.isGranted && uiState.showNotificationPermissionRequestIfNoPermission) {
                    ActionTakingScreen(
                        actionTakingScreenUIStateModel = ActionTakingScreenUIStateModel.PairingRequestSentWithPermissionRequest(
                            onRequestPermissionClick = {
                                notificationsPermissionState.launchPermissionRequest()
                            },
                            onSkipClicked = {
                                viewModel.onGotItClick()
                            },
                            boldPartOfMessage = uiState.accountId,
                        ),
                    )
                } else {
                    ActionTakingScreen(
                        actionTakingScreenUIStateModel = ActionTakingScreenUIStateModel.PairingRequestSent(
                            boldPartOfMessage = uiState.accountId,
                            onGotItClicked = {
                                viewModel.onGotItClick()
                            },
                        ),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ManageContactView(
    onBackClick: () -> Unit,
    uiState: PairWithOthersUiState,
    viewModel: ManageContactViewModel,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val errorCaption = uiState.pairingErrorCaption

    Column(
        modifier = Modifier.padding(
            horizontal = HorizontalScreenPadding,
        ),
    ) {
        LetroActionBarWithBackAction(
            title = stringResource(uiState.manageContactTexts.title),
            onBackClick = { onBackClick() },
        )
        Spacer(
            modifier = Modifier.height(8.dp),
        )
        LetroOutlinedTextField(
            value = uiState.accountId,
            onValueChange = viewModel::onIdChanged,
            label = R.string.general_id,
            hintText = stringResource(id = R.string.new_contact_id_hint),
            isError = errorCaption != null,
            isEnabled = uiState.isVeraIdInputEnabled,
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            val caption = when {
                errorCaption != null -> CaptionType.Error(errorCaption.message)
                !uiState.isActionButtonEnabled -> CaptionType.Hint
                else -> CaptionType.None
            }
            AnimatedContent(targetState = caption, label = "ManageContactScreenHint") { caption ->
                when (caption) {
                    is CaptionType.Error -> {
                        Text(
                            text = stringResource(id = caption.text),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    is CaptionType.Hint -> {
                        Text(
                            text = stringResource(id = R.string.pair_request_invalid_id),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    else -> {}
                }
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
            onClick = {
                viewModel.onUpdateContactButtonClick()
                keyboardController?.hide()
            },
            isEnabled = uiState.isActionButtonEnabled,
            withProgressIndicator = uiState.isSendingMessage,
            modifier = Modifier
                .height(SubmitButtonHeight),
        )
    }
}

private val SubmitButtonHeight = 48.dp

private sealed interface CaptionType {
    data class Error(@StringRes val text: Int) : CaptionType
    data object Hint : CaptionType
    data object None : CaptionType
}
