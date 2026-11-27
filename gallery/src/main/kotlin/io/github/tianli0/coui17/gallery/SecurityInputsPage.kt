package io.github.tianli0.coui17.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.CodeTextField
import io.github.suqi8.coui.kmp.basic.LockPatternView
import io.github.suqi8.coui.kmp.basic.LockScreenPwdInput
import io.github.suqi8.coui.kmp.basic.NumericKeyboard
import io.github.suqi8.coui.kmp.basic.NumericKeyboardSideStyle
import io.github.suqi8.coui.kmp.basic.NumericKeyboardSideType
import io.github.suqi8.coui.kmp.basic.SimpleLockView
import io.github.suqi8.coui.kmp.basic.Text
import io.github.tianli0.coui17.patterns.GroupCard
import io.github.tianli0.coui17.patterns.Page
import io.github.tianli0.coui17.patterns.SectionHeader
import io.github.tianli0.coui17.patterns.SmallButton

@Composable
internal fun SecurityInputsPage(onBack: () -> Unit) {
    var digits by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf(emptyList<Int>()) }
    var textPassword by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var keyboardCode by remember { mutableStateOf("") }
    var failed by remember { mutableStateOf(false) }
    var accepted by remember { mutableStateOf(false) }
    val simulatedCode = "123456"

    Page(title = tr("安全输入", "Security inputs"), onBack = onBack, immersive = true) {
        PageList {
            item { SectionHeader(tr("验证码", "Verification code")) }
            item {
                GroupCard {
                    Column(
                        Modifier.fillMaxWidth().padding(CardContentPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(tr("输入演示验证码 123456", "Enter the demo code 123456"))
                        CodeTextField(value = digits, onValueChange = { digits = it.filter(Char::isDigit) }, cellCount = 6)
                        if (digits.length == simulatedCode.length) {
                            Text(if (digits == simulatedCode) tr("验证码正确", "Code accepted") else tr("验证码错误", "Incorrect code"))
                        }
                        SmallButton(tr("清除", "Clear"), { digits = "" })
                    }
                }
            }
            item { SectionHeader(tr("数字键盘", "Numeric keypad")) }
            item {
                GroupCard {
                    Column(
                        Modifier.fillMaxWidth().padding(CardContentPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(tr("用键盘输入 123456，再点完成", "Enter 123456 on the keypad, then tap Done"))
                        SimpleLockView(count = keyboardCode.length, failed = failed)
                        if (failed) Text(tr("密码错误，请重试", "Incorrect PIN. Try again."))
                        if (accepted) Text(tr("演示密码正确", "Demo PIN accepted"))
                        NumericKeyboard(
                            onNumberClick = { digit ->
                                if (failed) keyboardCode = ""
                                failed = false
                                accepted = false
                                if (keyboardCode.length < simulatedCode.length) keyboardCode += digit
                            },
                            leftStyle = NumericKeyboardSideStyle(
                                type = NumericKeyboardSideType.Delete,
                                description = tr("删除一位", "Delete digit"),
                            ),
                            onLeftClick = {
                                failed = false
                                accepted = false
                                keyboardCode = keyboardCode.dropLast(1)
                            },
                            rightStyle = NumericKeyboardSideStyle(
                                type = NumericKeyboardSideType.Finish,
                                text = tr("完成", "Done"),
                            ),
                            onRightClick = {
                                accepted = keyboardCode == simulatedCode
                                failed = !accepted
                                if (accepted) keyboardCode = ""
                            },
                        )
                    }
                }
            }
            item { SectionHeader(tr("手势图案", "Unlock pattern")) }
            item {
                GroupCard {
                    Column(
                        Modifier.fillMaxWidth().padding(CardContentPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            if (pattern.isEmpty()) tr("连接图案上的点", "Connect the dots")
                            else tr("已连接 ${pattern.size} 个点", "${pattern.size} dots connected"),
                        )
                        LockPatternView(onPatternComplete = { pattern = it }, pattern = pattern)
                        SmallButton(tr("重新绘制", "Reset pattern"), { pattern = emptyList() })
                    }
                }
            }
            item { SectionHeader(tr("密码字段", "Password field")) }
            item {
                GroupCard {
                    Column(Modifier.padding(CardContentPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LockScreenPwdInput(
                            value = textPassword,
                            onValueChange = {
                                textPassword = it
                                submitted = false
                            },
                            label = tr("演示密码（任意四个字符）", "Demo password (any four characters)"),
                            minCount = 4,
                            onNextClick = {
                                submitted = true
                                textPassword = ""
                            },
                            nextContentDescription = tr("提交演示密码", "Submit demo password"),
                        )
                        if (submitted) Text(tr("已提交", "Submitted"))
                    }
                }
            }
        }
    }
}
