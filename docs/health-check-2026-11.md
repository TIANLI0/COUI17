# Health check, November 2026

A standalone review of the library: design, performance and the upstream process. It covers
`coui-patterns` in full; upstream's blur pipeline (`coui-blur`, `GlassSurface.kt`), the window
backdrop (`HostWindowBackdrop*`, `ImageBackdrop`) and the components that the SwitchTether
session page uses; and the sync tooling.

There was no device or emulator. Every performance statement below comes from reading the code,
from the Compose compiler reports, or from JVM tests. None of it is a frame-time measurement.

Severity:
- **Critical**: loses work or ships wrong code without anyone noticing.
- **High**: real cost or defect on the main consumer's hot path.
- **Medium**: measurable waste, or a wrong result in some cases.
- **Low**: polish.

## Findings and fixes

| # | Severity | Finding | Status |
| :- | :- | :- | :- |
| 1 | Critical | Five commits (`a1e4725`, `ad66fb0`, `598a0d9`, `106bd7c`, `854e118`) edited upstream-owned sources directly. `sync_upstream.py` replaces those paths and re-applies only `patches/`. Re-running the sync at the same `UPSTREAM_SHA` in a scratch worktree reverted **17 files**: the COUI SDK alignment, the bottom bar fade, the arrow duration and the live host-window backdrop. The daily workflow would have pushed that revert to `main` with a green build. | Fixed: patches 0005–0008; `tools/check_patches.py`, run by CI and by the sync workflow before it syncs |
| 2 | High | Live host-window resampling (`854e118`): a host draw that arrived while a copy was in flight was dropped, with nothing re-armed. When streaming stopped, the panel could keep showing the second-to-last frame. Every host frame also posted a `Runnable`, and the delayed retry was not removed on dispose. | Fixed: `HostCaptureScheduler` (8 JVM tests) |
| 3 | Medium | `GlassGroup` built a new `UnionShape` and list on every outline request, which happens every draw, so every frame while the page streams. With a squircle (path) member the blur's outline cache could never hit, so a `Path` was rebuilt per frame. | Fixed: the union is a derived state, one instance until a member changes |
| 4 | Medium | `GlassGroup` members compared `Outline`s to skip writes. `Outline.Generic` has no value equality, so each positioning callback rewrote the state map and redrew the blur. During an IME animation that is every frame. | Fixed: members compare the geometry they were built from (4 JVM tests) |
| 5 | Medium | `InputPill` lerped the send arrow's tint from its animation in composition. Each frame of the ready transition recomposed the whole composer, text field and glass included, and re-ran the glass node's `update()` (see 8). | Fixed: the arrow is tinted in `drawBehind` |
| 6 | Low | The back arrow did not mirror in RTL: coui icons are not auto-mirrored. | Fixed |
| 7 | Low | `CollapsibleRow` announced "expanded" / "collapsed" in English unless the app provided its own words. `BackArrow` and `ToolbarIcon` had no `Modifier` parameter. The docs promised a halo or press light on `ComposerKey` and `RoundIconButton` that they no longer draw, and gave 400ms for `ExpandBody`, which runs 300ms. | Fixed |

### Left as is, with reasons

These are in upstream's pipeline. Changing GPU compositing without a device to compare output on
risks edge artefacts in every glass surface, and SwitchTether ships on this code. Each one needs
an on-device A/B before it becomes a patch.

| # | Severity | Finding | Suggested fix |
| :- | :- | :- | :- |
| 8 | High | **Noise-dither layer is recorded at the padded size.** `DrawBackdropModifier.renderBlurLevel` / `drawUpscaledLayer` (`fullWidth = size + 2 × padding`) records the noise layer at that size, plus a shader pass over it. With a 24dp blur the padding is `13 × sf` px (104px at sf = 8). For a 40dp key at 3× density that is 328² px against 120² px visible, about 7.5×. For a composer row it is about 2.8×. Clipping then throws the excess away. | Record the noise layer at `size + 2m` and translate by `padding − m`. The margin `m` must cover the sub-pixel residual (≤ `sf`) and the inverse display scale of an animating popup (`renderBlurInto`). The shader samples only `child.eval(xy)`, so cropping is otherwise exact. |
| 9 | High | Every glass surface draws inside an `Offscreen` layer with a shape clip (`layoutLayerBlock`), plus ½-res and ⅛-res cascade layers. This is about 4 render targets per surface per frame while the backdrop changes. | Try `CompositingStrategy.Auto` when `contentBlendMode == SrcOver`, and compare anti-aliasing of squircle clips on a device. |
| 10 | Medium | `LayerBackdrop` draws its content twice on the CPU while it has a consumer: once to screen, once into the layer. Recording is correctly skipped when nothing consumes it. | Record once and draw the layer to screen. Upstream's platform-view and custom-`onDraw` cases make this unsafe as a blind change. |
| 11 | Medium | `textureEffect` / `progressiveTextureBlur` create new lambdas per call, and `DrawBackdropElement.equals` compares them by identity. Every recomposition of a glass caller therefore runs `update()` → `observeNodeState(notifyLayoutAndDraw = true)`. | `remember` the modifier in `couiGlassSurface` / `couiGlassHeader`. The cost is mostly a re-measure, because content changes re-record that layer anyway. |
| 12 | Low | `layoutCoordinates` uses `neverEqualPolicy`, so each position callback redraws the glass. The highlight resolves `createOutline` on the raw shape on each draw. | Compare position before invalidating; pass the cached `shapeProvider.shape`. |
| 13 | Low | The host-window resampling copies the whole host window (¼ scale, about 0.86 MB for 1240 × 2772) up to 30 times a second while the host draws, and the panel re-blurs each copy. | This is by design: an idle host costs nothing. If a device runs warm with a panel open over streaming text, raise `MIN_CAPTURE_INTERVAL_MS` (66 ms is still smooth under a 200px blur). |
| 14 | Low | `InfoChip(backdrop: Color?)` uses a colour-typed parameter as a boolean flag ("floating"). | Rename it in a breaking release. It is used by SwitchTether. |
| 15 | Low | `InputPill` uses the value-based `BasicTextField`, which is deprecated in Compose 1.13. | Migrate to `TextFieldState` with an API change. |
| 16 | Low | The touch targets are smaller than 48dp: `ComposerKey` is 40dp, `InfoChip` 30dp and `CollapsibleRow` 44dp. Compose's pointer hit-testing extends small pointer-input nodes to `minimumTouchTargetSize` (48dp), so touch works. Accessibility scanners still report the semantic bounds. | Keep the ColorOS metrics, or add a `minimumInteractiveComponentSize`-style layout wrapper. |

### Checked and fine

- **Compose stability, `coui-patterns`**: 6 classes, all stable or effectively stable.
  `PopupGlass` is uncertain (it holds an `ImageBitmap`). Every restartable named composable is
  skippable (strong skipping): 35 restartable, 0 non-skippable. `module.json` counts 91
  restartable and 66 skippable because it includes composable lambdas.
- **Compose stability, `coui-ui`**: 413 restartable named composables, 0 non-skippable
  (`module.json`, with lambdas: 1156 restartable, 748 skippable). There are 28 unstable
  classes; all are internal motion or renderer engines that are held in `remember`. There are
  73 known-unstable arguments; with strong skipping these compare by instance.
- **Blur caching**: blur, downsample, noise and blend `RenderEffect`s are cached on size and
  parameters. Up to 8 tint layers go through one shader pass. A `LayerBackdrop` with no consumer
  does not record.
- **No idle animation** in the session-page components:
  - `BottomFloatingNavigationBar`, `COUITouchMotion`, `ChipTab` and `Switch` run event-driven
    pointer loops.
  - `Switch` loops only while loading.
  - `rememberDeviceTilt` (a 50Hz sensor) is cleaned up and is used by neither COUI17 nor
    SwitchTether.
- **Listener removal**: the host draw listener is removed after dispose, posted because it
  cannot be removed during dispatch.
- **Bitmap reuse in the three-bitmap ring is safe.** Each copy starts at least two copies, and
  2 × 33 ms, after the frame that last drew the bitmap it overwrites. HWUI is notified of the new
  pixels by the readback; that last step is not verified on a device.
- **Theme**: dark / light comes from the scheme's background luminance. `rememberLayerBackdrop`
  uses `rememberUpdatedState`, so the page colour follows theme switches.
- **Fallbacks**: without blur or below API 33, `glassSurface`, `glassHeader` and `GlassGroup`
  fall back to opaque or gradient fills. `rememberPopupGlass` needs API 26.

## Evidence

- **Patch drift**: `tools/check_patches.py` against `Costben/coui17@7135f75` reported 17
  differing files before patches 0005–0008, and reports none after. Removing 0008 makes it list
  the three host-window files.
- **Compose compiler reports** were produced with a temporary init script that set
  `composeCompiler.reportsDestination` / `metricsDestination`. On Windows the KMP reports land in
  NTFS alternate data streams because the module name contains `:`. Read them with
  `Get-Item -Stream *`.
- **Tests**, all passing:
  - `:coui-patterns:testDebugUnitTest`: 8 tests. There were 4 before this review; the 4 new ones
    are `GlassGroupStateTest`.
  - `:coui-ui:testAndroidHostTest`: 8 tests in `HostCaptureSchedulerTest`, a new source set.
- **Builds**: every library module assembles and `:gallery:assembleDebug` builds on JDK 21 (the
  toolchain the build files require; JDK 25 also starts Gradle).

## Needs a device

- The live panel frost over streaming text: it should end on the last frame and keep its rate
  around 30 fps.
- That PixelCopy into a reused bitmap refreshes the texture on every capture.
- Frame time and temperature for the session page: header blur + `GlassGroup` + a `PanelSheet`
  over it.
- RTL back arrow.
- `InputPill` ready transition: the colour should be unchanged and the composer should no longer
  recompose per frame (check with Layout Inspector's recomposition counts).
- Before patching 8–12: an A/B against the current output.
