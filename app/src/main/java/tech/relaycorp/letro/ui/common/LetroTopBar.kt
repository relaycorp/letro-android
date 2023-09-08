package tech.relaycorp.letro.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.theme.HorizontalScreenPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetroTopBar(
    accountVeraId: String,
    isAccountCreated: Boolean,
    modifier: Modifier = Modifier,
    onChangeAccountClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            modifier = modifier,
            title = {
                Row(
                    modifier = Modifier
                        .clickable { onChangeAccountClicked() },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = accountVeraId,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.surface,
                    )
                    if (!isAccountCreated) {
                        Spacer(modifier = Modifier.width(6.dp))
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp, 20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            strokeWidth = 2.dp,
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_down),
                        contentDescription = stringResource(id = R.string.top_bar_change_account),
                        tint = MaterialTheme.colorScheme.surface
                    )
                }
//                    } else {
//                        Text(
//                            modifier = Modifier.fillMaxWidth(),
//                            text = stringResource(id = R.string.app_name),
//                            style = MaterialTheme.typography.titleMedium,
//                            textAlign = TextAlign.Center,
//                            color = if (currentRoute.isTopBarContainerColorPrimary) {
//                                MaterialTheme.colorScheme.onPrimary
//                            } else {
//                                MaterialTheme.colorScheme.onSurface
//                            },
//                        )
//                    }
                },
                actions = {
//                    if (currentRoute.showAccountNameAndActions) {
                        IconButton(onClick = onSettingsClicked) {
                            Icon(
                                painterResource(id = R.drawable.settings),
                                contentDescription = stringResource(id = R.string.top_bar_settings),
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
//                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
            )
//            if (currentRoute.showTabs) {
//                LetroTabs(
//                    tabIndex = tabIndex,
//                    updateTabIndex = updateTabIndex,
//                    navigateToHomeScreen = navigateToHomeScreen,
//                )
//            }
        }
}