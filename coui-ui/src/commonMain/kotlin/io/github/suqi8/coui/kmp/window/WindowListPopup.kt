// Copyright 2025, compose-coui-ui contributors
// SPDX-License-Identifier: Apache-2.0
package io.github.suqi8.coui.kmp.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Dialog
import io.github.suqi8.coui.kmp.basic.ListPopupColumn
import io.github.suqi8.coui.kmp.basic.ListPopupDefaults
import io.github.suqi8.coui.kmp.basic.PopupPositionProvider
import io.github.suqi8.coui.kmp.basic.PreciseClickState
import io.github.suqi8.coui.kmp.blur.isBlurEnabled
import io.github.suqi8.coui.kmp.layout.ListPopupLayout
import io.github.suqi8.coui.kmp.shader.isRuntimeShaderSupported
import io.github.suqi8.coui.kmp.theme.LocalDismissState
import io.github.suqi8.coui.kmp.utils.DialogTouchPassThrough
import io.github.suqi8.coui.kmp.utils.ProvideHostWindowBackdrop
import io.github.suqi8.coui.kmp.utils.RemovePlatformDialogDefaultEffects
import io.github.suqi8.coui.kmp.utils.WindowNavigationEventScope
import io.github.suqi8.coui.kmp.utils.platformDialogProperties
import io.github.suqi8.coui.kmp.utils.rememberHostWindowBackdrop

/**
 * A popup with a list of items, rendered at window level without `Scaffold`.
 *
 * Use [LocalDismissState] inside `content` to request dismissal from inner composables.
 *
 * @param show Whether the [WindowListPopup] is shown.
 * @param popupModifier The modifier to be applied to the [WindowListPopup].
 * @param popupPositionProvider The [PopupPositionProvider] of the [WindowListPopup].
 * @param alignment The alignment of the [WindowListPopup].
 * @param enableWindowDim Whether to enable window dimming when the [WindowListPopup] is shown.
 *   Defaults to false: `COUIPopupListWindow` calls `setBackgroundDrawable(null)` and never dims,
 *   so a COUI dropdown leaves the content behind it untouched.
 * @param onDismissRequest The callback when the [WindowListPopup] is dismissed.
 * @param onDismissFinished The callback when the [WindowListPopup] is completely dismissed (after exit animation).
 * @param maxHeight The maximum height of the [WindowListPopup]. If null, the height will be calculated automatically.
 * @param minWidth The minimum width of the [WindowListPopup].
 * @param preciseClickState Opens the popup at the last point recorded by
 *   [io.github.suqi8.coui.kmp.basic.preciseClickAnchor] on the anchor instead of centring it on the
 *   anchor, matching COUI `PreciseClickHelper`. Null (default) keeps anchor-centred placement.
 * @param content The [Composable] content of the [WindowListPopup]. You should use the [ListPopupColumn] in general.
 */
@Composable
fun WindowListPopup(
    show: Boolean,
    popupModifier: Modifier = Modifier,
    popupPositionProvider: PopupPositionProvider = ListPopupDefaults.DropdownPositionProvider,
    alignment: PopupPositionProvider.Align = PopupPositionProvider.Align.Start,
    enableWindowDim: Boolean = false,
    onDismissRequest: (() -> Unit)? = null,
    onDismissFinished: (() -> Unit)? = null,
    maxHeight: Dp? = null,
    minWidth: Dp = ListPopupDefaults.MinWidth,
    preciseClickState: PreciseClickState? = null,
    staticBackdrop: ImageBitmap? = null,
    staticBackdropWindowSize: IntSize = IntSize.Zero,
    content: @Composable () -> Unit,
) {
    val currentOnDismissRequest = rememberUpdatedState(onDismissRequest)
    // Keep the caller density when the popup is composed in a different host.
    val density = LocalDensity.current
    val useHostBackdrop = staticBackdrop == null
    var hostMounted by remember { mutableStateOf(false) }
    val hostBackdrop = rememberHostWindowBackdrop(useHostBackdrop && (show || hostMounted))
    val blurActive = useHostBackdrop && isBlurEnabled() && isRuntimeShaderSupported()

    ListPopupLayout(
        show = show,
        // A dialog window is touch-modal while it is mounted, so the popup has to give its own
        // window up as soon as the close starts — otherwise the page stays undraggable for the
        // whole exit animation even though the panel is already gone from sight.
        popupHost = { visible, passTouches, hostContent ->
            if (visible) {
                DisposableEffect(Unit) {
                    hostMounted = true
                    onDispose { hostMounted = false }
                }
                Dialog(
                    onDismissRequest = { currentOnDismissRequest.value?.invoke() },
                    properties = platformDialogProperties(),
                ) {
                    RemovePlatformDialogDefaultEffects()
                    DialogTouchPassThrough(passTouches)
                    WindowNavigationEventScope {
                        CompositionLocalProvider(LocalDensity provides density) {
                            if (useHostBackdrop) {
                                ProvideHostWindowBackdrop(hostBackdrop.value) {
                                    hostContent()
                                }
                            } else {
                                hostContent()
                            }
                        }
                    }
                }
            }
        },
        popupModifier = popupModifier,
        popupPositionProvider = popupPositionProvider,
        alignment = alignment,
        enableWindowDim = enableWindowDim,
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
        maxHeight = maxHeight,
        minWidth = minWidth,
        preciseClickState = preciseClickState,
        enableGlass = useHostBackdrop,
        staticBackdrop = staticBackdrop,
        staticBackdropWindowSize = staticBackdropWindowSize,
        backdropReady = { !blurActive || hostBackdrop.value != null },
        content = {
            CompositionLocalProvider(
                LocalDismissState provides {
                    currentOnDismissRequest.value?.invoke()
                },
            ) {
                content()
            }
        },
    )
}
