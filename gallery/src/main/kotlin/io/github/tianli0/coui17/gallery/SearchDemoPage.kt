package io.github.tianli0.coui17.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.BasicComponent
import io.github.suqi8.coui.kmp.basic.InputField
import io.github.suqi8.coui.kmp.basic.SearchBar
import io.github.suqi8.coui.kmp.basic.SearchBarActionButton
import io.github.suqi8.coui.kmp.basic.SearchHistoryView
import io.github.suqi8.coui.kmp.basic.SearchResultContainer
import io.github.suqi8.coui.kmp.basic.SearchResultGroupCard
import io.github.suqi8.coui.kmp.basic.rememberSearchResultsTransitionState
import io.github.tianli0.coui17.patterns.EmptyMessage
import io.github.tianli0.coui17.patterns.Page
import io.github.tianli0.coui17.patterns.pageListPadding
import kotlinx.coroutines.delay

@Composable
internal fun SearchDemoPage(onBack: () -> Unit) {
    val names = listOf(
        tr("相册", "Albums"),
        tr("旅行照片", "Travel photos"),
        tr("家庭照片", "Family photos"),
        tr("文档", "Documents"),
        tr("收藏", "Favorites"),
        tr("视频", "Videos"),
    )
    var query by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf("") }
    var request by rememberSaveable { mutableIntStateOf(0) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var loading by rememberSaveable { mutableStateOf(false) }
    var history by rememberSaveable { mutableStateOf(arrayListOf(names[1], names[3], names[5])) }
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val locale = LocalConfiguration.current.locales[0]
    val removeHistoryItem = tr("移除 %s", "Remove %s")

    fun setExpanded(value: Boolean) {
        expanded = value
        if (!value) {
            query = ""
            submitted = ""
            loading = false
            focus.clearFocus()
            keyboard?.hide()
        }
    }

    fun submit(text: String) {
        if (text.isBlank()) return
        val term = text.trim()
        query = term
        submitted = term
        request++
        loading = true
        history = ArrayList((listOf(term) + history).distinct().take(8))
        focus.clearFocus()
        keyboard?.hide()
    }

    LaunchedEffect(request, submitted) {
        if (submitted.isNotEmpty()) {
            delay(450)
            loading = false
        }
    }
    val transition = rememberSearchResultsTransitionState(
        showingResults = submitted.isNotEmpty(),
        loading = loading,
        queryKey = request,
    )

    Page(title = tr("搜索", "Search"), onBack = onBack) {
        Column(Modifier.fillMaxSize().padding(pageListPadding()).imePadding()) {
            SearchBar(
                inputField = {
                    InputField(
                        query = query,
                        onQueryChange = {
                            query = it
                            submitted = ""
                            loading = false
                        },
                        onSearch = ::submit,
                        expanded = expanded,
                        onExpandedChange = ::setExpanded,
                        label = tr("搜索照片和文件", "Search photos and files"),
                        clearContentDescription = tr("清除搜索词", "Clear query"),
                    )
                },
                expanded = expanded,
                onExpandedChange = ::setExpanded,
                outsideEndAction = {
                    SearchBarActionButton(tr("取消", "Cancel"), onClick = { setExpanded(false) })
                },
            ) {
                SearchResultContainer(state = transition, modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (!shownResults) {
                            if (query.isBlank()) {
                                SearchHistoryView(
                                    items = history,
                                    onItemClick = ::submit,
                                    onDeleteClick = { history = arrayListOf() },
                                    title = tr("搜索历史", "Search history"),
                                    deleteIconDescription = tr("清除搜索历史", "Clear history"),
                                    onItemDelete = { item -> history = ArrayList(history.filterNot { it == item }) },
                                    itemDeleteContentDescription = { removeHistoryItem.format(locale, it) },
                                )
                            } else {
                                val suggestions = names.filter { it.contains(query, ignoreCase = true) }
                                SearchResultGroupCard(tr("搜索建议", "Suggestions")) {
                                    (suggestions.ifEmpty { listOf(query) }).forEach { name ->
                                        BasicComponent(title = name, onClick = { submit(name) })
                                    }
                                }
                            }
                        } else if (resultsVisible) {
                            val results = names.filter { it.contains(submitted, ignoreCase = true) }
                            if (results.isEmpty()) {
                                EmptyMessage(title = tr("没有匹配结果", "No results"), subtitle = submitted)
                            } else {
                                SearchResultGroupCard(tr("搜索结果", "Results")) {
                                    results.forEach { name -> BasicComponent(title = name) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
