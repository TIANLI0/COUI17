// Copyright 2026, COUI17 contributors
// SPDX-License-Identifier: Apache-2.0

package io.github.tianli0.coui17.patterns

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.AppBarBlurOverlayDefaults
import io.github.suqi8.coui.kmp.basic.BindScrollToTop
import io.github.suqi8.coui.kmp.basic.COUIScrollBehavior
import io.github.suqi8.coui.kmp.basic.Card
import io.github.suqi8.coui.kmp.basic.CardDefaults
import io.github.suqi8.coui.kmp.basic.CardListPosition
import io.github.suqi8.coui.kmp.basic.HighlightedIconButton
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.Scaffold
import io.github.suqi8.coui.kmp.basic.ScrollBehavior
import io.github.suqi8.coui.kmp.basic.SmallTopAppBar
import io.github.suqi8.coui.kmp.basic.TopAppBar
import io.github.suqi8.coui.kmp.basic.couiTopAppBarActionItem
import io.github.suqi8.coui.kmp.blur.LocalBackdrop
import io.github.suqi8.coui.kmp.blur.isRuntimeShaderSupported
import io.github.suqi8.coui.kmp.blur.layerBackdrop
import io.github.suqi8.coui.kmp.blur.rememberLayerBackdrop
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.Back
import io.github.suqi8.coui.kmp.squircle.SquircleShape
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.suqi8.coui.kmp.utils.overScrollVertical
import io.github.suqi8.coui.kmp.utils.scrollEndHaptic

/** ColorOS 17 page metrics. Call sites pick a name, not a number. */
object PageDefaults {
    /** Margin between the screen edge and a group card. */
    val CardGutter = 16.dp

    /**
     * Side gutter of loose text on a page or in a panel (a note under a group, an intro, an empty
     * hint): the card margin plus the 16dp inside it, so the text lines up with `SmallTitle` and
     * with the titles of the rows in the cards around it.
     */
    val TextGutter = 32.dp

    /** Gap under a group card. */
    val GroupSpacing = 16.dp

    /**
     * Corner radius of a text field that sits on the page colour as a card of its own: the group
     * card's radius, so a field between two cards has the same corners as they do.
     */
    val FieldCornerRadius = CardDefaults.GroupCornerRadius

    /** How far the strong header material reaches below a small title bar while it fades out. */
    internal val HeaderFade = 16.dp
}

/** Space a floating bottom navigation bar occupies over page content (provided by the app shell). */
val LocalBottomBarSpace = compositionLocalOf { 0.dp }

/**
 * Height of the page's title bar (incl. the status bar) when the page is [Page]`(immersive = true)`:
 * the list then starts at the top of the screen and scrolls under the glass bar, so its first item
 * must start this far down ([pageListPadding] adds it). 0 on other pages.
 */
val LocalPageTopInset = compositionLocalOf { 0.dp }

/** The current page's collapsing-title scroll behavior (null outside a [Page]). */
val LocalPageScrollBehavior = compositionLocalOf<ScrollBehavior?> { null }

/** ColorOS 17 page background (group cards use `surfaceContainer` on top of it). */
@Composable
fun pageContainerColor(): Color = AppBarBlurOverlayDefaults.containerColor()

/**
 * Modifier for a page's main scrolling list: COUI overscroll, the end-of-list haptic, and nested
 * scroll into the collapsing large title.
 */
@Composable
fun Modifier.pageScroll(): Modifier {
    val behavior = LocalPageScrollBehavior.current
    return this
        .scrollEndHaptic()
        .overScrollVertical()
        .then(if (behavior != null) Modifier.nestedScroll(behavior.nestedScrollConnection) else Modifier)
        .fillMaxHeight()
}

/**
 * State for a page's main list, bound to the scroll-to-top controller of the page's `Scaffold`:
 * tapping the top app bar (and, on ColorOS, the status bar) scrolls it back to the top and
 * re-expands the collapsing title. Not for bottom-following lists (chat, terminal).
 */
@Composable
fun rememberPageListState(): LazyListState {
    val state = rememberLazyListState()
    BindScrollToTop(state)
    return state
}

/** [rememberPageListState] for a page body that is a plain `verticalScroll` column. */
@Composable
fun rememberPageScrollState(): ScrollState {
    val state = rememberScrollState()
    BindScrollToTop(state)
    return state
}

/**
 * Content padding for a page's main list: on an immersive page the first item clears the title bar
 * (the list itself runs under it), and the last card clears the floating navigation bar
 * ([LocalBottomBarSpace]) or the gesture bar.
 */
@Composable
fun pageListPadding(top: Dp = 12.dp): PaddingValues = PaddingValues(
    top = top + LocalPageTopInset.current,
    bottom = 24.dp + (LocalBottomBarSpace.current.takeIf { it > 0.dp } ?: navigationBarHeight()),
)

@Composable
private fun navigationBarHeight(): Dp = with(LocalDensity.current) { WindowInsets.navigationBars.getBottom(this).toDp() }

/**
 * A standard ColorOS 17 page: coui `Scaffold` on the page colour, a large collapsing `TopAppBar`
 * for top-level pages or a `SmallTopAppBar` with a back key for detail pages ([onBack] non-null),
 * and the content below the bar. The list inside uses [pageScroll], [pageListPadding] and
 * [rememberPageListState].
 *
 * The title bar is ColorOS 17's gradient glass (a progressive blur of the list under it) on
 * immersive pages; elsewhere there is nothing under the bar to blur and it is the page colour.
 *
 * @param immersive the body is one scrolling list (anything fixed above it belongs inside it as its
 *   first item). It then starts at the top of the screen and scrolls under the status bar and the
 *   frosted title bar; [pageListPadding] keeps its first item below the bar. Pages with fixed
 *   content above the list (tabs, chips, a composer) stay non-immersive.
 * @param ownBarMaterial the page paints the material behind the title bar itself (see
 *   [glassHeader]); the bar is then transparent.
 */
@Composable
fun Page(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    subtitle: String = "",
    actions: @Composable RowScope.() -> Unit = {},
    large: Boolean = onBack == null,
    floatingActionButton: @Composable () -> Unit = {},
    immersive: Boolean = false,
    ownBarMaterial: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val behavior = COUIScrollBehavior()
    val color = pageContainerColor()
    // What the title bar's glass blurs: this page's content, recorded as a layer. The bar is the
    // content's sibling in the scaffold, so it never samples itself.
    // A small bar over an immersive list gets the strong header material (glassHeader): coui's
    // gradient material is too thin to read a title over text in the dark theme. A large collapsing
    // bar keeps coui's material, which follows the collapse.
    val strongHeader = immersive && !large && !ownBarMaterial
    val transparentBar = ownBarMaterial || strongHeader
    val barBackdrop = if (immersive && !ownBarMaterial && isRuntimeShaderSupported()) {
        rememberLayerBackdrop {
            drawRect(color)
            drawContent()
        }
    } else {
        null
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = color,
        topBar = {
            CompositionLocalProvider(LocalBackdrop provides barBackdrop) {
                val nav: @Composable () -> Unit = {
                    if (onBack != null) BackArrow(onBack)
                }
                if (large) {
                    TopAppBar(
                        title = title,
                        subtitle = subtitle,
                        color = color,
                        scrollBehavior = behavior,
                        showDivider = false,
                        gradientGlass = true,
                        navigationIcon = nav,
                        actions = actions,
                    )
                } else {
                    SmallTopAppBar(
                        title = title,
                        subtitle = subtitle,
                        color = if (transparentBar) Color.Transparent else color,
                        scrollBehavior = if (transparentBar) null else behavior,
                        showDivider = false,
                        gradientGlass = !transparentBar,
                        navigationIcon = nav,
                        actions = actions,
                    )
                }
            }
        },
        floatingActionButton = floatingActionButton,
        // coui paints the bar's progressive-blur material only when told that the content really
        // runs under the bar.
        edgeToEdge = immersive,
    ) { inner ->
        val top = inner.calculateTopPadding()
        CompositionLocalProvider(
            LocalPageScrollBehavior provides behavior,
            LocalPageTopInset provides if (immersive) top else 0.dp,
            LocalOnPageColor provides true,
        ) {
            Box(Modifier.fillMaxSize()) {
                Column(
                    Modifier.fillMaxSize()
                        .then(if (barBackdrop != null) Modifier.layerBackdrop(barBackdrop) else Modifier)
                        .padding(top = if (immersive) 0.dp else top),
                    content = content,
                )
                if (strongHeader) {
                    // Behind the (transparent) title bar, over the list: full strength under the
                    // bar, gone a little below it. It takes no touches.
                    CompositionLocalProvider(LocalGlassBackdrop provides barBackdrop) {
                        val band = top + PageDefaults.HeaderFade
                        Box(Modifier.fillMaxWidth().height(band).glassHeader(color, top / band))
                    }
                }
            }
        }
    }
}

/** The back key of a detail page: the COUI17 glass round key of the top bar. */
@Composable
fun BackArrow(onBack: () -> Unit) {
    HighlightedIconButton(onClick = onBack, modifier = Modifier.couiTopAppBarActionItem()) {
        Icon(imageVector = COUIIcons.Back, contentDescription = stringResource(R.string.coui17_action_back))
    }
}

/** A 40dp COUI17 round toolbar key with the standard action spacing, for a [Page]'s `actions`. */
@Composable
fun ToolbarIcon(icon: ImageVector, contentDescription: String, onClick: () -> Unit, enabled: Boolean = true) {
    HighlightedIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.couiTopAppBarActionItem(),
    ) {
        Icon(icon, contentDescription = contentDescription, tint = COUITheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
    }
}

/**
 * A ColorOS group: one rounded card, [PageDefaults.CardGutter] from the edges, holding a column of
 * rows (`BasicComponent`, `ArrowPreference`, `SwitchPreference`, …). [bottom] is the gap under it.
 */
@Composable
fun GroupCard(modifier: Modifier = Modifier, bottom: Dp = PageDefaults.GroupSpacing, content: @Composable ColumnScope.() -> Unit) {
    Card(
        cornerRadius = CardDefaults.GroupCornerRadius,
        weight = CardDefaults.GroupWeight,
        modifier = modifier.fillMaxWidth().padding(horizontal = PageDefaults.CardGutter).padding(bottom = bottom),
    ) {
        CompositionLocalProvider(LocalOnPageColor provides false) { content() }
    }
}

/**
 * The outline of one row of a group card: the card squircle corners on the outer edges of the
 * group only. Clip a row with it before `clickable` / `combinedClickable`
 * (`Modifier.clip(groupRowShape(position))`), so the press feedback of the first and last rows
 * follows the corners of the card.
 */
fun groupRowShape(position: CardListPosition, radius: Dp = CardDefaults.GroupCornerRadius): Shape = when (position) {
    CardListPosition.Full -> SquircleShape(radius, weight = CardDefaults.GroupWeight)
    CardListPosition.Head -> SquircleShape(topStart = radius, topEnd = radius, bottomEnd = 0.dp, bottomStart = 0.dp, weight = CardDefaults.GroupWeight)
    CardListPosition.Tail -> SquircleShape(topStart = 0.dp, topEnd = 0.dp, bottomEnd = radius, bottomStart = radius, weight = CardDefaults.GroupWeight)
    else -> RectangleShape
}

/**
 * One row of a group card laid out as its own lazy-list item: the rows of a long list (a folder
 * with thousands of entries) read as one [GroupCard] without composing them all at once. Pass the
 * same [position] to the row inside (`cardListPosition`); the last row adds the gap under the group.
 */
@Composable
fun LazyGroupRow(
    position: CardListPosition,
    modifier: Modifier = Modifier,
    bottom: Dp = PageDefaults.GroupSpacing,
    content: @Composable () -> Unit,
) {
    val last = position == CardListPosition.Tail || position == CardListPosition.Full
    Box(
        modifier
            .fillMaxWidth()
            .padding(horizontal = PageDefaults.CardGutter)
            .padding(bottom = if (last) bottom else 0.dp)
            .clip(groupRowShape(position))
            .background(CardDefaults.defaultColors().color),
    ) {
        CompositionLocalProvider(LocalOnPageColor provides false) { content() }
    }
}
