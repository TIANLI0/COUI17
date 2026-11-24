# ColorOS 17 设计规范（COUI17 的用法约定）

组件本身只保证“单个控件长得对”。一个页面像不像 ColorOS 17，取决于下面这些约定。`coui-patterns` 把它们做成了组件，调用处选的是名字而不是数值。

## 1. 页面结构

- **页面底色 + 卡片。** 页面是 `pageContainerColor()`，内容放在 `surfaceContainer` 的圆角分组卡片（`GroupCard`）里。不要把行直接铺在页面底色上。
- **标题栏。** 顶级页面用可折叠的大标题，详情页用小标题加返回键：`Page(title, onBack = …)` 会自己选。长标题在小标题栏里滚动而不是截断。
- **沉浸式列表。** 页面主体是一整个滚动列表时用 `Page(immersive = true)`：列表从屏幕顶端开始，滚到状态栏和标题栏下面，标题栏是渐进毛玻璃。列表配套三件事：

  ```kotlin
  LazyColumn(
      state = rememberPageListState(),     // 点标题栏 / 状态栏回到顶部
      modifier = Modifier.pageScroll(),    // 过度滚动、触底震动、联动大标题
      contentPadding = pageListPadding(),  // 首项让开标题栏，末项让开导航胶囊 / 手势条
  )
  ```

  列表上方有固定内容（分段、筛选条、输入栏）的页面不要用沉浸式。固定在列表上方的提示条应当放进列表，作为第一项。
- **底部导航。** `BottomFloatingNavigationBar` 是悬浮的玻璃胶囊。页面内容录成一个 `LayerBackdrop`，导航条是它的**兄弟节点**（不能在被录制的层里），并通过 `LocalBottomBarSpace` 告诉页面要留出多少空间。详情页隐藏导航条。写法见 `gallery/…/GalleryApp.kt`。

## 2. 间距

| 名称 | 值 | 用在哪 |
| :--- | :--- | :--- |
| `PageDefaults.CardGutter` | 16dp | 卡片到屏幕边缘 |
| `PageDefaults.TextGutter` | 32dp | 页面上的散文字（分组下的说明、引导语、空提示）：卡片边距 16dp 加卡片内的 16dp，和行标题、`SmallTitle` 对齐 |
| `PageDefaults.GroupSpacing` | 16dp | 分组卡片下方的间隔 |

散文字只有这一个缩进。同一个应用里出现 16、24、28、32 四种缩进，是页面显得“不对”的最常见原因。

## 3. 分组里的行

- 一行是 `BasicComponent` 或某个 `*Preference`。同一张卡片里的行要传 `cardListPosition = cardListPositionOf(index, count)`，这样首行和末行的按压反馈才会跟着卡片的圆角走。
- 懒加载的长列表里，每一行是独立的列表项时用 `LazyGroupRow(position)`，看起来仍是一张卡片；自己给行加点击时先 `clip(groupRowShape(position))`。
- 打开新页面的行用 `ArrowPreference`（带箭头）；在原地弹出菜单或执行动作的行不带箭头。
- 行首图标用统一尺寸的彩色超椭圆底（见 gallery 的 `IconTile`），不要混用不同大小的裸图标。
- 行里的操作用小按钮（见下），靠末端对齐。

## 4. 按钮分级

coui 的 `Button` / `TextButton` 不传参数时是 **Large**：44dp 的实心胶囊。把它用在“重试”“加载更多”和行内操作上，页面就会变成一摞一摞的色块。

| 层级 | 组件 | 外观 | 用在哪 |
| :--- | :--- | :--- | :--- |
| Large | `Button` / `TextButton`、`SheetButtons`、`StepButtons` | 44dp 胶囊 | 决策面板或引导步骤的末尾。一屏只有一个实心按钮；页面内部不用 |
| 面板确认 | `PanelSheet(confirm = SheetConfirm(…))` | 面板工具栏里的 40dp 强调色胶囊 | 完成一个表单面板的动作（新建 → 创建），键盘弹出时仍可见 |
| Small | `SmallButton(text, onClick, tone)` | 28dp 胶囊，14sp | 行、卡片、工具栏里的操作。从不撑满宽度；多个并排时间距 8dp |
| Link | `LinkButton(text, onClick)` | 无底色的强调色文字，36dp 触摸高度 | 列表页脚（加载更多、查看全部）、文字旁的操作（刷新、管理） |

`ActionTone`：`Default`（灰底、强调色字）、`Primary`（强调色底：这张卡片唯一要做的事——允许、提交、保存）、`Danger`（灰底、红字：停止、删除、解除绑定）。不使用撑满宽度的灰色按钮。

## 5. 强调色

COUI 的配色里有好几个强调色槽位：`primary`、`primaryContainer`、`containerTheme`（实心按钮的填充）、`tertiaryContainer`（浅色底）。只改 `primary`，实心按钮仍是默认蓝，和旁边应用色的开关不一致。`COUI17Theme(accent = Accent(light, dark))` 一次设置全部槽位；深色模式下用同色相提亮后的颜色保证对比度。

状态色（成功、警告、错误）不随强调色变化，而且**状态永远带文字**，不只靠颜色区分（`StatusPill`）。

## 6. 玻璃

- **只有浮在内容之上的东西才是玻璃**：标题栏、导航胶囊、悬浮输入栏和按键、弹出菜单、对话框。卡片和页面是不透明的。
- 悬浮控件的表面用 `Modifier.glassSurface(shape)`，页面把它下方的列表录成 `LayerBackdrop` 并通过 `LocalGlassBackdrop` 提供。控件必须是被录制层的兄弟节点。
- 这个材质用的是 ColorOS 17 悬浮工具栏的着色层（含 `Luminosity` 层），不是“卡片色加透明度”。后者在深色模式下会把白色文字的亮度透上来，看起来是一块浅灰的板。
- **输入栏是一条 ColorOS 工具栏。** 一行：附加键、输入框、发送键。键用 `ComposerKey`（和标题栏返回键同款的 40dp 玻璃圆键，间距 `ComposerKeyGap` = 10dp），输入框用 `InputPill`：40dp 胶囊，随文字长高，有内容可发时发送键填上强调色。不要在一行里混用三种不同做法的圆键。
- 小标题栏压在密集文字上时，用 `glassHeader`（`Page` 在沉浸式小标题页上自动使用）：比 coui 自带的渐变材质更强，标题下方的文字不会干扰阅读。
- 弹出菜单在自己的窗口里，没有页面可模糊。`GlassMenu` 在打开时对 Activity 窗口做一次 1/8 尺寸的 PixelCopy 并做盒式模糊，作为菜单的静态背景。
- **浮起来的控件只有一种阴影**：`Modifier.floatingHalo(shape)`，即标题栏返回键、面板关闭胶囊的那种柔和光晕（16dp 模糊、下移 6dp、浅色 12% 黑 / 深色 5% 白）。输入栏的输入框和圆键、悬浮圆按钮、面板工具栏里的确认胶囊都用它（`glassSurface(shape, halo = true)`）。不要用 Material 的 `shadow(elevation)`，它和玻璃控件放在一起是另一种光。卡片、行内的小按钮没有投影。
- 条件不满足时（Android 13 以下、系统关闭模糊、测试环境）一律退回不透明填充，布局不变。

## 7. 面板与对话框

- **确认**用居中的 `ConfirmDialog`：标题、一段说明、取消 | 确认。
- **半屏面板**用 `PanelSheet`：画在页面底色上，沿用页面的 16dp 边距，所以面板里的 `GroupCard` 和页面上一样是卡片。（默认的面板底色上，白卡片会消失。）工具栏一行：关闭胶囊、居中的标题、表单的确认胶囊。
- 面板是在问一个决定而不是填表时，以 `SheetButtons(primary, secondary, tertiary)` 结尾：两个 44dp 胶囊并排，三个或文字过长时纵向排列。
- **问一个值**（名称、行号、口令）用居中的 `FormDialog`：标题、内容、取消 | 确认；超过两个字段改用 `PanelSheet`。
- **输入框的圆角跟随它所在的容器。** 直接放在页面或面板上的输入框本身就是一张卡片，用分组卡片的圆角（`PageDefaults.FieldCornerRadius`，24dp）；卡片和对话框里的输入框保持 COUI 自带的 10dp。聊天输入栏是 40dp 的胶囊。不要在同一屏出现第三种。
- **输入框要看得见。** COUI 的矩形输入框是透明底加细描边，为白色表面设计；直接放在灰色页面底色上几乎消失。`TextInput` 在页面和面板上会自动填卡片色（`LocalOnPageColor`），在卡片和对话框里保持透明。
- 菜单用 `GlassMenu`：所有行属于同一组，行与行之间没有分隔条。

## 8. 按压反馈与动效

- 每个可点按的元素，反馈都裁剪在它可见的形状之内。自己写 `clickable` 时，`.clip(shape)` 要放在点击修饰符**之前**；圆形按钮用 `CircleShape`，卡片首末行用卡片的圆角。纯手势目标用 `indication = null`。永远不要出现方形的按压蒙版。
- 展开 / 收起用 `ExpandBody`：内容从行的下沿长出来并淡入（弹簧），行上的 `ExpandArrow` 同步旋转。
- 列表项的出现、移动用 `animateItem()`。
- 页面切换用共享 X 轴的位移加淡入淡出；系统返回手势应当能拖动返回动画（predictive back）。

## 9. 无障碍

- 触摸目标不小于 40–48dp（28dp 的小按钮由 Compose 自动扩展触摸区域）。
- 图标按钮必须有 `contentDescription`；可折叠项提供状态描述；分组标题用 `SectionHeader`（朗读为标题）。
- 连接状态、错误、提示条用 live region 礼貌播报（`NoticeBanner`、`ErrorState`、`LoadingState` 已内置）。
- 字号用 sp，尊重系统字体缩放。

## 10. 文案与语言

`coui-patterns` 自带的少量文案（返回、取消、关闭）提供英文和简体中文资源，随应用语言切换。其余文案由调用方传入。
