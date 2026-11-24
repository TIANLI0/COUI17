package io.github.tianli0.coui17.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.ButtonDefaults
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.Slider
import io.github.suqi8.coui.kmp.basic.Switch
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.TextButton
import io.github.suqi8.coui.kmp.basic.cardListPositionOf
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.basic.Check
import io.github.suqi8.coui.kmp.preference.RadioButtonPreference
import io.github.suqi8.coui.kmp.squircle.SquircleShape
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.tianli0.coui17.patterns.Accent
import io.github.tianli0.coui17.patterns.ActionTone
import io.github.tianli0.coui17.patterns.GroupCard
import io.github.tianli0.coui17.patterns.LocalIsDarkTheme
import io.github.tianli0.coui17.patterns.Page
import io.github.tianli0.coui17.patterns.SectionHeader
import io.github.tianli0.coui17.patterns.SmallButton

@Composable
fun ThemePage(modeIndex: Int, onMode: (Int) -> Unit, accentIndex: Int, onAccent: (Int) -> Unit) {
    val scheme = COUITheme.colorScheme
    val styles = COUITheme.textStyles
    Page(title = tr("主题", "Theme"), immersive = true) {
        PageList {
            item { SectionHeader(tr("外观", "Appearance")) }
            item {
                GroupCard {
                    val modes = listOf(tr("跟随系统", "Follow system"), tr("浅色", "Light"), tr("深色", "Dark"))
                    modes.forEachIndexed { index, label ->
                        RadioButtonPreference(
                            title = label,
                            selected = modeIndex == index,
                            onClick = { onMode(index) },
                            cardListPosition = cardListPositionOf(index, modes.size),
                        )
                    }
                }
            }
            item { SectionHeader(tr("强调色", "Accent")) }
            item {
                GroupCard {
                    Column(Modifier.padding(CardContentPadding)) {
                        val names = listOf(tr("蓝", "Blue"), tr("绿", "Green"), tr("橙", "Orange"), tr("红", "Red"), tr("紫", "Purple"), tr("青", "Cyan"))
                        val dark = LocalIsDarkTheme.current
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Accent.Presets.forEachIndexed { index, accent ->
                                val color = if (dark) accent.dark else accent.light
                                val picked = index == accentIndex
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable(role = Role.RadioButton) { onAccent(index) }
                                        .semantics {
                                            contentDescription = names[index]
                                            selected = picked
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (picked) Icon(COUIIcons.Basic.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                        Spacer(Modifier.height(18.dp))
                        // One accent drives every accent slot: these all change together.
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Switch(checked = true, onCheckedChange = null)
                            SmallButton(tr("主要", "Primary"), {}, tone = ActionTone.Primary)
                            SmallButton(tr("默认", "Default"), {})
                        }
                        Spacer(Modifier.height(14.dp))
                        Slider(value = 0.6f, onValueChange = {})
                        Spacer(Modifier.height(16.dp))
                        TextButton(
                            text = tr("实心按钮", "Filled button"),
                            onClick = {},
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            item { SectionHeader(tr("颜色", "Colours")) }
            item {
                GroupCard {
                    Column(Modifier.padding(CardContentPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Swatch("primary", scheme.primary)
                        Swatch("background", scheme.background)
                        Swatch("surfaceContainer", scheme.surfaceContainer)
                        Swatch("onSurface", scheme.onSurface)
                        Swatch("onSurfaceVariantSummary", scheme.onSurfaceVariantSummary)
                        Swatch("error", scheme.error)
                    }
                }
            }
            item { SectionHeader(tr("字体", "Type")) }
            item {
                GroupCard {
                    Column(Modifier.padding(CardContentPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        TypeSample("title1", styles.title1)
                        TypeSample("title2", styles.title2)
                        TypeSample("title3", styles.title3)
                        TypeSample("title4", styles.title4)
                        TypeSample("headline1", styles.headline1)
                        TypeSample("body1", styles.body1)
                        TypeSample("body2", styles.body2)
                        TypeSample("footnote1", styles.footnote1)
                    }
                }
            }
            item { Note(tr("页面底色之上是 surfaceContainer 卡片；深色模式下强调色会提亮以保证对比度。", "Cards are surfaceContainer on the page colour; in dark mode the accent is lightened for contrast.")) }
        }
    }
}

@Composable
private fun Swatch(name: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(28.dp)
                .background(color, SquircleShape(8.dp))
                .border(0.5.dp, COUITheme.colorScheme.onSurface.copy(alpha = 0.12f), SquircleShape(8.dp)),
        )
        Text(name, style = COUITheme.textStyles.body1, color = COUITheme.colorScheme.onSurface, modifier = Modifier.padding(start = 14.dp).weight(1f))
        Text(
            "#%08X".format(java.util.Locale.ROOT, colorToArgb(color)),
            style = COUITheme.textStyles.footnote1,
            color = COUITheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

private fun colorToArgb(color: Color): Int {
    fun channel(v: Float) = (v * 255f + 0.5f).toInt().coerceIn(0, 255)
    return (channel(color.alpha) shl 24) or (channel(color.red) shl 16) or (channel(color.green) shl 8) or channel(color.blue)
}

@Composable
private fun TypeSample(name: String, style: TextStyle) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(tr("色彩与光", "Light and colour"), style = style, color = COUITheme.colorScheme.onSurface, maxLines = 1, modifier = Modifier.weight(1f))
        Text(
            "$name · ${style.fontSize.value.toInt()}sp",
            style = COUITheme.textStyles.footnote1,
            color = COUITheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}
