# Upstream

COUI17 is a downstream of [Costben/coui17](https://github.com/Costben/coui17). This page says what
comes from there, what is this repository's own, what was changed, and how the two are kept in step.

## Lineage

| Order | Repository | Role |
| :--- | :--- | :--- |
| 1 | [`compose-miuix-ui/miuix`](https://github.com/compose-miuix-ui/miuix) | The original project: a MIUI / HyperOS styled Compose Multiplatform library. |
| 2 | [`suqi8/coui`](https://github.com/suqi8/coui) | Rethemes Miuix to ColorOS 16, renames modules to `coui-*` and packages to `io.github.suqi8.coui.kmp`. |
| 3 | [`Costben/coui17`](https://github.com/Costben/coui17) | Continues it towards ColorOS 17. **The direct upstream of this repository.** |
| 4 | this repository | An Android-focused distribution of (3) with its own build, a patterns module, a gallery, and automatic syncing. |

All of them are Apache-2.0; `NOTICE` and `THIRD-PARTY-LICENSES.md` carry the attributions. Files in
`coui-ui/src/*/kotlin/io/github/suqi8/coui/kmp/kit/**` are ported from COUI-Kit and stay under
MPL-2.0 with their headers; this repository is their published source.

## What is whose

| Path | Origin |
| :--- | :--- |
| `coui-{shader,blur,squircle,core,ui,preference,icons}/src/{commonMain,androidMain}` | Upstream, at the commit in [`UPSTREAM_SHA`](UPSTREAM_SHA), plus `patches/*.patch` |
| `LICENSE`, `THIRD-PARTY-LICENSES.md` | Upstream |
| `coui-*/build.gradle.kts`, `settings.gradle.kts`, `gradle/` | This repository (Android-only Kotlin Multiplatform modules, own toolchain versions) |
| `coui-patterns/`, `gallery/`, `docs/`, `tools/`, `.github/` | This repository |

Not taken from upstream: its other targets (iOS, desktop, web), `coui-nav` / `coui-navigation3-ui`,
its sample apps, tests, documentation site and build logic. The package prefix
`io.github.suqi8.coui.kmp` is kept so upstream changes apply without rewriting, and so code written
against upstream compiles against this repository unchanged.

The modules build with AGP 9.5 / Kotlin 2.4.21 / Compose Multiplatform 1.12 (upstream: AGP 9.3 /
Kotlin 2.4.10 / CMP 1.11). `org.lsposed.hiddenapibypass` is not a dependency here (patch 0001).

## Changes to upstream sources

Each change is one file in `patches/`, re-applied in file-name order on every sync.

### 0001 — no hidden-API bypass

- **File:** `coui-ui/src/androidMain/…/basic/BottomBarBackdropRenderer.android.kt`
- **Upstream:** on Android 16 devices *without* the Oplus framework, the native bottom-bar material
  called the hidden `View.setBackdropRenderEffect(RenderEffect)` through
  `org.lsposed.hiddenapibypass.HiddenApiBypass`.
- **Here:** that fallback and its private helper class are removed, and the dependency is not
  declared. Bypassing the non-SDK-interface restrictions is a Play-policy and stability risk.
- **Effect:** on devices without the Oplus framework `rememberPlatformBottomBarBackdropRenderer()`
  returns `null` and the bar is drawn by the portable Compose blur engine, as it already was below
  API 36. The Oplus framework path is unchanged (and off by default, see `COUI17Theme`).

### 0002 — localizable sheet close label

- **File:** `coui-ui/src/commonMain/…/layout/BottomSheetContentLayout.kt`
- **Upstream:** the close capsule of `WindowBottomSheet` is hard-coded as `"关闭"`.
- **Here:** a public `LocalSheetCloseLabel` composition local (default `"关闭"`, so behaviour is
  unchanged for apps that do not provide it). `COUI17Theme` provides it from string resources.

### 0003 — dialog button fill without blur

- **File:** `coui-ui/src/commonMain/…/layout/DialogButtonBar.kt`
- **Upstream:** `DialogButtonBarDefaults.buttonFill()` always returns the frosted-panel capsule
  (`#AAFFFFFF` light / `#33FFFFFF` dark). When the panel falls back to its flat fill, the light
  panel is plain white and the capsule disappears: cancel / confirm read as bare text in light
  theme only.
- **Here:** when there is no backdrop or blur is off, `buttonFill()` returns the non-blur recipe
  (`#14000000` / `#1AFFFFFF`). The frosted path is unchanged.

### 0004 — small top app bar title marquee

- **File:** `coui-ui/src/commonMain/…/basic/TopAppBar.kt`
- **Upstream:** `SmallTopAppBar` cuts a long title or subtitle with an ellipsis.
- **Here:** a public `LocalTopAppBarTitleMarquee` composition local (default `false`). When true,
  title and subtitle use `basicMarquee`; text that fits does not move. `COUI17Theme` turns it on.

### 0005 — bottom bar capsule inner fade

- **Files:** `coui-ui/src/commonMain/…/basic/BottomFloatingNavigationBar.kt`, `…/basic/MaterialShadowEdge.kt`
- **Upstream:** the Compose glass of the floating bottom bar draws the rim line of
  `TYPE_FRAMEWORK_CAPSULE_3` only, and the expansion view fades on the bouncing geometry progress.
- **Here:** the inward fade of the system material (fade-in 0.1 light / 0.03 dark) is drawn under
  the icons (`materialInnerGlow(behindContent)`), and the expansion view fades on its own spring
  (response 0.35, no bounce).

### 0006 — alignment with the ColorOS 17 COUI SDK

- **Files:** `basic/Button.kt`, `basic/HighlightedIconButton.kt`, `basic/COUITouchMotion.kt`,
  `basic/ListPopup.kt`, `basic/MaterialShadowEdge.kt`, `basic/SpotLightEffect.kt`, `basic/Switch.kt`,
  `kit/topappbar/CouiTopAppBarButtonDefaults.kt`, `layout/DialogContentLayout.kt`,
  `utils/COUIIndication.kt`, `utils/CouiHapticEffect.kt` and a new
  `androidMain/…/utils/CouiClickSound.android.kt`.
- **Here:** button rims and press lights by material type, the touch-motion springs, popup enter
  damping and its captured-page tint, the dialog tint, a quick tap's indication reaching 70 %, the
  switch's dark press colours and system click sound — measured against the COUI SDK. The dark page
  stays `#303030` (upstream's value), not the SDK's `#000000`.

### 0007 — expand arrow duration

- **File:** `coui-ui/src/commonMain/…/basic/ExpandArrow.kt`
- **Here:** `ExpandArrow` takes `durationMillis` (default unchanged), so `coui-patterns`' 300ms
  expand motion turns the arrow in the same time.

### 0008 — live host-window backdrop for window dialogs and sheets

- **Files:** `coui-blur/src/commonMain/…/blur/ImageBackdrop.kt`,
  `coui-ui/src/{commonMain,androidMain}/…/utils/HostWindowBackdrop*.kt`
- **Upstream:** a `Window*` dialog or sheet captures the host window once when it opens and frosts
  that still.
- **Here:** the host is captured again after each of its draws (at most every 33 ms, one copy in
  flight, three reused bitmaps); `ImageBackdrop` holds the capture as state read while drawing, so
  only the consumers redraw. A host that does not draw is not copied again, and a draw that lands
  while a copy is in flight is copied after it, so the panel ends on the host's last frame
  (`HostCaptureScheduler`, unit-tested in `coui-ui/src/androidHostTest`).

### 0009 — row text limits, localized top-tip close label, search keyboard type

- **Files:** `coui-ui/src/commonMain/…/basic/{Component,TopTips,SearchBar}.kt`
- **Upstream:** `BasicComponent` has no way to cap its title or summary, so a user-named item (a
  chat title, a file path) can grow a list row to many lines; the `TopTips` close button announces a
  hard-coded English "Close" in every language; the `SearchBar` field always uses a text
  keyboard, so a CJK input method composes file names ("README_ZH" became "README＿").
- **Here:** `BasicComponent` takes `titleMaxLines` / `summaryMaxLines` (default unlimited, ending in
  an ellipsis when set), added at the end of the parameter list so positional calls are unchanged.
  The `TopTips` close button reads its description from `LocalSheetCloseLabel` (patch 0002), the
  label the app already localizes. `LocalSearchBarKeyboardType` (default `KeyboardType.Text`, so
  unchanged unless provided) sets the field's keyboard; with a non-text type autocorrect is off.

### 0010 — black dark-theme page

- **File:** `coui-ui/src/commonMain/…/basic/AppBarBlurOverlay.kt`
- **Upstream:** `AppBarBlurOverlayDefaults.DarkContainerColor` is `#303030`, measured on a device in
  ColorOS's softest dark style; pages, headers and sheets take it, and cards (10 % white) land at
  `#454545`.
- **Here:** black, as ColorOS's default dark style and its own apps (File Manager): cards land at
  about `#1A1A1A` and nested surfaces such as code blocks no longer read as holes.

0001–0004 are small, additive and off by default where they add behaviour, so they are candidates
to send upstream; once upstream has one, delete its patch file.

**Every change to the upstream-owned paths must be a patch here.** The sync replaces those paths
wholesale, so an edit made directly to them is reverted by the next sync. `tools/check_patches.py`
rebuilds upstream at `UPSTREAM_SHA` plus the patches and fails on any difference; CI runs it on
every push, and the sync workflow runs it before it syncs. To record a change, commit it, then
append `git diff <before> <after> -- <upstream paths>` as the next numbered patch.

## Staying in step: automatic sync

[`.github/workflows/upstream-sync.yml`](.github/workflows/upstream-sync.yml) runs every day (and
from the Actions tab, optionally for a specific commit). When upstream's `main` has moved past
`UPSTREAM_SHA` it:

1. replaces the upstream-owned paths with upstream's and re-applies `patches/*.patch`
   (`tools/sync_upstream.py`);
2. checks what came in (`tools/upstream_review.py`): added lines that use a hidden-API bypass,
   reflection, vendor-private framework classes, network / file / process access or broadcast
   receivers, and any change to upstream's dependency catalog or module build files;
3. runs the unit tests, publishes every module to the local Maven repository and builds the gallery.

| Outcome | What happens |
| :--- | :--- |
| Patches apply, build is green, the check finds nothing | The sync is committed and **pushed to `main`** by the workflow. |
| The build fails, or the check has findings | A pull request `upstream-sync/<sha>` is opened with the failure / findings and the upstream commit list. Nothing reaches `main` until a person merges it. |
| A patch no longer applies | An issue is opened with the `git apply` error. Rebase or drop the patch, then re-run. |

The check exists because a sync brings third-party code into `main` with no person reading it. It
is a filter for the things this distribution promises not to do, not a security review: read the
commit list in the run summary from time to time.

Commits pushed by the workflow use the repository's `GITHUB_TOKEN`, so they do not start another CI
run; the sync job has already built and tested exactly that tree.

## Syncing by hand

```bash
git clone https://github.com/Costben/coui17 ../coui17-upstream
python -I tools/check_patches.py --repo ../coui17-upstream   # nothing local is left out of patches/
python -I tools/sync_upstream.py --repo ../coui17-upstream --sha <full-40-char-sha>
git add -A
python -I tools/upstream_review.py --upstream ../coui17-upstream --old <previous-sha> --new <full-sha>
./gradlew :coui-patterns:testDebugUnitTest :coui-ui:testAndroidHostTest publishToMavenLocal :gallery:assembleDebug
python -I tools/gen_component_catalog.py
```

Then compare upstream's `NOTICE` with the section kept in this repository's, and check
upstream's `gradle/libs.versions.toml` for dependency changes that the module build files here need.
