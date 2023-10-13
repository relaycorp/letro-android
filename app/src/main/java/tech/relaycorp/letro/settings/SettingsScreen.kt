package tech.relaycorp.letro.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.relaycorp.letro.R
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.model.AccountStatus
import tech.relaycorp.letro.ui.common.LetroActionBarWithBackAction
import tech.relaycorp.letro.ui.common.text.BoldText
import tech.relaycorp.letro.ui.theme.LabelLargeProminent
import tech.relaycorp.letro.ui.theme.LetroColor
import tech.relaycorp.letro.ui.theme.TitleMediumProminent
import tech.relaycorp.letro.utils.ext.applyIf

@Composable
fun SettingsScreen(
    onNotificationsClick: () -> Unit,
    onTermsAndConditionsClick: () -> Unit,
    onBackClick: () -> Unit,
    onAddAccountClick: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val accounts by viewModel.accounts.collectAsState()
    val confirmAccountDeleteDialogState by viewModel.deleteAccountConfirmationDialog.collectAsState()

    val confirmAccountDeleteDialog = confirmAccountDeleteDialogState
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
            title = stringResource(id = R.string.settings),
            onBackClick = { onBackClick() },
        )
        Spacer(modifier = Modifier.height(8.dp))
        AccountsBlock(
            accounts = accounts,
            onAccountDeleteClick = {
                viewModel.onAccountDeleteClick(it)
            },
            onAddAccountClick = {
                onAddAccountClick()
            },
        )
        Spacer(modifier = Modifier.height(24.dp))
        NotificationsBlock(
            onNotificationsClick = onNotificationsClick,
        )
        Spacer(modifier = Modifier.height(24.dp))
        AboutLetroBlock(
            appVersion = viewModel.appVersion,
            onTermsAndConditionsClick = onTermsAndConditionsClick,
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
private fun AccountsBlock(
    accounts: List<Account>,
    onAddAccountClick: () -> Unit,
    onAccountDeleteClick: (Account) -> Unit,
) {
    SettingsBlock(
        title = stringResource(id = R.string.manage_accounts),
    ) {
        for (i in accounts.indices) {
            Account(
                accountId = accounts[i].accountId,
                isCreationError = accounts[i].status == AccountStatus.ERROR,
                onDeleteClick = { onAccountDeleteClick(accounts[i]) },
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAddAccountClick() }
                .padding(
                    horizontal = ELEMENT_HORIZONTAL_PADDING,
                    vertical = ELEMENT_VERTICAL_PADDING,
                ),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_plus_18),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.add_another_account),
                style = MaterialTheme.typography.LabelLargeProminent,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun Account(
    accountId: String,
    isCreationError: Boolean,
    onDeleteClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = ELEMENT_HORIZONTAL_PADDING,
                vertical = ELEMENT_VERTICAL_PADDING,
            ),
    ) {
        Column(
            modifier = Modifier
                .weight(1f),
        ) {
            Text(
                text = accountId,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (isCreationError) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.account_linking_failed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_delete),
            contentDescription = null,
            modifier = Modifier.clickable { onDeleteClick() },
        )
    }
}

@Composable
private fun NotificationsBlock(
    onNotificationsClick: () -> Unit,
) {
    SettingsBlock(
        title = stringResource(id = R.string.notifications),
    ) {
        SettingElement(
            icon = painterResource(id = R.drawable.ic_notifications_24),
            title = stringResource(id = R.string.notifications),
            onClick = { onNotificationsClick() },
        )
    }
}

@Composable
private fun AboutLetroBlock(
    appVersion: String,
    onTermsAndConditionsClick: () -> Unit,
) {
    SettingsBlock(
        title = stringResource(id = R.string.about_letro),
    ) {
        SettingElement(
            icon = painterResource(R.drawable.ic_info_24),
            title = stringResource(id = R.string.app_version_x, appVersion),
        )
        SettingElement(
            icon = painterResource(R.drawable.ic_shield_24),
            title = stringResource(id = R.string.terms_and_conditions),
            onClick = { onTermsAndConditionsClick() },
        )
    }
}

@Composable
private fun SettingsBlock(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.TitleMediumProminent,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
        ) {
            content()
        }
    }
}

@Composable
private fun SettingElement(
    icon: Painter,
    title: String,
    onClick: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .applyIf(onClick != null) {
                clickable { onClick?.invoke() }
            }
            .padding(
                horizontal = ELEMENT_HORIZONTAL_PADDING,
                vertical = ELEMENT_VERTICAL_PADDING,
            ),
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = LetroColor.OnSurfaceContainer,
        )
        Spacer(modifier = Modifier.width(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f),
        )
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
                style = MaterialTheme.typography.titleMedium,
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

private val ELEMENT_HORIZONTAL_PADDING = 16.dp
private val ELEMENT_VERTICAL_PADDING = 12.dp
