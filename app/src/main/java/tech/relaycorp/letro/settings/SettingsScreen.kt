package tech.relaycorp.letro.settings

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.relaycorp.letro.R
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.model.AccountStatus
import tech.relaycorp.letro.ui.common.LetroActionBarWithBackAction
import tech.relaycorp.letro.ui.common.LetroAvatar
import tech.relaycorp.letro.ui.common.LetroInfoView
import tech.relaycorp.letro.ui.common.LetroTransparentButton
import tech.relaycorp.letro.ui.theme.LetroColor
import tech.relaycorp.letro.ui.theme.TitleMediumProminent
import tech.relaycorp.letro.utils.ext.applyIf

@Composable
fun SettingsScreen(
    onNotificationsClick: () -> Unit,
    onTermsAndConditionsClick: () -> Unit,
    onBackClick: () -> Unit,
    openRegistrationScreen: () -> Unit,
    openAccountLinkingScreen: () -> Unit,
    openAccountManageScreen: (Account) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
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
        Spacer(modifier = Modifier.height(12.dp))
        AccountsBlock(
            accounts = uiState.accounts,
            onAddAccountClick = { isFreeAccountsLimitReached ->
                if (isFreeAccountsLimitReached) {
                    openAccountLinkingScreen()
                } else {
                    openRegistrationScreen()
                }
            },
            onAccountClick = { openAccountManageScreen(it) },
            accountsInfoView = uiState.infoViewType,
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
        Spacer(modifier = Modifier.height(32.dp))
        Image(
            painter = painterResource(id = R.drawable.powered_by_awala),
            contentDescription = stringResource(id = R.string.powered_by_awala),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun AccountsBlock(
    accounts: List<Account>,
    accountsInfoView: SettingsAccountsInfoViewType,
    onAccountClick: (Account) -> Unit,
    onAddAccountClick: (Boolean) -> Unit,
) {
    val isFreeAccountsLimitReached = accountsInfoView is SettingsAccountsInfoViewType.Warning
    SettingsBlock(
        title = stringResource(id = R.string.manage_accounts),
    ) {
        for (i in accounts.indices) {
            Account(
                accountId = accounts[i].accountId,
                avatarFilePath = accounts[i].avatarPath,
                accountStatus = accounts[i].status,
                onClick = { onAccountClick(accounts[i]) },
            )
        }
        LetroInfoView(
            backgroundColor = LetroColor.SurfaceContainer,
            infoIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            iconModifier = Modifier.size(20.dp),
            modifier = Modifier
                .padding(
                    horizontal = ELEMENT_HORIZONTAL_PADDING,
                    vertical = 8.dp,
                ),
        ) {
            Column {
                if (accountsInfoView is SettingsAccountsInfoViewType.Warning) {
                    Text(
                        text = stringResource(id = R.string.you_hit_the_limit_of_accounts, accountsInfoView.maxFreeAccounts),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = when (accountsInfoView) {
                        is SettingsAccountsInfoViewType.Warning -> stringResource(id = R.string.until_you_delete_a_free_account)
                        is SettingsAccountsInfoViewType.Info -> stringResource(id = R.string.you_using_x_of_y_free_accounts, accountsInfoView.createdAccounts, accountsInfoView.maxFreeAccounts)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        LetroTransparentButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ELEMENT_HORIZONTAL_PADDING,
                    vertical = ELEMENT_VERTICAL_PADDING,
                ),
            text = stringResource(id = if (isFreeAccountsLimitReached) R.string.general_use_existing_account else R.string.add_another_account),
            icon = R.drawable.ic_plus_18,
            onClick = { onAddAccountClick(isFreeAccountsLimitReached) },
        )
    }
}

@Composable
private fun Account(
    accountId: String,
    avatarFilePath: String?,
    @AccountStatus accountStatus: Int,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                horizontal = ELEMENT_HORIZONTAL_PADDING,
                vertical = ELEMENT_VERTICAL_PADDING,
            ),
    ) {
        LetroAvatar(
            modifier = Modifier
                .clip(CircleShape)
                .size(40.dp),
            filePath = avatarFilePath,
        )
        Spacer(modifier = Modifier.width(16.dp))
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
            if (accountStatus == AccountStatus.ERROR_CREATION || accountStatus == AccountStatus.ERROR_LINKING) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = if (accountStatus == AccountStatus.ERROR_LINKING) R.string.account_linking_failed else R.string.account_creation_failed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            painter = painterResource(id = R.drawable.chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
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

@Preview(showBackground = true)
@Composable
private fun Account_Preview() {
    Account(
        accountId = "test@account.id",
        accountStatus = AccountStatus.CREATED,
        avatarFilePath = null,
        onClick = {},
    )
}

private val ELEMENT_HORIZONTAL_PADDING = 16.dp
private val ELEMENT_VERTICAL_PADDING = 12.dp
