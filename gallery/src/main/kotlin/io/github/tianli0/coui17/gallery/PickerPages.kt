package io.github.tianli0.coui17.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.CalendarView
import io.github.suqi8.coui.kmp.basic.ColorPalette
import io.github.suqi8.coui.kmp.basic.ColorPicker
import io.github.suqi8.coui.kmp.basic.ColorSpace
import io.github.suqi8.coui.kmp.basic.ColorSwatchPicker
import io.github.suqi8.coui.kmp.basic.DatePicker
import io.github.suqi8.coui.kmp.basic.DateValue
import io.github.suqi8.coui.kmp.basic.LunarDatePicker
import io.github.suqi8.coui.kmp.basic.NumberPicker
import io.github.suqi8.coui.kmp.basic.TabRowWithContour
import io.github.suqi8.coui.kmp.basic.TimePicker
import io.github.suqi8.coui.kmp.basic.TimeValue
import io.github.suqi8.coui.kmp.preference.SwitchPreference
import io.github.suqi8.coui.kmp.utils.LunarDateValue
import io.github.tianli0.coui17.patterns.GroupCard
import io.github.tianli0.coui17.patterns.KeyValueRow
import io.github.tianli0.coui17.patterns.Page
import io.github.tianli0.coui17.patterns.SectionHeader
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle

private val dateSaver = listSaver<DateValue, Int>(
    save = { listOf(it.year, it.month, it.day) },
    restore = { DateValue(it[0], it[1], it[2]) },
)
private val lunarDateSaver = listSaver<LunarDateValue, Int>(
    save = { listOf(it.year, it.month, it.day, if (it.isLeapMonth) 1 else 0) },
    restore = { LunarDateValue(it[0], it[1], it[2], isLeapMonth = it[3] == 1) },
)
private val timeSaver = listSaver<TimeValue, Int>(
    save = { listOf(it.hour, it.minute) },
    restore = { TimeValue(it[0], it[1]) },
)

@Composable
internal fun PickersPage(onBack: () -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
    var date by rememberSaveable(stateSaver = dateSaver) { mutableStateOf(DateValue(2026, 11, 27)) }
    var lunar by rememberSaveable(stateSaver = lunarDateSaver) { mutableStateOf(LunarDateValue(2026, 10, 19)) }
    var time by rememberSaveable(stateSaver = timeSaver) { mutableStateOf(TimeValue(14, 30)) }
    var use24Hour by rememberSaveable { mutableStateOf(true) }
    var number by rememberSaveable { mutableIntStateOf(5) }
    val selected = LocalDate.of(date.year, date.month, date.day).format(dateFormat)

    Page(title = tr("日期与时间", "Date and time"), onBack = onBack, immersive = true) {
        PageList {
            item { SectionHeader(tr("公历日期", "Gregorian date")) }
            item {
                GroupCard {
                    Column(Modifier.padding(CardContentPadding), horizontalAlignment = Alignment.CenterHorizontally) {
                        KeyValueRow(tr("已选日期", "Selected date"), selected)
                        CalendarView(
                            value = date,
                            onValueChange = { date = it },
                            modifier = Modifier.fillMaxWidth(),
                            yearRange = 1901..2099,
                            title = { year, month ->
                                val pattern = if (locale.language == "zh") "yyyy年M月" else "LLLL yyyy"
                                LocalDate.of(year, month, 1).format(DateTimeFormatter.ofPattern(pattern, locale))
                            },
                            weekdayLabel = { DayOfWeek.of(it).getDisplayName(TextStyle.NARROW, locale) },
                            dayDescription = { year, month, day -> LocalDate.of(year, month, day).format(dateFormat) },
                        )
                        DatePicker(date, { date = it }, yearRange = 1901..2099)
                    }
                }
            }
            item { SectionHeader(tr("农历滚轮", "Lunar date wheel")) }
            item {
                GroupCard {
                    Column(Modifier.padding(CardContentPadding), horizontalAlignment = Alignment.CenterHorizontally) {
                        val leap = if (lunar.isLeapMonth) tr("（闰月）", " (leap month)") else ""
                        KeyValueRow(tr("已选农历日期", "Selected lunar date"), "${lunar.year}/${lunar.month}/${lunar.day}$leap")
                        LunarDatePicker(lunar, { lunar = it })
                    }
                }
            }
            item { SectionHeader(tr("时间滚轮", "Time wheel")) }
            item {
                GroupCard {
                    SwitchPreference(
                        title = tr("24 小时制", "24-hour time"),
                        checked = use24Hour,
                        onCheckedChange = { use24Hour = it },
                    )
                    Column(Modifier.padding(CardContentPadding), horizontalAlignment = Alignment.CenterHorizontally) {
                        KeyValueRow(tr("已选时间", "Selected time"), "%02d:%02d".format(locale, time.hour, time.minute))
                        TimePicker(time, { time = it }, is24Hour = use24Hour)
                    }
                }
            }
            item { SectionHeader(tr("数字滚轮", "Number wheel")) }
            item {
                GroupCard {
                    Column(Modifier.fillMaxWidth().padding(CardContentPadding), horizontalAlignment = Alignment.CenterHorizontally) {
                        KeyValueRow(tr("已选数字", "Selected number"), number.toString())
                        NumberPicker(number, { number = it }, range = 0..15, visibleItemCount = 5, wrapAround = false)
                    }
                }
            }
        }
    }
}

@Composable
internal fun ColorsPage(onBack: () -> Unit) {
    val swatches = listOf(0xff3478f6, 0xff29a979, 0xfffa684c, 0xff8557e7, 0xffeeac32).map { Color(it) }
    var argb by rememberSaveable { mutableIntStateOf(swatches.first().toArgb()) }
    var mode by rememberSaveable { mutableIntStateOf(0) }
    val color = Color(argb)

    Page(title = tr("颜色选择", "Color pickers"), onBack = onBack, immersive = true) {
        PageList {
            item {
                GroupCard {
                    Column(Modifier.padding(CardContentPadding), horizontalAlignment = Alignment.CenterHorizontally) {
                        KeyValueRow(tr("当前颜色", "Current color"), "#%08X".format(argb))
                        Box(Modifier.size(44.dp).background(color, CircleShape))
                        ColorSwatchPicker(
                            colors = swatches,
                            selectedIndex = swatches.indexOf(color),
                            onSwatchSelected = { argb = swatches[it].toArgb() },
                        )
                    }
                }
            }
            item { SectionHeader(tr("色板", "Palette")) }
            item {
                GroupCard {
                    ColorPalette(color, { argb = it.toArgb() }, Modifier.padding(CardContentPadding))
                }
            }
            item { SectionHeader(tr("颜色空间", "Color space")) }
            item {
                GroupCard {
                    Column(Modifier.padding(CardContentPadding), horizontalAlignment = Alignment.CenterHorizontally) {
                        TabRowWithContour(ColorSpace.entries.map { it.name }, mode, { mode = it })
                        ColorPicker(
                            color = color,
                            onColorChanged = { argb = it.toArgb() },
                            modifier = Modifier.fillMaxWidth(),
                            colorSpace = ColorSpace.entries[mode],
                        )
                    }
                }
            }
        }
    }
}
