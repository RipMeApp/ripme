// Separate Gradle build for the Android app: the root build (../build.gradle.kts) never applies
// AGP and never sees this file. Built with the system `gradle` command, not a wrapper checked in
// here - see android/README.md for the exact toolchain (Gradle 9.7.0 + AGP 9.3.1 + JDK 26,
// probe-verified on this machine).
pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "ripme-android"

// Phase A built just the shared engine (:core). Phase B adds the Compose application module.
include(":core")
include(":app")
