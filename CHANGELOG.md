# Changelog

## Unreleased

- Local changes to upstream sources are recorded as patches 0005–0008, and
  `tools/check_patches.py` (run in CI and before each sync) fails when the upstream-owned sources
  differ from `UPSTREAM_SHA` plus `patches/`. Before this, the next sync would have reverted
  them.
- Window dialogs and sheets re-sample the host window after it draws, and always end on the
  host's last frame (`HostCaptureScheduler`).
- `GlassGroup` keeps one union shape between member moves, and does not redraw on positioning
  callbacks that change nothing.
- `InputPill` no longer recomposes on each frame of its send-ready animation.
- `BackArrow` mirrors in RTL. `BackArrow` and `ToolbarIcon` take a `modifier`. `COUI17Theme`
  provides localized expand / collapse state words.
- JVM host tests for `coui-ui` (`:coui-ui:testAndroidHostTest`).
- Health-check report: `docs/health-check-2026-11.md`.
- `BasicComponent` takes `titleMaxLines` / `summaryMaxLines`; the `TopTips` close button is
  announced with the localized `LocalSheetCloseLabel` instead of English "Close";
  `LocalSearchBarKeyboardType` picks the `SearchBar` keyboard (patch 0009).
- Dark theme pages are black like ColorOS's default dark style, not `#303030` (patch 0010).
- `coui-nav` from upstream: a back-stack host (`NavDisplay`) whose push / pop, system predictive
  back and in-page swipe drive one position, so the gesture follows the finger. Its swipe leaves
  drags to the page (horizontal scrollers, selection) that claim them first (patch 0011).

## 0.1.0

First release as a standalone repository.

- The seven library modules of [Costben/coui17](https://github.com/Costben/coui17) at `7135f75`
  (`coui-shader`, `coui-blur`, `coui-squircle`, `coui-core`, `coui-ui`, `coui-preference`,
  `coui-icons`) as Android-only Kotlin Multiplatform modules, with four changes (`patches/`,
  described in `UPSTREAM.md`): no hidden-API bypass, a localizable sheet close label, a visible
  dialog button fill without blur, and an optional title marquee.
- New module `coui-patterns`: `COUI17Theme` and `Accent`, the `Page` scaffold with its list
  helpers, `GroupCard`, the button tiers (`SmallButton`, `LinkButton`, `ActionTone`), `PanelSheet`
  / `SheetButtons` / `ConfirmDialog` / `FullScreenDialog`, `GlassMenu` and `rememberPopupGlass`,
  `glassSurface` / `glassHeader`, state views (`LoadingState`, `ErrorState`, `EmptyMessage`,
  `NoticeBanner`, `StatusPill`), `ExpandBody` and `TextInput`; the composer (`InputPill`,
  `ComposerKey`, `RoundIconButton`); dense rows and capsules (`CollapsibleRow`, `InfoChip`,
  `StatFigure`, `StatusDot`, `Modifier.segment`); cards (`ChatBubble`, `AttentionCard`,
  `TimelineCard`), `StepButtons`, `FormDialog`, `groupRowShape` / `LazyGroupRow`, a loading
  state for `SmallButton`, and `LocalOnPageColor` (a `TextInput` fills itself with the card colour
  when it sits directly on the page colour).
- A gallery application.
- Maven publications `io.github.tianli0.coui17:<module>` (local repository).
- CI, and a daily workflow that follows upstream and merges it when it is safe to.
- One corner radius per container for text fields (`PageDefaults.FieldCornerRadius` on the page
  colour) and one shadow for floating controls (`Modifier.floatingHalo`, `glassSurface(halo = true)`).
