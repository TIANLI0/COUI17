# Changelog

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
  `NoticeBanner`, `StatusPill`), `ExpandBody` and `TextInput`.
- A gallery application.
- Maven publications `io.github.tianli0.coui17:<module>` (local repository).
- CI, and a daily workflow that follows upstream and merges it when it is safe to.
