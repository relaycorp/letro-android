package tech.relaycorp.letro.notification.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.R
import tech.relaycorp.letro.notification.NotificationsViewModel
import tech.relaycorp.letro.notification.converter.NotificationDateInfo
import tech.relaycorp.letro.notification.model.ExtendedNotification
import tech.relaycorp.letro.ui.theme.TitleMediumProminent
import tech.relaycorp.letro.utils.compose.DoOnLifecycleEvent
import tech.relaycorp.letro.utils.time.nowUTC

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
) {
    DoOnLifecycleEvent(
        onResume = { viewModel.onScreenResumed() },
    )
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        if (uiState.unreadNotifications.isNotEmpty()) {
            notificationsBlock(
                title = R.string.unread,
                notifications = uiState.unreadNotifications,
                onClick = { viewModel.onNotificationClick(it) },
            )
        }
        if (uiState.readNotifications.isNotEmpty()) {
            notificationsBlock(
                title = R.string.read,
                notifications = uiState.readNotifications,
                onClick = { viewModel.onNotificationClick(it) },
            )
        }
    }
}

private fun LazyListScope.notificationsBlock(
    @StringRes title: Int,
    notifications: List<ExtendedNotification>,
    onClick: (ExtendedNotification) -> Unit,
) {
    items(1) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = title),
            style = MaterialTheme.typography.TitleMediumProminent,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
        )
    }
    items(notifications) {
        Notification(
            upperText = stringResource(id = it.upperText),
            bottomText = it.bottomText,
            date = stringResource(
                id = it.date.stringRes,
                formatArgs = arrayOf(it.date.value),
            ),
            isRead = it.isRead,
            onClick = { onClick(it) },
        )
    }
}

@Composable
private fun Notification(
    upperText: String,
    bottomText: String,
    date: String,
    isRead: Boolean,
    onClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .clickable { onClick() }
            .background(if (isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)
            .padding(
                horizontal = 16.dp,
            ),
    ) {
        Row {
            Text(
                text = upperText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = bottomText,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Notifications_Preview() {
    LazyColumn {
        notificationsBlock(
            title = R.string.unread,
            notifications = listOf(
                ExtendedNotification(id = 0L, type = 0, upperText = R.string.you_re_now_connected_to, bottomText = "jamesbond@cuppa.uk", date = NotificationDateInfo(1L, R.string.notification_time_days, nowUTC()), ownerId = "", isRead = false),
            ),
            onClick = {},
        )
        notificationsBlock(
            title = R.string.read,
            notifications = listOf(
                ExtendedNotification(id = 2L, type = 0, upperText = R.string.you_re_now_connected_to, bottomText = "jamesbond@cuppa.uk", date = NotificationDateInfo(1L, R.string.notification_time_weeks, nowUTC()), ownerId = "", isRead = true),
            ),
            onClick = {},
        )
    }
}
