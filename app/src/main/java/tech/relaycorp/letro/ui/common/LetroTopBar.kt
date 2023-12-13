package tech.relaycorp.letro.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.R
import tech.relaycorp.letro.account.model.AccountStatus
import tech.relaycorp.letro.ui.common.shimmer.Shimmer
import tech.relaycorp.letro.ui.theme.LetroColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetroTopBar(
    accountVeraId: String,
    @AccountStatus accountStatus: Int,
    domain: String,
    showAccountIdAsShimmer: Boolean,
    modifier: Modifier = Modifier,
    onChangeAccountClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        TopAppBar(
            modifier = modifier,
            title = {
                Row(
                    modifier = Modifier
                        .clickable { onChangeAccountClicked() },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (showAccountIdAsShimmer) {
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
                                text = "@$domain",
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
                        Text(
                            text = accountVeraId,
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
                    when (accountStatus) {
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
                                contentDescription = stringResource(id = if (accountStatus == AccountStatus.ERROR_LINKING) R.string.account_linking_failed else R.string.account_creation_failed),
                                tint = LetroColor.OnSurfaceContainerHigh,
                            )
                        }
                        else -> {}
                    }
                    Spacer(
                        modifier = Modifier.width(6.dp),
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_down),
                        contentDescription = stringResource(id = R.string.top_bar_change_account),
                        tint = LetroColor.OnSurfaceContainerHigh,
                    )
                }
            },
            actions = {
                IconButton(onClick = onSettingsClicked) {
                    Icon(
                        painterResource(id = R.drawable.settings),
                        contentDescription = stringResource(id = R.string.settings),
                        tint = LetroColor.OnSurfaceContainerHigh,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = LetroColor.SurfaceContainerHigh,
            ),
        )
    }
}
