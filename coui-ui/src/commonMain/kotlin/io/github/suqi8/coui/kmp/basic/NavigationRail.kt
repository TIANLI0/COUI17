// Copyright 2025, compose-coui-ui contributors
// SPDX-License-Identifier: Apache-2.0
package io.github.suqi8.coui.kmp.basic

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.suqi8.coui.kmp.theme.COUITheme

/**
 * A [NavigationRail] that is suitable for wide screens.
 *
 * @param modifier The modifier to be applied to the [NavigationRail].
 * @param header The header of the [NavigationRail], usually a [FloatingActionButton] or a logo.
 * @param color The color of the [NavigationRail].
 * @param showDivider Whether to show the divider line between the [NavigationRail] and the content.
 * @param defaultWindowInsetsPadding whether to apply default window insets padding to the [NavigationRail].
 * @param minWidth The minimum width of the [NavigationRail].
 * @param mode The mode for displaying items in the [NavigationRail]. It can show icons, text or both.
 * @param content The content of the [NavigationRail], usually [NavigationRailItem]s.
 */
@Composable
fun NavigationRail(
    modifier: Modifier = Modifier,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    color: Color = COUITheme.colorScheme.surface,
    showDivider: Boolean = true,
    defaultWindowInsetsPadding: Boolean = true,
    minWidth: Dp = NavigationRailDefaults.MinWidth,
    mode: NavigationRailDisplayMode = NavigationRailDisplayMode.IconAndText,
    content: @Composable ColumnScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxHeight()
            .then(
                if (defaultWindowInsetsPadding) {
                    Modifier
                        .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Vertical))
                        .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Start))
                        .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Start))
                } else {
                    Modifier
                },
            )
            .background(color),
    ) {
        Column(
            modifier = Modifier
                .width(minWidth)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(top = NavigationRailDefaults.TopPadding, bottom = NavigationRailDefaults.BottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            if (header != null) {
                header()
                Spacer(modifier = Modifier.height(NavigationRailDefaults.HeaderSpacing))
            }
            CompositionLocalProvider(LocalNavigationRailDisplayMode provides mode) {
                content()
            }
        }
        if (showDivider) {
            VerticalDivider()
        }
    }
}

/**
 * A [NavigationRailItem] that is suitable for [NavigationRail].
 *
 * @param selected Whether the item is selected.
 * @param onClick The callback when the item is clicked.
 * @param icon The icon of the item.
 * @param label The label of the item.
 * @param modifier The modifier to be applied to the [NavigationRailItem].
 * @param enabled Whether the item is enabled.
 */
@Composable
fun NavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // COUI's icon and label color selectors resolve disabled, pressed, selected, then idle.
    val scheme = COUITheme.colorScheme
    val tint = when {
        !enabled -> scheme.disabledOnSurface
        isPressed -> scheme.onSurface
        selected -> scheme.onSurface
        else -> scheme.onSurfaceSecondary
    }
    val mode = LocalNavigationRailDisplayMode.current
    val showIcon = mode != NavigationRailDisplayMode.TextOnly
    val showLabel = when (mode) {
        NavigationRailDisplayMode.IconOnly -> false
        NavigationRailDisplayMode.IconWithSelectedLabel -> selected
        else -> true
    }
    val textOnly = mode == NavigationRailDisplayMode.TextOnly

    Column(
        modifier = modifier
            .padding(horizontal = NavigationRailDefaults.ItemMarginHorizontal)
            .widthIn(max = NavigationRailDefaults.ItemWidth)
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null,
            )
            .padding(vertical = NavigationRailDefaults.ItemVerticalPadding)
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (showIcon) {
            Image(
                modifier = Modifier.size(NavigationRailDefaults.IconSize),
                imageVector = icon,
                contentDescription = if (showLabel) null else label,
                colorFilter = ColorFilter.tint(tint),
            )
        }
        if (showIcon && showLabel) {
            Spacer(modifier = Modifier.height(NavigationRailDefaults.IconTextSpacing))
        }
        if (showLabel) {
            Text(
                modifier = if (textOnly) Modifier.padding(vertical = NavigationRailDefaults.TextOnlyVerticalPadding) else Modifier,
                text = label,
                color = tint,
                textAlign = TextAlign.Center,
                fontSize = if (textOnly) NavigationRailDefaults.TextOnlyFontSize else NavigationRailDefaults.LabelFontSize,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Contains default values used by [NavigationRail] and [NavigationRailItem]. */
object NavigationRailDefaults {
    /** The default minimum width of the [NavigationRail] (COUI coui_navigation_rail_min_width). */
    val MinWidth = 72.dp

    /** The original symmetric padding, retained for callers that share it across layouts. */
    val VerticalPadding = 24.dp

    internal val TopPadding = 4.dp
    internal val BottomPadding = 16.dp
    internal val ItemWidth = 64.dp
    internal val ItemMarginHorizontal = 4.dp

    /** The default spacing after the header. */
    val HeaderSpacing = 24.dp

    /** The default icon size (COUI coui_side_navigation_icon_size). */
    val IconSize = 24.dp

    /** The default spacing between icon and text (COUI coui_navigation_rail_text_margin_top). */
    val IconTextSpacing = 2.dp

    /** The default vertical padding for each item. */
    val ItemVerticalPadding = 12.dp

    /** The default label font size (COUI coui_navigation_rail_item_text_size). */
    val LabelFontSize = 10.sp

    /** The font size in [NavigationRailDisplayMode.TextOnly] mode. */
    val TextOnlyFontSize = 14.sp

    /** The vertical padding in [NavigationRailDisplayMode.TextOnly] mode. */
    val TextOnlyVerticalPadding = 4.dp

    /** The original pressed alpha for selected items, retained for custom rail content. */
    val SelectedPressedAlpha = 0.5f

    /** The original pressed alpha for unselected items, retained for custom rail content. */
    val UnselectedPressedAlpha = 0.6f

    /** The original unselected alpha, retained for custom rail content. */
    val UnselectedAlpha = 0.4f
}

/**
 * Defines the display mode for items in a [NavigationRail].
 *
 * This controls whether to show both icon and text, icon only, text only,
 * or icon with text only when selected.
 */
enum class NavigationRailDisplayMode {
    /** Show both icon and text. */
    IconAndText,

    /** Show icon only. */
    IconOnly,

    /** Show text only. */
    TextOnly,

    /** Show icon always, show text only when selected. */
    IconWithSelectedLabel,
}

/**
 * A composition local to control the display mode for items in a [NavigationRail].
 */
val LocalNavigationRailDisplayMode = compositionLocalOf { NavigationRailDisplayMode.IconAndText }
