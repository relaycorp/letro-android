package tech.relaycorp.letro.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.R
import tech.relaycorp.letro.account.model.AccountStatus
import tech.relaycorp.letro.ui.common.shimmer.Shimmer
import tech.relaycorp.letro.ui.theme.LetroColor
import tech.relaycorp.letro.ui.theme.LetroTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetroTopBar(
    modifier: Modifier = Modifier,
    config: LetroTopBarConfiguration,
) {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        title = {
            when (config) {
                is LetroTopBarConfiguration.Common -> CommonTopBarTitle(config)
                is LetroTopBarConfiguration.ConversationsEditMode -> ConversationsEditTopBarTitle(config)
            }
        },
        actions = {
            when (config) {
                is LetroTopBarConfiguration.Common -> {
                    IconButton(onClick = config.onSettingsClicked) {
                        Icon(
                            painterResource(id = R.drawable.settings),
                            contentDescription = stringResource(id = R.string.settings),
                            tint = LetroColor.OnSurfaceContainerHigh,
                        )
                    }
                }
                is LetroTopBarConfiguration.ConversationsEditMode -> {
                    Row {
                        IconButton(onClick = config.onArchiveClick) {
                            Icon(
                                painter = painterResource(id = if (config.isArchiveFolder) R.drawable.ic_unarchive_24 else R.drawable.ic_archive_24),
                                contentDescription = stringResource(id = R.string.archive),
                                tint = LetroColor.OnSurfaceContainerHigh,
                            )
                        }
                        IconButton(onClick = config.onDeleteClick) {
                            Icon(
                                painterResource(id = R.drawable.ic_trash),
                                contentDescription = stringResource(id = R.string.settings),
                                tint = LetroColor.OnSurfaceContainerHigh,
                            )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (config is LetroTopBarConfiguration.ConversationsEditMode) LetroColor.SurfaceContainerMedium else LetroColor.SurfaceContainerHigh,
        ),
    )
}

@Composable
private fun CommonTopBarTitle(
    config: LetroTopBarConfiguration.Common,
) {
    Row(
        modifier = Modifier
            .clickable { config.onChangeAccountClicked() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (config.showAccountIdAsShimmer) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Shimmer(
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(72.dp, 20.dp),
                )
                Text(
                    text = "@${config.domain}",
                    style = MaterialTheme.typography.titleMedium,
                    color = LetroColor.OnSurfaceContainerHigh,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier.weight(
                        weight = 1f,
                        fill = false,
                    ),
                )
            }
        } else {
            LetroAvatar(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(26.dp),
                filePath = config.avatarFilePath,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = config.accountVeraId,
                style = MaterialTheme.typography.titleMedium,
                color = LetroColor.OnSurfaceContainerHigh,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.weight(
                    weight = 1f,
                    fill = false,
                ),
            )
        }
        when (config.accountStatus) {
            AccountStatus.CREATION_WAITING, AccountStatus.LINKING_WAITING -> {
                Spacer(modifier = Modifier.width(6.dp))
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(20.dp, 20.dp),
                    color = LetroColor.OnSurfaceContainerHigh,
                    strokeWidth = 2.dp,
                )
            }
            AccountStatus.ERROR_LINKING, AccountStatus.ERROR_CREATION -> {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_error_24),
                    contentDescription = stringResource(id = if (config.accountStatus == AccountStatus.ERROR_LINKING) R.string.account_linking_failed else R.string.account_creation_failed),
                    tint = LetroColor.OnSurfaceContainerHigh,
                )
            }
            else -> {}
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_down_24),
            contentDescription = stringResource(id = R.string.top_bar_change_account),
            tint = LetroColor.OnSurfaceContainerHigh,
        )
    }
}

@Composable
private fun ConversationsEditTopBarTitle(
    config: LetroTopBarConfiguration.ConversationsEditMode,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_back_24),
            contentDescription = stringResource(id = R.string.general_navigate_back),
            tint = LetroColor.OnSurfaceContainerHigh,
            modifier = Modifier.clickable {
                config.onCancelClick()
            },
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = config.selectedConversations.toString(), style = MaterialTheme.typography.titleLarge, color = LetroColor.OnSurfaceContainerHigh)
    }
}

@Preview
@Composable
private fun LetroTopBarCommon_Preview() {
    LetroTheme {
        LetroTopBar(
            config = LetroTopBarConfiguration.Common(
                accountVeraId = "account@vera.id",
                accountStatus = AccountStatus.CREATED,
                avatarFilePath = null,
                domain = "",
                showAccountIdAsShimmer = false,
                onChangeAccountClicked = { /*TODO*/ },
            ) {},
        )
    }
}

@Preview
@Composable
private fun LetroTopBarConversationsEdit_Preview() {
    LetroTheme {
        LetroTopBar(
            config = LetroTopBarConfiguration.ConversationsEditMode(
                selectedConversations = 2,
                isArchiveFolder = false,
                {},
                {},
                {},
            ),
        )
    }
}

sealed class LetroTopBarConfiguration {
    data class Common(
        val accountVeraId: String,
        @AccountStatus val accountStatus: Int,
        val avatarFilePath: String?,
        val domain: String,
        val showAccountIdAsShimmer: Boolean,
        val onChangeAccountClicked: () -> Unit,
        val onSettingsClicked: () -> Unit,
    ) : LetroTopBarConfiguration()

    data class ConversationsEditMode(
        val selectedConversations: Int,
        val isArchiveFolder: Boolean,
        val onCancelClick: () -> Unit,
        val onArchiveClick: () -> Unit,
        val onDeleteClick: () -> Unit,
    ) : LetroTopBarConfiguration()
}
