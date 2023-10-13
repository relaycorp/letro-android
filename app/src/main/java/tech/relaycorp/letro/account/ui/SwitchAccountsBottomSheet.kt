package tech.relaycorp.letro.account.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.R
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.model.AccountStatus
import tech.relaycorp.letro.ui.common.bottomsheet.LetroBottomSheet
import tech.relaycorp.letro.ui.theme.BodyMediumProminent

@Composable
fun SwitchAccountsBottomSheet(
    accounts: List<Account>,
    onAccountClick: (Account) -> Unit,
    onManageContactsClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    LetroBottomSheet(
        onDismissRequest = { onDismissRequest() },
        title = stringResource(id = R.string.switch_accounts),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState()),
        ) {
            for (i in accounts.indices) {
                Account(
                    account = accounts[i],
                    onClick = { onAccountClick(accounts[i]) },
                )
            }
            ManageContactsButton(onClick = onManageContactsClick)
        }
    }
}

@Composable
private fun Account(
    account: Account,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                horizontal = 16.dp,
                vertical = 14.dp,
            ),
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = account.accountId,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (account.status == AccountStatus.ERROR) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.account_linking_failed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            painter = painterResource(id = if (account.isCurrent) R.drawable.radio_button_selected else R.drawable.radio_button_unselected),
            contentDescription = stringResource(id = if (account.isCurrent) R.string.content_description_selected_item else R.string.content_description_unselected_item),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ManageContactsButton(
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                horizontal = 16.dp,
                vertical = 14.dp,
            ),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_settings_18),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = R.string.manage_accounts),
            style = MaterialTheme.typography.BodyMediumProminent,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
