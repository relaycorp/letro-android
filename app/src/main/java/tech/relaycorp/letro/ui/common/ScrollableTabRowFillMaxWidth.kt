package tech.relaycorp.letro.ui.common

import android.util.Log
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Tab row that fills max space if width of tabbar is less than width of a screen.
 */
@Composable
fun ScrollableTabRowFillMaxWidth(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = TabRowDefaults.containerColor,
    contentColor: Color = TabRowDefaults.contentColor,
    paddingBetweenTabs: Dp = 8.dp,
    edgePadding: Dp = 9.dp,
    indicator: @Composable (tabPositions: List<TabPosition>) -> Unit = @Composable { tabPositions ->
        TabRowDefaults.Indicator(
            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
        )
    },
    divider: @Composable () -> Unit = @Composable {
        Divider()
    },
    tabs: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor,
    ) {
        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()
        val scrollableTabData = remember(scrollState, coroutineScope) {
            ScrollableTabData(
                scrollState = scrollState,
                coroutineScope = coroutineScope,
            )
        }
        val screenWidth = LocalConfiguration.current.screenWidthDp
        SubcomposeLayout(
            Modifier
                .fillMaxWidth()
                .wrapContentSize(align = Alignment.CenterStart)
                .horizontalScroll(scrollState)
                .selectableGroup()
                .clipToBounds(),
        ) { constraints ->
            val screenWidthPx = screenWidth.dp.toPx().toInt()
            val minTabWidth = ScrollableTabRowMinimumTabWidth.roundToPx()
            val tabPaddingEndPx = paddingBetweenTabs.roundToPx()
            val edgePaddingPx = edgePadding.roundToPx()

            val tabMeasurables = subcompose(TabSlots.Tabs, tabs)

            val layoutHeight = tabMeasurables.fold(initial = 0) { curr, measurable ->
                maxOf(curr, measurable.maxIntrinsicHeight(Constraints.Infinity))
            }

            val tabConstraints = constraints.copy(
                minWidth = minTabWidth,
                minHeight = layoutHeight,
                maxHeight = layoutHeight,
            )
            val wholeViewWidth = tabMeasurables
                .foldIndexed(initial = edgePaddingPx * 2) { index, curr, measurable ->
                    curr + measurable.maxIntrinsicWidth(tabConstraints.maxHeight) + if (index == tabMeasurables.size - 1) 0 else tabPaddingEndPx
                }

            val tabsWithoutEndPaddingsWidth = tabMeasurables
                .fold(initial = edgePaddingPx * 2) { curr, measurable ->
                    curr + measurable.maxIntrinsicWidth(tabConstraints.maxHeight)
                }

            Log.d(TAG, "total view width: $wholeViewWidth, view without end paddings: $tabsWithoutEndPaddingsWidth, screen width: $screenWidthPx")
            val isNeedAdditionalPaddingToFillAllSpace = wholeViewWidth < screenWidthPx
            val paddingEndToFillAllSpace = if (isNeedAdditionalPaddingToFillAllSpace) (screenWidthPx - tabsWithoutEndPaddingsWidth) / (tabMeasurables.size - 1) else tabPaddingEndPx

            Log.d(TAG, "PreviousPadding: $tabPaddingEndPx, NewPadding: $paddingEndToFillAllSpace")

            val tabPlaceables = tabMeasurables
                .map { measurable ->
                    measurable.measure(tabConstraints)
                }

            val layoutWidth = tabPlaceables.foldIndexed(initial = edgePaddingPx * 2) { index, curr, placeable ->
                curr + placeable.width + if (index == tabMeasurables.size - 1) 0 else paddingEndToFillAllSpace
            }

            Log.d(TAG, "new layout width: $layoutWidth")

            // Position the children.
            layout(layoutWidth, layoutHeight) {
                // Place the tabs
                val tabPositions = mutableListOf<TabPosition>()
                var left = edgePaddingPx
                tabPlaceables.forEachIndexed { index, placeable ->
                    placeable.placeRelative(left, 0)
                    tabPositions.add(TabPosition(left = left.toDp(), width = placeable.width.toDp()))
                    left += placeable.width + if (index != tabPlaceables.size - 1) paddingEndToFillAllSpace else 0
                }

                // The divider is measured with its own height, and width equal to the total width
                // of the tab row, and then placed on top of the tabs.
                subcompose(TabSlots.Divider, divider).forEach {
                    val placeable = it.measure(
                        constraints.copy(
                            minHeight = 0,
                            minWidth = layoutWidth,
                            maxWidth = layoutWidth,
                        ),
                    )
                    placeable.placeRelative(0, layoutHeight - placeable.height)
                }

                // The indicator container is measured to fill the entire space occupied by the tab
                // row, and then placed on top of the divider.
                subcompose(TabSlots.Indicator) {
                    indicator(tabPositions)
                }.forEach {
                    it.measure(Constraints.fixed(layoutWidth, layoutHeight)).placeRelative(0, 0)
                }

                scrollableTabData.onLaidOut(
                    density = this@SubcomposeLayout,
                    edgeOffset = edgePaddingPx,
                    tabPositions = tabPositions,
                    selectedTab = selectedTabIndex,
                )
            }
        }
    }
}

private class ScrollableTabData(
    private val scrollState: ScrollState,
    private val coroutineScope: CoroutineScope,
) {
    private var selectedTab: Int? = null

    fun onLaidOut(
        density: Density,
        edgeOffset: Int,
        tabPositions: List<TabPosition>,
        selectedTab: Int,
    ) {
        // Animate if the new tab is different from the old tab, or this is called for the first
        // time (i.e selectedTab is `null`).
        if (this.selectedTab != selectedTab) {
            this.selectedTab = selectedTab
            tabPositions.getOrNull(selectedTab)?.let {
                // Scrolls to the tab with [tabPosition], trying to place it in the center of the
                // screen or as close to the center as possible.
                val calculatedOffset = it.calculateTabOffset(density, edgeOffset, tabPositions)
                if (scrollState.value != calculatedOffset) {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(
                            calculatedOffset,
                            animationSpec = ScrollableTabRowScrollSpec,
                        )
                    }
                }
            }
        }
    }

    /**
     * @return the offset required to horizontally center the tab inside this TabRow.
     * If the tab is at the start / end, and there is not enough space to fully centre the tab, this
     * will just clamp to the min / max position given the max width.
     */
    private fun TabPosition.calculateTabOffset(
        density: Density,
        edgeOffset: Int,
        tabPositions: List<TabPosition>,
    ): Int = with(density) {
        val totalTabRowWidth = tabPositions.last().right.roundToPx() + edgeOffset
        val visibleWidth = totalTabRowWidth - scrollState.maxValue
        val tabOffset = left.roundToPx()
        val scrollerCenter = visibleWidth / 2
        val tabWidth = width.roundToPx()
        val centeredTabOffset = tabOffset - (scrollerCenter - tabWidth / 2)
        // How much space we have to scroll. If the visible width is <= to the total width, then
        // we have no space to scroll as everything is always visible.
        val availableSpace = (totalTabRowWidth - visibleWidth).coerceAtLeast(0)
        return centeredTabOffset.coerceIn(0, availableSpace)
    }
}

private val ScrollableTabRowScrollSpec: AnimationSpec<Float> = tween(
    durationMillis = 250,
    easing = FastOutSlowInEasing,
)

private enum class TabSlots {
    Tabs,
    Divider,
    Indicator,
}

private val ScrollableTabRowMinimumTabWidth = 76.dp

@Immutable
class TabPosition internal constructor(val left: Dp, val width: Dp) {
    val right: Dp get() = left + width

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TabPosition) return false

        if (left != other.left) return false
        if (width != other.width) return false

        return true
    }

    override fun hashCode(): Int {
        var result = left.hashCode()
        result = 31 * result + width.hashCode()
        return result
    }

    override fun toString(): String {
        return "TabPosition(left=$left, right=$right, width=$width)"
    }
}

fun Modifier.tabIndicatorOffset(
    currentTabPosition: TabPosition,
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "tabIndicatorOffset"
        value = currentTabPosition
    },
) {
    val currentTabWidth by animateDpAsState(
        targetValue = currentTabPosition.width,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
    )
    val indicatorOffset by animateDpAsState(
        targetValue = currentTabPosition.left,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
    )
    fillMaxWidth()
        .wrapContentSize(Alignment.BottomStart)
        .offset(x = indicatorOffset)
        .width(currentTabWidth)
}

private const val TAG = "ScrollableTabRow"
