// Copyright 2026, COUI17 contributors
// SPDX-License-Identifier: Apache-2.0

package io.github.tianli0.coui17.patterns

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.LocalSearchBarKeyboardType
import io.github.suqi8.coui.kmp.basic.SearchBar
import io.github.suqi8.coui.kmp.basic.SearchBarDefaults
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.basic.Search

/**
 * The one search / filter field of a COUI17 page: COUI's 40dp search capsule with the magnifier,
 * the clear key and, while it is in use, the "取消" key beside it (`COUISearchBar`, as on the
 * ColorOS 17 Settings home and in Files). Use it for every search and every list filter instead
 * of a [TextInput]; filters about *what* is searched belong in a chip row under it, not in extra
 * keys on its row.
 *
 * The cancel key shows while the field has focus or text, or always when [alwaysExpanded]. It
 * clears the text, drops the focus and calls [onCancel].
 *
 * @param fileNames an ASCII keyboard, so a Chinese input method does not compose "README_ZH" into
 *   "README＿" while a file name is typed.
 * @param autoFocus focus the field (and raise the keyboard) when it first appears, for a search
 *   the user has just opened.
 */
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit = {},
    onCancel: () -> Unit = {},
    alwaysExpanded: Boolean = false,
    autoFocus: Boolean = false,
    fileNames: Boolean = false,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    if (autoFocus) {
        LaunchedEffect(Unit) {
            withFrameNanos { }
            runCatching { focusRequester.requestFocus() }
        }
    }
    val cancel = {
        onQueryChange("")
        focusManager.clearFocus()
        keyboard?.hide()
        onCancel()
    }
    val colors = SearchBarDefaults.searchBarColors()
    CompositionLocalProvider(LocalSearchBarKeyboardType provides if (fileNames) KeyboardType.Ascii else KeyboardType.Text) {
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = {
                keyboard?.hide()
                onSearch(it)
            },
            expanded = alwaysExpanded || focused || query.isNotEmpty(),
            onExpandedChange = { if (!it) cancel() },
            label = label,
            cancelText = stringResource(R.string.coui17_action_cancel),
            onCancel = cancel,
            insideMargin = DpSize(0.dp, 6.dp),
            interactionSource = interaction,
            clearContentDescription = stringResource(R.string.coui17_action_clear),
            leadingIcon = {
                Icon(
                    imageVector = COUIIcons.Basic.Search,
                    contentDescription = null,
                    tint = colors.iconColor,
                    modifier = Modifier.padding(
                        start = SearchBarDefaults.LeadingIconStartPadding,
                        end = SearchBarDefaults.LeadingIconEndPadding,
                    ),
                )
            },
            trailingIcon = trailingIcon,
            modifier = modifier.focusRequester(focusRequester),
        )
    }
}
