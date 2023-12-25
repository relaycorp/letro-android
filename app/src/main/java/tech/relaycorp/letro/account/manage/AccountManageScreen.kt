package tech.relaycorp.letro.account.manage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.relaycorp.letro.R
import tech.relaycorp.letro.base.utils.SnackbarString
import tech.relaycorp.letro.ui.common.LetroActionBarWithBackAction
import tech.relaycorp.letro.ui.common.LetroAvatar
import tech.relaycorp.letro.ui.common.LetroTransparentButton
import tech.relaycorp.letro.ui.common.text.BoldText
import tech.relaycorp.letro.ui.theme.LabelLargeProminent
import tech.relaycorp.letro.ui.theme.TitleMediumProminent

@Composable
fun AccountManageScreen(
    onBackClick: () -> Unit,
    onAccountDeleted: () -> Unit,
    showSnackbar: (SnackbarString) -> Unit,
    viewModel: AccountManageViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsState()
    val confirmAccountDeleteDialogState by viewModel.deleteAccountConfirmationDialog.collectAsState()

    val confirmAccountDeleteDialog = confirmAccountDeleteDialogState

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {
            viewModel.onAvatarPicked(it?.toString())
        },
    )

    LaunchedEffect(Unit) {
        viewModel.showSnackbar.collect {
            showSnackbar(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                bottom = 24.dp,
                start = 15.dp,
                end = 15.dp,
            )
            .verticalScroll(rememberScrollState()),
    ) {
        LetroActionBarWithBackAction(
            title = stringResource(id = R.string.account),
            onBackClick = { onBackClick() },
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProfilePhotoBlock(
            currentAvatarFilePath = uiState.value.account?.avatarPath,
            onPickPhotoClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            onDeletePhotoClick = { viewModel.onAvatarDeleteClick() },
        )

        Spacer(modifier = Modifier.height(20.dp))

        IdBlock(accountId = uiState.value.account?.accountId ?: "")

        Spacer(modifier = Modifier.height(20.dp))

        DeleteAccountButton(
            onClick = { viewModel.onAccountDeleteClick() },
        )

        if (confirmAccountDeleteDialog.isShown && confirmAccountDeleteDialog.account != null) {
            DeleteAccountDialog(
                accountId = confirmAccountDeleteDialog.account.accountId,
                onDismissRequest = { viewModel.onConfirmAccountDeleteDialogDismissed() },
                onConfirmClick = {
                    viewModel.onConfirmAccountDeleteClick(confirmAccountDeleteDialog.account)
                    onAccountDeleted()
                },
            )
        }
    }
}

@Composable
private fun ProfilePhotoBlock(
    currentAvatarFilePath: String?,
    onPickPhotoClick: () -> Unit,
    onDeletePhotoClick: () -> Unit,
) {
    AccountManageBlock(
        title = stringResource(id = R.string.profile_photo),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LetroAvatar(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(170.dp)
                    .align(Alignment.CenterHorizontally),
                filePath = currentAvatarFilePath,
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (currentAvatarFilePath == null) {
                LetroTransparentButton(
                    modifier = Modifier
                        .padding(
                            vertical = 10.dp,
                            horizontal = 16.dp,
                        )
                        .align(Alignment.CenterHorizontally),
                    text = stringResource(id = R.string.set_profile_photo),
                    icon = R.drawable.ic_edit_18,
                    onClick = onPickPhotoClick,
                )
            } else {
                Row {
                    LetroTransparentButton(
                        modifier = Modifier
                            .padding(
                                vertical = 10.dp,
                                horizontal = 16.dp,
                            ),
                        text = stringResource(id = R.string.change),
                        icon = R.drawable.ic_edit_18,
                        onClick = onPickPhotoClick,
                    )
                    LetroTransparentButton(
                        modifier = Modifier
                            .padding(
                                vertical = 10.dp,
                                horizontal = 16.dp,
                            ),
                        text = stringResource(id = R.string.remove),
                        icon = R.drawable.ic_delete_18,
                        onClick = onDeletePhotoClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun IdBlock(
    accountId: String,
) {
    AccountManageBlock(
        title = stringResource(id = R.string.general_id),
    ) {
        Text(
            text = accountId,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DeleteAccountButton(
    onClick: () -> Unit,
) {
    AccountManageBlock(
        modifier = Modifier.clickable { onClick() },
    ) {
        LetroTransparentButton(
            modifier = Modifier
                .padding(vertical = 6.dp),
            text = stringResource(id = R.string.delete_account),
            icon = R.drawable.ic_delete,
            color = MaterialTheme.colorScheme.error,
            onClick = onClick,
        )
    }
}

@Composable
private fun AccountManageBlock(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp,
            ),
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.TitleMediumProminent,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        content()
    }
}

@Composable
private fun DeleteAccountDialog(
    accountId: String,
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            onDismissRequest()
        },
        title = {
            Text(
                text = stringResource(id = R.string.delete_account),
                style = MaterialTheme.typography.TitleMediumProminent,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            BoldText(
                fullText = stringResource(id = R.string.delete_account_dialog_text, accountId),
                boldParts = listOf(accountId),
                textStyle = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmClick()
                },
            ) {
                Text(
                    text = stringResource(id = R.string.delete),
                    style = MaterialTheme.typography.LabelLargeProminent,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                },
            ) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    style = MaterialTheme.typography.LabelLargeProminent,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}
