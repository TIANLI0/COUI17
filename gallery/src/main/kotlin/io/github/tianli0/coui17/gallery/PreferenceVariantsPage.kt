package io.github.tianli0.coui17.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.suqi8.coui.kmp.basic.DropdownItem
import io.github.suqi8.coui.kmp.basic.cardListPositionOf
import io.github.suqi8.coui.kmp.core.ExperimentalCOUIApi
import io.github.suqi8.coui.kmp.preference.AppInfoPreference
import io.github.suqi8.coui.kmp.preference.ButtonPreference
import io.github.suqi8.coui.kmp.preference.CheckBoxWithDividerPreference
import io.github.suqi8.coui.kmp.preference.InputPreference
import io.github.suqi8.coui.kmp.preference.MarkPreference
import io.github.suqi8.coui.kmp.preference.OverlayDropdownPreference
import io.github.suqi8.coui.kmp.preference.OverlaySpinnerPreference
import io.github.suqi8.coui.kmp.preference.RangeSliderPreference
import io.github.suqi8.coui.kmp.preference.RecommendedItem
import io.github.suqi8.coui.kmp.preference.RecommendedPreference
import io.github.suqi8.coui.kmp.preference.SliderPreference
import io.github.suqi8.coui.kmp.preference.StepperPreference
import io.github.suqi8.coui.kmp.preference.SwitchWithDividerPreference
import io.github.suqi8.coui.kmp.preference.WindowDropdownPreference
import io.github.suqi8.coui.kmp.preference.WindowSpinnerPreference
import io.github.tianli0.coui17.patterns.GroupCard
import io.github.tianli0.coui17.patterns.KeyValueRow
import io.github.tianli0.coui17.patterns.Page
import io.github.tianli0.coui17.patterns.SectionHeader
import kotlin.math.roundToInt

@OptIn(ExperimentalCOUIApi::class)
@Composable
internal fun PreferenceVariantsPage(onBack: () -> Unit) {
    val choices = listOf(tr("标准", "Standard"), tr("高", "High"), tr("最高", "Highest"))
    val spinnerItems = choices.map { DropdownItem(text = it) }
    var dropdownInWindow by rememberSaveable { mutableIntStateOf(0) }
    var dropdownInPage by rememberSaveable { mutableIntStateOf(0) }
    var spinnerInWindow by rememberSaveable { mutableIntStateOf(0) }
    var spinnerInPage by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var copies by rememberSaveable { mutableIntStateOf(1) }
    var quality by rememberSaveable { mutableFloatStateOf(75f) }
    var low by rememberSaveable { mutableFloatStateOf(20f) }
    var high by rememberSaveable { mutableFloatStateOf(80f) }
    var autoSave by rememberSaveable { mutableStateOf(true) }
    var useMetadata by rememberSaveable { mutableStateOf(true) }
    var markOriginal by rememberSaveable { mutableStateOf(false) }
    var metadataDetails by rememberSaveable { mutableStateOf(false) }
    val defaultName = tr("未命名", "Untitled")

    Page(title = tr("偏好行变体", "Preference variants"), onBack = onBack, immersive = true) {
        PageList {
            item { SectionHeader(tr("可在行内修改", "Edit in the row")) }
            item {
                GroupCard {
                    InputPreference(
                        title = tr("文件名称", "File name"),
                        value = name,
                        onValueChange = { name = it },
                        cardListPosition = cardListPositionOf(0, 4),
                    )
                    StepperPreference(copies, { copies = it }, title = tr("副本数量", "Copies"), minValue = 1, maxValue = 9)
                    SliderPreference(
                        quality,
                        { quality = it },
                        title = tr("压缩质量", "Compression quality"),
                        valueText = "${quality.roundToInt()}%",
                        valueRange = 0f..100f,
                        cardListPosition = cardListPositionOf(2, 4),
                    )
                    RangeSliderPreference(
                        low..high,
                        {
                            low = it.start
                            high = it.endInclusive
                        },
                        title = tr("范围", "Range"),
                        valueText = "${low.roundToInt()}–${high.roundToInt()}%",
                        valueRange = 0f..100f,
                        cardListPosition = cardListPositionOf(3, 4),
                    )
                }
            }
            item { SectionHeader(tr("选项菜单", "Option menus")) }
            item {
                GroupCard {
                    WindowDropdownPreference(
                        choices, dropdownInWindow, tr("独立窗口下拉项", "Window dropdown"),
                        onSelectedIndexChange = { dropdownInWindow = it },
                        cardListPosition = cardListPositionOf(0, 4),
                    )
                    OverlayDropdownPreference(
                        choices, dropdownInPage, tr("页内下拉项", "In-page dropdown"),
                        onSelectedIndexChange = { dropdownInPage = it },
                        cardListPosition = cardListPositionOf(1, 4),
                    )
                    WindowSpinnerPreference(
                        spinnerItems, spinnerInWindow, tr("独立窗口选择器", "Window spinner"),
                        onSelectedIndexChange = { spinnerInWindow = it },
                        cardListPosition = cardListPositionOf(2, 4),
                    )
                    OverlaySpinnerPreference(
                        spinnerItems, spinnerInPage, tr("页内选择器", "In-page spinner"),
                        onSelectedIndexChange = { spinnerInPage = it },
                        cardListPosition = cardListPositionOf(3, 4),
                    )
                }
            }
            item { SectionHeader(tr("独立控件与整行操作", "Separate controls and row actions")) }
            item {
                GroupCard {
                    SwitchWithDividerPreference(
                        autoSave, { autoSave = it }, tr("自动保存", "Auto-save"),
                        cardListPosition = cardListPositionOf(0, 3),
                    )
                    CheckBoxWithDividerPreference(
                        title = tr("保留元数据", "Preserve metadata"),
                        summary = if (metadataDetails) tr("照片的时间和拍摄参数", "Photo dates and camera settings")
                        else tr("点按可查看说明", "Tap for details"),
                        checked = useMetadata,
                        onCheckedChange = { useMetadata = it },
                        onMainLayoutClick = { metadataDetails = !metadataDetails },
                        cardListPosition = cardListPositionOf(1, 3),
                    )
                    MarkPreference(
                        title = tr("标记为原始文件", "Mark as original"),
                        checked = markOriginal,
                        onClick = { markOriginal = !markOriginal },
                        cardListPosition = cardListPositionOf(2, 3),
                    )
                }
            }
            item {
                GroupCard {
                    KeyValueRow(tr("保存为", "Save as"), name.ifBlank { defaultName })
                    ButtonPreference(
                        title = tr("恢复默认名称", "Restore default name"),
                        buttonText = tr("重置", "Reset"),
                        onButtonClick = { name = "" },
                    )
                }
            }
            item {
                val suggestedNames = listOf(tr("照片", "Photo"), tr("副本", "Copy"))
                RecommendedPreference(
                    title = tr("推荐名称", "Suggested names"),
                    items = suggestedNames.map { suggestion -> RecommendedItem(suggestion) { name = suggestion } },
                )
            }
            item { AppInfoPreference(appName = "COUI17 Gallery", appVersion = tr("组件演示", "Component gallery")) }
        }
    }
}
