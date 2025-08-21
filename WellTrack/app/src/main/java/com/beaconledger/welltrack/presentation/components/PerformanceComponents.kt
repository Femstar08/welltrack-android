package com.beaconledger.welltrack.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue

// Optimized lazy loading components for better performance
@Composable
fun <T> OptimizedLazyColumn(
    items: List<T>,
    key: ((item: T) -> Any)? = null,
    contentType: (item: T) -> Any? = { null },
    itemContent: @Composable LazyItemScope.(item: T) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    header: (@Composable LazyItemScope.() -> Unit)? = null,
    footer: (@Composable LazyItemScope.() -> Unit)? = null
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled
    ) {
        header?.let {
            item(key = "header", contentType = "header") {
                it()
            }
        }
        
        items(
            items = items,
            key = key,
            contentType = contentType
        ) { item ->
            itemContent(item)
        }
        
        footer?.let {
            item(key = "footer", contentType = "footer") {
                it()
            }
        }
    }
}

// Responsive grid layout that adapts to screen size
@Composable
fun ResponsiveGrid(
    modifier: Modifier = Modifier,
    minItemWidth: androidx.compose.ui.unit.Dp = 160.dp,
    spacing: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable LazyGridScope.() -> Unit
) {
    val density = LocalDensity.current
    var screenWidth by remember { mutableStateOf(0.dp) }
    
    BoxWithConstraints(modifier = modifier) {
        screenWidth = maxWidth
        
        val itemWidthPx = with(density) { minItemWidth.toPx() }
        val spacingPx = with(density) { spacing.toPx() }
        val screenWidthPx = with(density) { screenWidth.toPx() }
        
        val columns = ((screenWidthPx + spacingPx) / (itemWidthPx + spacingPx)).toInt().coerceAtLeast(1)
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing),
            content = content
        )
    }
}

// Smooth page indicator for horizontal pagers
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SmoothPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    inactiveColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            Box(
                modifier = Modifier
                    .size(
                        width = if (isActive) 24.dp else 8.dp,
                        height = 8.dp
                    )
                    .graphicsLayer {
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        clip = true
                    }
                    .background(
                        color = if (isActive) activeColor else inactiveColor,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

// Parallax scrolling effect for enhanced visual appeal
@Composable
fun ParallaxBox(
    modifier: Modifier = Modifier,
    parallaxRatio: Float = 0.5f,
    content: @Composable BoxScope.() -> Unit
) {
    val scrollState = rememberLazyListState()
    val firstVisibleItemScrollOffset by remember {
        derivedStateOf { scrollState.firstVisibleItemScrollOffset }
    }
    
    Box(
        modifier = modifier.graphicsLayer {
            translationY = firstVisibleItemScrollOffset * parallaxRatio
        }
    ) {
        content()
    }
}

// Staggered grid for Pinterest-like layouts
@Composable
fun StaggeredVerticalGrid(
    columns: Int,
    modifier: Modifier = Modifier,
    content: @Composable LazyGridScope.() -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columns),
        modifier = modifier,
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

// Animated visibility for smooth transitions
@Composable
fun AnimatedVisibilityBox(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = fadeIn() + slideInVertically(),
    exit: ExitTransition = fadeOut() + slideOutVertically(),
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit,
        content = content
    )
}

// Swipe-to-refresh implementation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val pullRefreshState = rememberPullToRefreshState()
    
    Box(
        modifier = modifier.pullToRefresh(
            isRefreshing = isRefreshing,
            state = pullRefreshState,
            onRefresh = onRefresh
        )
    ) {
        content()
        
        if (pullRefreshState.isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )
        }
    }
}

// Skeleton loading placeholders
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                shape = shape
            )
    )
}

@Composable
fun SkeletonText(
    modifier: Modifier = Modifier,
    lines: Int = 1
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(lines) { index ->
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(
                        fraction = when {
                            lines == 1 -> 1f
                            index == lines - 1 -> 0.7f
                            else -> 1f
                        }
                    )
                    .height(16.dp)
            )
        }
    }
}

// Memory-efficient image loading placeholder
@Composable
fun ImagePlaceholder(
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.Image,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        )
    }
}