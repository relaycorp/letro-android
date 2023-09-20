package tech.relaycorp.letro.home.tabs

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.R
import tech.relaycorp.letro.home.HomeViewModel
import tech.relaycorp.letro.ui.theme.LetroColor

@SuppressLint
@Composable
fun LetroTabs(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    val tabTitles = listOf(
        stringResource(id = R.string.top_bar_tab_conversations),
        stringResource(id = R.string.top_bar_tab_contacts),
        stringResource(id = R.string.top_bar_tab_notifications),
    )
    ScrollableTabRow(
        selectedTabIndex = uiState.currentTab,
        containerColor = LetroColor.SurfaceContainerHigh,
        contentColor = LetroColor.OnSurfaceContainerHigh,
        edgePadding = 9.dp,
        indicator = {
            TabRowDefaults.Indicator(
                color = LetroColor.OnSurfaceContainerHigh,
                modifier = Modifier.tabIndicatorOffset(it[uiState.currentTab]),
            )
        },
        modifier = modifier,
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = uiState.currentTab == index,
                onClick = {
                    viewModel.onTabClick(index)
                },
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = LetroColor.OnSurfaceContainerHigh,
                        maxLines = 1,
                    )
                },
                selectedContentColor = LetroColor.OnSurfaceContainerHigh,
                unselectedContentColor = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 0.dp)
                    .alpha(if (uiState.currentTab == index) 1f else 0.6f)
            )
        }
    }
}