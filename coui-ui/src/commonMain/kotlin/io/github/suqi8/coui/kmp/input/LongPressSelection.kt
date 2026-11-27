// Copyright 2026, COUI17 contributors
// SPDX-License-Identifier: Apache-2.0
package io.github.suqi8.coui.kmp.input

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned

/** Tracks a pressed selection without committing it until the finger lifts. */
class COUILongPressSelectionState internal constructor() {
    var previewKey: Any? by mutableStateOf(null)
        internal set

    internal var coordinates: LayoutCoordinates? = null
    internal val items = mutableMapOf<Any, LongPressSelectionItem>()

    internal fun itemAt(position: Offset): Map.Entry<Any, LongPressSelectionItem>? {
        val container = coordinates?.takeIf { it.isAttached } ?: return null
        return items.entries.firstOrNull { (_, item) ->
            val child = item.coordinates
            child != null && child.isAttached && container.localBoundingBoxOf(child).contains(position)
        }
    }
}

internal class LongPressSelectionItem(val onSelect: () -> Unit) {
    var coordinates: LayoutCoordinates? = null
}

@Composable
fun rememberCOUILongPressSelectionState(): COUILongPressSelectionState = remember { COUILongPressSelectionState() }

/** Handles hold-and-drag selection while leaving ordinary taps and scrolling to the children. */
fun Modifier.couiLongPressSelection(state: COUILongPressSelectionState): Modifier = this
    .onGloballyPositioned { state.coordinates = it }
    .pointerInput(state) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            if (down.type != PointerType.Touch || state.itemAt(down.position) == null) return@awaitEachGesture

            var position = down.position
            val cancelled = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { it.id == down.id }
                    if (change == null || !change.pressed || event.changes.any { it.id != down.id && it.pressed }) break
                    position = change.position
                    if ((position - down.position).getDistance() > viewConfiguration.touchSlop) break
                }
                true
            }
            if (cancelled != null) return@awaitEachGesture

            try {
                state.previewKey = state.itemAt(position)?.key
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (event.changes.any { it.id != down.id && it.pressed }) break
                    val target = state.itemAt(change.position)
                    state.previewKey = target?.key
                    change.consume()
                    if (!change.pressed) {
                        target?.value?.onSelect?.invoke()
                        break
                    }
                }
            } finally {
                state.previewKey = null
            }
        }
    }

/** Registers an enabled choice's rendered bounds and its release action. */
@Composable
fun Modifier.couiLongPressSelectionItem(
    state: COUILongPressSelectionState,
    key: Any,
    enabled: Boolean,
    onSelect: () -> Unit,
): Modifier {
    if (!enabled) return this
    val callback by rememberUpdatedState(onSelect)
    val item = remember(state, key) { LongPressSelectionItem { callback() } }
    DisposableEffect(state, key, item) {
        state.items[key] = item
        onDispose { state.items.remove(key) }
    }
    return onGloballyPositioned { item.coordinates = it }
}
