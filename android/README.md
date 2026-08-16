# RipMe for Android (preview)

A native Android app around the same ripper engine as the desktop build (~114 rippers minus
`InstagramRipper` - see "What's shared" below), with a Compose Material3 UI: Rip / Queue / History
/ Log / Settings, a foreground service so rips survive backgrounding, and a "copy to Downloads"
export so a rip outlives uninstalling the app. This is a separate Gradle build under `android/` -
the repo root build (`../build.gradle.kts`) never applies the Android Gradle Plugin and never
includes this directory.

## Prerequisites

| Tool | Version used to build this | Notes |
| --- | --- | --- |
| JDK | 26 | Any JDK 26 works (e.g. Homebrew's `openjdk`); Gradle's daemon just needs to launch on it. |
| Gradle | 9.7.0, the system `gradle` command | There is **no Gradle wrapper checked in under `android/`** - use whatever `gradle` resolves to on your `PATH`, or install 9.7.0 specifically if a different Gradle is your default. |
| Android SDK | platforms 34 and 36, build-tools 36.x, licenses accepted | `sdkmanager --licenses`, then `sdkmanager "platforms;android-36" "build-tools;36.0.0"` (adjust the exact build-tools revision to whatever's current). |
| AGP | 9.3.1 (pinned in `app/build.gradle.kts` / `core/build.gradle.kts`) | Applied per-module, not via a version catalog. |

**AGP 9 gotcha**: AGP 9 has built-in Kotlin support. Do not add `kotlin("android")` to either
module - it hard-fails the build if present alongside `com.android.application`/`com.android.library`.
Only the Compose compiler plugin (`org.jetbrains.kotlin.plugin.compose`) is applied separately.

**compileSdk is pinned at 36, not the newest available.** `android-37` is not published in any
stable SDK channel; a dependency with `minCompileSdk=37` is simply unusable here. Likewise the
AndroidX versions in `app/build.gradle.kts` (`core-ktx:1.18.0`, `compose-bom:2026.06.01`,
`lifecycle:2.10.0`, …) are pinned to the newest releases that are still compatible with compileSdk
36 - each was checked against its AAR's `aar-metadata.properties`. Do not bump any of these without
re-checking that constraint; see that file's comments for the specifics.

### `local.properties`

Gitignored (machine-specific), and not created for you. Create `android/local.properties`:

```properties
sdk.dir=/absolute/path/to/your/Android/sdk
```

(`ANDROID_HOME` being set is not assumed - this file is the only thing that has to point at your
SDK.)

## Build

```bash
gradle -p android :app:assembleDebug
```

APK lands at `android/app/build/outputs/apk/debug/app-debug.apk`. `gradle -p android :core:test`
runs `RipperRegistryTest` (offline, deterministic - asserts the generated ripper registry resolves
a handful of real URLs to the expected ripper classes, and that `InstagramRipper` is absent).

## Run

An emulator or a real device (`minSdk` 26 / Android 8.0+) both work; internet access is required
(the rippers make real HTTP requests). To use the emulator from the command line rather than
Android Studio:

```bash
# Create once, if you don't already have a suitable AVD:
#   ~/Library/Android/sdk/cmdline-tools/latest/bin/avdmanager create avd \
#     -n Medium_Phone_API_36.1 -k "system-images;android-36;google_apis;arm64-v8a"
~/Library/Android/sdk/emulator/emulator -avd Medium_Phone_API_36.1 -no-window -no-audio -no-boot-anim &

~/Library/Android/sdk/platform-tools/adb install -r android/app/build/outputs/apk/debug/app-debug.apk
~/Library/Android/sdk/platform-tools/adb shell am start -n com.rarchives.ripme.android/.MainActivity
```

(`adb`/`emulator` are not assumed to be on `PATH` - full paths above are what worked during
development on macOS; adjust for your SDK location and OS.)

## Architecture

```
android/
  settings.gradle.kts    google() + mavenCentral(); include(":core", ":app")
  gradle.properties       android.useAndroidX=true, org.gradle.jvmargs=-Xmx4g
  local.properties        sdk.dir=… (gitignored, not checked in)
  core/                   java-library: the shared engine, compiled at bytecode release 17
  app/                    AGP application: Compose UI, bootstrap, foreground service
```

### `:core` - what's shared with the desktop build, and what isn't

`:core` compiles the **same source files** as the desktop build
(`src/main/java/com/rarchives/ripme/**`), via a filtered `Sync` task
(`syncSharedJava` in `core/build.gradle.kts`) rather than a bare source-directory reference - see
that task's comment for why a plain `srcDir` can't selectively exclude files without also deleting
this module's own same-named shim classes. Excluded from the copy: `App.java` (Swing + CLI entry
point), `ui/MainWindow.java` / `ui/UpdateUtils.java` / `ui/ClipboardUtils.java` and the Swing
mouse-listener classes (all Swing-only), `uiUtils/**` (Swing), and `ripper/rippers/InstagramRipper.java`
(depends on GraalVM's JS engine, which doesn't dex).

Three tiny shim classes live in `:core`'s own source (same package/class names as the files they
replace) so the rest of the shared code compiles unchanged: `App.stringToAppendToFoldername`,
`ui.MainWindow.addUrlToQueue` (repointed at a settable listener the Android app installs, see
`RipMeApplication`), and `ui.UpdateUtils.getThisJarVersion` (returns the app's own version string,
used to build `RedditRipper`'s User-Agent). Each has a header comment naming the desktop class it
stands in for.

Ripper discovery is different by necessity: the desktop scans the runtime classpath/jar for
`AbstractRipper` subclasses, which finds nothing inside a DEX (no classpath to enumerate). A
generator Gradle task (`generateRipperRegistry`) lists the same ripper source directories at
*build* time instead and writes `com.rarchives.ripme.android.RipperRegistry`, whose `getRipper(URL)`
mirrors `AbstractRipper.getRipper`'s algorithm against that fixed, alphabetised list. It regenerates
on every build, so it stays in sync as upstream adds rippers - nothing to maintain by hand.

One dependency-scope wrinkle worth knowing if you touch `:core`'s dependencies: `log4j-core` is
shipped as `implementation` (not `compileOnly`, despite no Android code path ever calling
`Utils.configureLogger()`, the only method that references it) because HotSpot's class verifier
resolves those references just to *load* `Utils.class` - confirmed by a real `:core:test` failure
when it was `compileOnly`. `:app` then excludes `log4j-core` again from its own dependency on
`:core` and relies on ART's different (method-level, not whole-class) verifier being fine with that
combination - confirmed on-device, not assumed; see `app/build.gradle.kts`'s comment for the full
chain of evidence.

### The one shared-tree patch

`src/main/java/com/rarchives/ripme/utils/Utils.java`'s `getConfigDir()` honours a
`ripme.config.dir` system property before falling through to the desktop's OS-specific paths (which
resolve under `$HOME`, not writable on Android). This is the *only* change to shared source in this
whole feature - `gradle build` at the repo root still passes unmodified, because nothing else under
`src/` differs from what the desktop build already compiles.

### `:app`

Bootstrap (`RipMeApplication.onCreate`) sets that system property to `filesDir/config` before
anything else touches `Utils` (whose static initialiser loads `rip.properties`), then points
`rips.directory` at `getExternalFilesDir(DIRECTORY_DOWNLOADS)/rips` (app-scoped external storage -
no `WRITE_EXTERNAL_STORAGE` permission needed, but wiped on uninstall, which is what the export
feature below is for), and forces `urls_only.save=false` / `play.sound=false` off since their
underlying desktop code paths (`java.awt.Desktop`, `javax.sound.sampled`) don't exist on Android.

The engine itself (`engine/RipController.kt`, `engine/queue/QueueController.kt`,
`engine/history/HistoryStore.kt`) is a process-level `RipEngine` singleton exposing `StateFlow`s,
ported from the desktop's Compose-GUI controllers of the same names
(`src/main/kotlin/com/rarchives/ripme/ui/compose/**`) with `Compose State`/`SnapshotStateList`
swapped for `StateFlow` so `RipService` (a plain, non-`@Composable` foreground service) can observe
rip progress without depending on the Compose runtime. `RipService` starts when the queue starts
draining and stops itself (debounced) once it observes both `busy=false` and an empty queue - see
that file's header comment for why the stop decision specifically needs debouncing.

The five `NavigationBar` destinations (`ui/rip`, `ui/queue`, `ui/history`, `ui/log`,
`ui/settings`) are the phone equivalent of the desktop's `MainScreen` + togglable side panels -
Rip is a destination of equal standing rather than the permanently-visible base layout, since a
phone doesn't have the width for both at once. Behaviour is preserved 1:1 from each desktop
screen's port (add/remove/clear queue, history list/re-rip/clear, coloured log lines, the same
config keys); layout is redrawn for a single narrow column instead of desktop's two-column grids
and fixed-width table.

### Export to Downloads

`export/MediaStoreExporter.kt` copies a finished rip's files into `MediaStore.Downloads`, under
`Download/RipMe/<album folder name>` - reusing the ripper's own (already filesystem-safe) folder
name rather than re-sanitising a title. Surfaced both on `HistoryScreen`'s per-row action and on
`RipScreen`'s "rip complete" card. Requires Android 10 (API 29) for the `MediaStore.Downloads`
collection this uses; on API 26-28 the action reports that plainly rather than attempting a legacy
`WRITE_EXTERNAL_STORAGE` write (out of scope for this preview - see Known Gaps).

## Notable findings

Two things turned up while porting the engine that weren't obvious going in, and are worth
knowing if you're touching `:core`'s dependencies or `RipService`.

### `log4j-core`: `compileOnly` doesn't work, and here's why

The first pass at `:core/build.gradle.kts` shipped `log4j-core` as `compileOnly`, on the
reasoning that `Utils` only references `org.apache.logging.log4j.core.*` inside
`configureLogger()`, and no Android code path ever calls that method. That reasoning is correct
and the conclusion is still wrong: JVM class verification is a whole-class, link-time step, not
scoped to the methods that actually run. HotSpot's verifier resolves `configureLogger()`'s
`ConsoleAppender$Target` reference while verifying `Utils.class` as a whole, the moment `Utils` is
first loaded - regardless of whether `configureLogger()` ever runs. Since the generated
`RipperRegistry.getRipper(URL)` reflectively `Class.forName()`s every candidate ripper in turn,
and nearly every ripper's constructor touches `Utils` (via `AbstractRipper`'s
`Utils.getURLHistoryFile()` field initialiser), the *first* ripper class loaded threw
`NoClassDefFoundError` - and because that's an `Error`, not an `Exception`, the registry's
`catch (Exception e)` didn't swallow it; it killed the whole `getRipper()` call. Confirmed three
ways before touching the build file: the real `:core:test` failure, a plain `java -cp` run outside
Gradle's test-worker classloader, and forcing `Class.forName("com.rarchives.ripme.utils.Utils")`
alone with zero other classes involved.

`:core` now ships `log4j-core` as `implementation` for real (see its `build.gradle.kts`). That
left an open question for `:app`: ART might verify the same way HotSpot does, in which case the
APK would need to ship `log4j-core` too. It doesn't - `:app` excludes `log4j-core` again
(`implementation(project(":core")) { exclude(...) }`) and relies on ART verifying method-by-method
rather than whole-class, soft-failing only the individual method that references a missing class.
Confirmed on-device, not assumed: with `log4j-core` absent from the APK, `RipperRegistry.getRipper`
reflectively constructed on the order of 119 ripper classes end to end - including `E621Ripper`,
whose `AbstractRipper` superclass field initialiser forces `Utils.class` to load and verify on
essentially every attempt - with no `VerifyError`/`NoClassDefFoundError`, and that ripper then ran
a real rip to `RIP_COMPLETE`. `logcat` shows log4j-api's own graceful fallback
(`Log4j API could not find a logging provider`) rather than a crash.

### `RipService`: a stop-notification race

Early testing surfaced a service that occasionally left a permanently stale, un-stoppable
notification on screen. Root cause: `QueueController`'s drain loop can go `busy=false` with an
empty queue for a few milliseconds *between* two rips - most visibly when one URL fails to resolve
(`beginRip` returns `false` instantly, before ever setting `busy=true`) and the next queued URL
starts right after. `RipEngine`'s `onRipStarted` hook calls `startForegroundService` for *every*
rip attempt, including that instantly-failing one. An un-debounced stop decision reacted to that
momentary idle gap by stopping the freshly created service instance - cancelling its collector in
`onDestroy` - before the very next rip's `startForegroundService` call could even be delivered,
orphaning that second rip with no collector left to ever stop its notification.

Reproduced deterministically, then fixed with a `.debounce(500)` on the stop decision only (the
notification's live content updates stay un-debounced, so progress still looks immediate) - see
the comment at that call site in `RipService.kt`. A real new rip's `busy=true` arrives well within
500ms and the stop decision simply never fires; a genuinely idle queue still stops promptly once
the debounce window elapses.

## Known Gaps

- **No `InstagramRipper`.** Its dependency on GraalVM's JS engine doesn't dex; excluded from both
  `:core`'s synced source and its generated ripper registry.
- **No in-app updates.** `UpdateUtils.getThisJarVersion()` is stubbed to return the app's own
  version (for `RedditRipper`'s User-Agent); the desktop's actual update-check/self-replace flow
  has no Android equivalent, and Settings has no update-check control.
- **No SAF folder picker.** The rips directory is fixed at `getExternalFilesDir(DIRECTORY_DOWNLOADS)/rips`
  and shown read-only in Settings; letting a user redirect it to arbitrary shared storage would need
  the core's file IO abstracted behind an interface first (`AbstractRipper` uses `java.nio.file`
  directly throughout).
- **No R8/minification.** `isMinifyEnabled = false`. The generated `RipperRegistry` resolves rippers
  via plain reflection (`Class.forName` + a `URL`-arg constructor) - safe only because nothing
  renames or strips those classes today. Turning on minification needs keep rules for
  `com.rarchives.ripme.ripper.rippers.**` first.
- **Settings covers only the phone-relevant config keys**: threads, timeout, retries, overwrite,
  album titles, save order, save descriptions, plus a read-only rips-directory display. Dropped
  versus the desktop's config screen: log level (no `log4j-core` in the APK - see `:core`'s
  dependency-scope note above; ART tolerates its *absence* at runtime, but there's still no UI hook
  for a level nothing consumes), sound (`play.sound` is forced off at bootstrap), clipboard-autorip
  (`ClipboardUtils` is Swing/AWT-only, excluded from `:core`'s synced source), language, SSL/window/
  URL-history toggles, cookies configuration, and the URL-list file import (needs a picker - see the
  SAF gap above).
- **`HistoryEntry.dir` doesn't survive an app restart.** This is a gap in the *shared* Java
  `HistoryEntry`/`History` classes (`src/main/java/com/rarchives/ripme/ui/`), not Android-specific:
  `HistoryEntry.toJSON()` never writes a `"dir"` key, even though `fromJSON()` reads one if present.
  A history row created earlier in the same app process has its folder path and offers "Export to
  Downloads"; the same row reloaded from `history.json` after the app was killed and relaunched has
  lost track of its folder and shows "Folder unknown for this entry" instead (re-ripping the same
  URL repopulates it). Not fixed here since it would mean touching shared source beyond the one
  sanctioned `Utils.getConfigDir()` patch.
- **e621 currently scrapes zero images** (a site/selector mismatch in `E621Ripper` - it resolves
  the URL, paginates and completes normally, just downloads nothing). This affects the desktop
  build identically; it's not an Android-specific regression, just something you'll notice if you
  use e621 as a smoke-test URL.

## Testing

- `gradle -p android :core:test` - offline, deterministic, no emulator needed.
- `gradle -p android :app:assembleDebug` - compiles the whole app; review any new D8 "missing
  class" warnings against the `-dontwarn` list already in `app/proguard-rules.pro` (desktop-only
  APIs the shared engine references but never calls on Android: `java.awt.**`, `javax.swing.**`,
  `javax.sound.**`, `org.apache.logging.log4j.core.**`).
- `gradle build` at the **repo root** still passes unmodified - the only shared-tree change is the
  `Utils.getConfigDir()` patch, and this `android/` build is entirely separate from the root one.
