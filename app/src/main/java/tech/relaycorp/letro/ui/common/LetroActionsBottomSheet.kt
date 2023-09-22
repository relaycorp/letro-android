package tech.relaycorp.letro.ui.common

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.ui.theme.LetroColor
import tech.relaycorp.letro.ui.theme.TitleSmallProminent
import androidx.compose.ui.res.painterResource as painterResource1

data class BottomSheetAction(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    val action: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetroActionsBottomSheet(
    title: String,
    actions: List<BottomSheetAction>,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = {
            onDismissRequest()
        },
    ) {
        BottomSheetContent(
            actions = actions,
            title = title,
        )
    }
}

@Composable
private fun BottomSheetContent(
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
        LazyColumn {
            items(actions) {
                BottomSheetActionView(it.icon, it.title, it.action)
            }
        }
    }
}

@Composable
private fun BottomSheetActionView(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
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
    }
}
