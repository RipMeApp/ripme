// :app is the actual Android application: Compose UI + bootstrap + foreground service, on top of
// the shared ripper engine from :core. AGP 9 has built-in Kotlin support - applying
// `kotlin("android")` here as well hard-fails the build (see android/core's neighbour comment in
// the plan this module was built from), so the only plugins are the Android application plugin
// itself and the Compose compiler plugin.
plugins {
  id("com.android.application") version "9.3.1"
  id("org.jetbrains.kotlin.plugin.compose") version "2.4.10"
}

android {
  namespace = "com.rarchives.ripme.android"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.rarchives.ripme.android"
    minSdk = 26     // java.nio.file (used throughout the shared ripper engine) isn't desugared below 26
    targetSdk = 36
    versionCode = 1
    // Tracks the root build's `version` (../../build.gradle.kts) by hand - the two builds are
    // deliberately independent (android/ is a separate Gradle build the root never sees, see
    // settings.gradle.kts), so there's no automated link between them. Bump this alongside that
    // one. Fed to UpdateUtils.setThisJarVersion via BuildConfig.VERSION_NAME (see
    // RipMeApplication) so RedditRipper's User-Agent (RedditRipper.java:71) carries a real version.
    versionName = "1.7.94"
  }

  buildFeatures {
    compose = true
    buildConfig = true   // AGP 9 no longer generates BuildConfig by default; RipMeApplication reads BuildConfig.VERSION_NAME
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildTypes {
    release {
      // The ripper registry (:core's generated RipperRegistry) resolves rippers via plain
      // Class.forName + reflective construction - safe today only because nothing shrinks or
      // renames those classes. Minification is out of scope for v1 (see the plan's "out of
      // scope" section); revisit with R8 keep-rules for com.rarchives.ripme.ripper.rippers.**
      // before ever shipping a minified build.
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  packaging {
    // Several dependencies shared with :core (jsoup/commons-* and friends) ship the same
    // META-INF license/notice files; the default AGP behavior of erroring on any duplicate
    // META-INF resource would otherwise fail packaging. Desktop's shadow-jar merge handles the
    // same fan-in via mergeServiceFiles(); here we just drop the duplicates.
    resources.excludes += setOf(
      "META-INF/LICENSE*",
      "META-INF/NOTICE*",
      "META-INF/DEPENDENCIES",
      "META-INF/*.kotlin_module",
    )
  }
}

dependencies {
  // :core ships log4j-core as `implementation` (see android/core/build.gradle.kts) because
  // HotSpot's *whole-class* verifier resolves Utils.configureLogger()'s log4j-core references
  // merely to LOAD Utils.class, even though no Android code path calls configureLogger(). ART
  // verifies method-by-method instead and soft-fails an individual method when a class it alone
  // references is missing (see https://source.android.com/docs/core/runtime/verifier) - so :app
  // excludes log4j-core again here and relies on that difference. CONFIRMED on-device with
  // log4j-core absent from the APK (Phase B emulator run, Medium_Phone_API_36.1): log4j-api's
  // own "could not find a logging provider" fallback prints cleanly to logcat (no NoClassDefFoundError
  // loading Utils.class), and RipperRegistry.getRipper reflectively constructed ~119 ripper
  // classes end to end - including E621Ripper, whose AbstractRipper superclass field
  // initializer (`Utils.getURLHistoryFile()`) forces Utils.class to load+verify on essentially
  // every ripper instantiation attempt - with no VerifyError/NoClassDefFoundError, and that
  // ripper then ran a real rip against e621.net to RIP_COMPLETE. The matching `-dontwarn` lives
  // in proguard-rules.pro (dormant today - D8 alone, without minification, doesn't do the
  // "missing class" analysis that warning would silence; see that file's header comment).
  implementation(project(":core")) {
    exclude(group = "org.apache.logging.log4j", module = "log4j-core")
  }

  // AndroidX versions below are pinned exactly to the plan's verified-against-compileSdk-36
  // table - do not bump. Anything newer (core-ktx 1.19.0, compose-bom 2026.08.00, lifecycle
  // 2.11.0 at time of writing) demands compileSdk 37, which isn't published in any stable SDK
  // channel on this machine and fails AGP's AAR-metadata check.
  implementation("androidx.core:core-ktx:1.18.0")
  implementation("androidx.activity:activity-compose:1.13.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")

  implementation(platform("androidx.compose:compose-bom:2026.06.01"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.material3:material3")
  // material-icons-core (curated, BOM-pinned), not -extended: the extended set is a
  // multi-thousand-icon, multi-megabyte artifact that exists specifically so apps that don't
  // need it can skip it. Five NavigationBar glyphs + three action-button glyphs comfortably fit
  // in core - verified directly against the resolved AAR's classes.jar (not assumed: the first
  // pass here reached for Icons.Filled.History and it doesn't exist in this artifact - see
  // ui/nav/Destination.kt's DateRange substitution).
  implementation("androidx.compose.material:material-icons-core")
}
