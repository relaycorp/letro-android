package tech.relaycorp.letro.main.home.ui.tabs

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.R
import tech.relaycorp.letro.main.home.HomeViewModel
import tech.relaycorp.letro.ui.common.ScrollableTabRowFillMaxWidth
import tech.relaycorp.letro.ui.common.tabIndicatorOffset
import tech.relaycorp.letro.ui.theme.Elevation1
import tech.relaycorp.letro.ui.theme.LetroColor

@SuppressLint
@Composable
fun LetroTabs(
    viewModel: HomeViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    val tabTitles = listOf(
        stringResource(id = R.string.conversations),
        stringResource(id = R.string.contacts),
        stringResource(id = R.string.notifications),
    )
    val tabCounters = uiState.tabCounters

    ScrollableTabRowFillMaxWidth(
        selectedTabIndex = uiState.currentTab,
        containerColor = LetroColor.SurfaceContainerHigh,
        edgePadding = 9.dp,
        indicator = {
            Surface(
                shadowElevation = Elevation1,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TabIndicatorHeight)
                    .tabIndicatorOffset(it[uiState.currentTab]),
            ) {
                Box(
                    Modifier
                        .background(color = LetroColor.OnSurfaceContainerHigh)
                        .tabIndicatorOffset(it[uiState.currentTab])
                        .fillMaxWidth()
                        .height(TabIndicatorHeight),
                )
            }
        },
    ) {
        tabTitles.forEachIndexed { index, title ->
            CounterTab(
                selected = uiState.currentTab == index,
                onClick = {
                    viewModel.onTabClick(index)
                },
                text = title,
                badge = tabCounters[index],
                modifier = Modifier
                    .padding(horizontal = 0.dp)
                    .alpha(if (uiState.currentTab == index) 1f else 0.6f),
            )
        }
    }
}

@Composable
private fun CounterTab(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    badge: String? = null,
) {
    Tab(
        selected = selected,
        onClick = { onClick() },
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = LetroColor.OnSurfaceContainerHigh,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.width(4.dp))
                AnimatedContent(targetState = badge != null, label = "LetroTabsBadgeVisibility") { isBadgeVisible ->
                    if (isBadgeVisible && badge != null) {
                        TabBadge(text = badge)
                    }
                }
            }
        },
        selectedContentColor = LetroColor.OnSurfaceContainerHigh,
        unselectedContentColor = MaterialTheme.colorScheme.error,
        modifier = modifier,
    )
}

@Composable
private fun TabBadge(
    text: String,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(
                color = LetroColor.OnSurfaceContainerHigh,
                shape = CircleShape,
            )
            .size(
                height = 16.dp,
                width = if (text.length == 1) 16.dp else 21.dp,
            ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = LetroColor.SurfaceContainerHigh,
            maxLines = 1,
        )
    }
}

private val TabIndicatorHeight = 3.dp
