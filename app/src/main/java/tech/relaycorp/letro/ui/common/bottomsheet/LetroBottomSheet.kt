package tech.relaycorp.letro.ui.common.bottomsheet

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.theme.LetroColor
import tech.relaycorp.letro.ui.theme.TitleSmallProminent
import tech.relaycorp.letro.utils.ext.applyIf
import androidx.compose.ui.res.painterResource as painterResource1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetroBottomSheet(
    onDismissRequest: () -> Unit,
    title: String? = null,
    content: @Composable () -> Unit,
) {
    ModalBottomSheet(
        containerColor = LetroColor.SurfaceContainerLow,
        onDismissRequest = {
            onDismissRequest()
        },
    ) {
        Column(
            modifier = Modifier
                .padding(
                    PaddingValues(
                        bottom = 44.dp,
                    ),
                ),
        ) {
            if (title != null) {
                BottomSheetTitle(title = title)
            }
            content()
        }
    }
}

data class BottomSheetAction(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    val action: () -> Unit,
    val isChosen: Boolean = false,
    val trailingText: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetroActionsBottomSheet(
    actions: List<BottomSheetAction>,
    onDismissRequest: () -> Unit,
    title: String? = null,
) {
    ModalBottomSheet(
        containerColor = LetroColor.SurfaceContainerLow,
        onDismissRequest = {
            onDismissRequest()
        },
    ) {
        ActionsBottomSheetContent(
            actions = actions,
            title = title,
        )
    }
}

@Composable
private fun ActionsBottomSheetContent(
    actions: List<BottomSheetAction>,
    title: String? = null,
) {
    Column(
        modifier = Modifier
            .padding(
                PaddingValues(
                    bottom = 44.dp,
                ),
            ),
    ) {
        if (title != null) {
            BottomSheetTitle(title = title)
        }
        ActionsContent(actions = actions)
    }
}

@Composable
private fun BottomSheetTitle(
    title: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.TitleSmallProminent,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
            ),
    )
    Spacer(
        modifier = Modifier.height(14.dp),
    )
    Divider(
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun ActionsContent(
    actions: List<BottomSheetAction>,
) {
    LazyColumn {
        items(actions) {
            BottomSheetActionView(
                icon = it.icon,
                title = it.title,
                onClick = it.action,
                isChosen = it.isChosen,
                trailingText = it.trailingText,
            )
        }
    }
}

@Composable
private fun BottomSheetActionView(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    onClick: () -> Unit,
    isChosen: Boolean,
    trailingText: String? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .applyIf(isChosen) {
                background(LetroColor.SurfaceContainer)
            }
            .clickable { onClick() }
            .padding(
                vertical = 14.dp,
                horizontal = 16.dp,
            ),
    ) {
        Icon(
            painter = painterResource1(id = icon),
            contentDescription = null,
            tint = LetroColor.OnSurfaceContainer,
        )
        Spacer(
            modifier = Modifier.width(16.dp),
        )
        Text(
            text = stringResource(id = title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (trailingText != null) {
            Spacer(modifier = Modifier.weight(1F))
            Text(
                text = trailingText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Actions_Preview() {
    Column {
        BottomSheetActionView(
            icon = R.drawable.inbox,
            title = R.string.inbox,
            onClick = { /*TODO*/ },
            isChosen = true,
            trailingText = "4",
        )
        BottomSheetActionView(
            icon = R.drawable.sent,
            title = R.string.sent,
            onClick = { /*TODO*/ },
            isChosen = false,
        )
    }
}
