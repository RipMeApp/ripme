// :core packages the shared ripper engine (~114 rippers minus InstagramRipper) as a plain
// java-library, compiled from the *same* sources as the desktop build (../../src/main/java) via
// a filtered copy - see syncSharedJava below. Nothing under ../../ is touched by this build; the
// root build.gradle.kts never applies AGP and never includes this project.
plugins {
  id("java-library")
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

// Compile at release 17 (the desktop build defaults to 25 - see javacRelease in
// ../../build.gradle.kts) so the class files are at a bytecode level D8 accepts for the Android
// :app module. Verified on this tree via `gradle compileJava -PjavacRelease=17` at the repo root.
tasks.withType<JavaCompile> {
  options.release.set(17)
  options.encoding = "UTF-8"
}

// --- Source sharing --------------------------------------------------------------------------
//
// Must go through a filtered *copy*, not a bare srcDir: Gradle excludes on a SourceDirectorySet
// apply to every directory added to it, so excluding com/rarchives/ripme/ui/MainWindow.java here
// would also delete this module's own shim of the same name (src/main/java/.../MainWindow.java,
// below) once both were merged into one logical source set. Sync-ing the filtered desktop tree
// into build/shared-java first, and adding *that* copy as the srcDir, keeps the exclude list
// scoped to the copy step so the shim survives.
val syncSharedJava = tasks.register<Sync>("syncSharedJava") {
  from(file("../../src/main/java")) {
    exclude("com/rarchives/ripme/App.java")                              // Swing + CLI entry point
    exclude("com/rarchives/ripme/ui/MainWindow.java")                    // Swing
    exclude("com/rarchives/ripme/ui/UpdateUtils.java")                   // Swing dialogs, jar self-update
    exclude("com/rarchives/ripme/ui/ClipboardUtils.java")
    exclude("com/rarchives/ripme/ui/ContextMenuMouseListener.java")
    exclude("com/rarchives/ripme/ui/CookieSettingsDialog.java")
    exclude("com/rarchives/ripme/ui/HistoryMenuMouseListener.java")
    exclude("com/rarchives/ripme/ui/QueueMenuMouseListener.java")
    exclude("com/rarchives/ripme/uiUtils/**")                            // Swing
    exclude("com/rarchives/ripme/ripper/rippers/InstagramRipper.java")   // GraalVM JS, won't dex
  }
  into(layout.buildDirectory.dir("shared-java"))
}

// --- Generated ripper registry ----------------------------------------------------------------
//
// Ripper discovery on the desktop scans the runtime classpath/jar for concrete AbstractRipper
// subclasses (Utils.getClassesForPackage, called from AbstractRipper.getRipperConstructors) -
// that finds nothing inside a DEX, which has no classpath to enumerate. This task regenerates
// the same class list at *build* time instead, by listing the same two source directories
// AbstractRipper.getRipper would have scanned, and writes a plain Java source file whose
// getRipper(URL) mirrors AbstractRipper.getRipper's algorithm (see AbstractRipper.java:794-830)
// against that fixed list: try each album ripper's URL constructor in turn, swallow any failure
// (canRip() rejecting the URL throws from inside the constructor - that's how a ripper says "not
// mine"), then try the video rippers, else throw. Generating from source rather than hand-listing
// keeps the registry in sync as upstream adds rippers.
val ripperRegistryOutputDir = layout.buildDirectory.dir("generated/ripperRegistry/java")

val generateRipperRegistry = tasks.register("generateRipperRegistry") {
  val albumRippersDir = file("../../src/main/java/com/rarchives/ripme/ripper/rippers")
  val videoRippersDir = file("../../src/main/java/com/rarchives/ripme/ripper/rippers/video")
  val outputDir = ripperRegistryOutputDir

  // albumRippersDir already contains videoRippersDir, so this one inputs.dir covers both -
  // slightly conservative (also invalidates on ripperhelpers/ changes, which the task ignores)
  // but never wrong.
  inputs.dir(albumRippersDir)
  outputs.dir(outputDir)

  doLast {
    // A file counts as a concrete ripper when it declares "public class <SameName>". This
    // excludes abstract base classes - there happen to be none at the top level of rippers/
    // today, but new ones could land there, and the whole point of generating this list is to
    // stay correct as upstream adds rippers - and any stray non-public/helper file.
    fun concreteRipperNames(dir: File): List<String> {
      val files = dir.listFiles { f -> f.isFile && f.name.endsWith(".java") } ?: emptyArray()
      return files
        .filter { it.name != "InstagramRipper.java" }   // GraalVM JS; excluded from the sync too
        .filter { f ->
          val baseName = f.name.removeSuffix(".java")
          val text = f.readText()
          Regex("public\\s+class\\s+$baseName\\b").containsMatchIn(text) &&
            !Regex("public\\s+abstract\\s+class\\s+$baseName\\b").containsMatchIn(text)
        }
        .map { it.name.removeSuffix(".java") }
        .sorted()
    }

    val albumRippers = concreteRipperNames(albumRippersDir)
      .map { "com.rarchives.ripme.ripper.rippers.$it" }
    val videoRippers = concreteRipperNames(videoRippersDir)
      .map { "com.rarchives.ripme.ripper.rippers.video.$it" }

    val packageDir = File(outputDir.get().asFile, "com/rarchives/ripme/android")
    packageDir.mkdirs()

    // The 2-space lead-in (rather than 8, matching the separator below) compensates for the 6
    // spaces the surrounding """ template already contributes on the "${quotedList(...)}" line
    // itself - without it, only the *first* generated entry would end up over-indented relative
    // to the rest once trimIndent() strips the template's common 6-space margin.
    fun quotedList(fqcns: List<String>): String =
      "  " + fqcns.joinToString(",\n        ") { "\"$it\"" }

    File(packageDir, "RipperRegistry.java").writeText(
      """
      package com.rarchives.ripme.android;

      import com.rarchives.ripme.ripper.AbstractRipper;
      import com.rarchives.ripme.ripper.VideoRipper;

      import java.lang.reflect.Constructor;
      import java.net.URL;
      import java.util.Arrays;
      import java.util.Collections;
      import java.util.List;

      /**
       * GENERATED by the :core "generateRipperRegistry" Gradle task - do not edit by hand, and
       * do not commit (it lives under build/). Regenerated on every build from the .java files in
       * ../../src/main/java/com/rarchives/ripme/ripper/rippers{,/video}; see
       * android/core/build.gradle.kts for the generator.
       *
       * Stands in for AbstractRipper.getRipper's classpath scan (AbstractRipper.java:794-830),
       * which finds nothing inside a DEX. ALBUM_RIPPERS/VIDEO_RIPPERS are the FQCNs of every
       * concrete (public, non-abstract) class under ripper.rippers / ripper.rippers.video at
       * generation time, alphabetised, with InstagramRipper dropped (GraalVM JS, won't dex).
       * getRipper(URL) mirrors AbstractRipper.getRipper exactly: try each album ripper's URL
       * constructor in turn, swallow any failure, then try the video rippers, else throw.
       */
      public final class RipperRegistry {

          public static final List<String> ALBUM_RIPPERS = Collections.unmodifiableList(Arrays.asList(
      ${quotedList(albumRippers)}
          ));

          public static final List<String> VIDEO_RIPPERS = Collections.unmodifiableList(Arrays.asList(
      ${quotedList(videoRippers)}
          ));

          private RipperRegistry() { }

          public static AbstractRipper getRipper(URL url) throws Exception {
              for (String fqcn : ALBUM_RIPPERS) {
                  try {
                      Constructor<?> constructor = Class.forName(fqcn).getConstructor(URL.class);
                      // by design: can throw ClassCastException, same as AbstractRipper.getRipper
                      AbstractRipper ripper = (AbstractRipper) constructor.newInstance(url);
                      return ripper;
                  } catch (Exception e) {
                      // Incompatible rippers *will* throw exceptions during instantiation.
                  }
              }
              for (String fqcn : VIDEO_RIPPERS) {
                  try {
                      Constructor<?> constructor = Class.forName(fqcn).getConstructor(URL.class);
                      // by design: can throw ClassCastException, same as AbstractRipper.getRipper
                      VideoRipper ripper = (VideoRipper) constructor.newInstance(url);
                      return ripper;
                  } catch (Exception e) {
                      // Incompatible rippers *will* throw exceptions during instantiation.
                  }
              }
              throw new Exception("No compatible ripper found");
          }
      }
      """.trimIndent()
    )
  }
}

// generateRipperRegistry's output is a plain build/ directory Provider, not a Copy/Sync task, so
// (unlike syncSharedJava above) Gradle can't infer the task dependency from srcDir() alone - wire
// it explicitly so compileJava never reads a stale or empty generated/ directory.
tasks.named("compileJava") {
  dependsOn(generateRipperRegistry)
}

sourceSets {
  main {
    java.srcDir(syncSharedJava)
    java.srcDir(ripperRegistryOutputDir)
    resources.srcDir("../../src/main/resources")   // rip.properties, LabelsBundle*
  }
}

// --- Dependencies ------------------------------------------------------------------------------
//
// Starting point is the root build.gradle.kts dependency list, minus what the excluded files
// above were the only users of: Compose (desktop GUI only), GraalVM js/js-language
// (InstagramRipper only), com.lmax:disruptor (log4j async logging only), and commons-cli
// (App.java's CLI parsing only).
//
// One correction versus that plan: org.apache.httpcomponents.client5:httpclient5 is *not*
// dependency-free here as first assumed - ScrolllerRipper and RedgifsRipper both import
// org.apache.hc.core5.{http,net} utility classes (NameValuePair, WWWFormCodec, URIBuilder).
// Neither file touches the actual client5 HTTP client, only httpcore5's value types, so this
// depends on the lighter httpcore5 artifact directly rather than pulling all of httpclient5 in
// just to get it transitively. Version 5.4.3 matches what httpclient5:5.6.3 resolves to in the
// root build (checked via `gradle dependencies --configuration compileClasspath` at the repo
// root), so both builds exercise the same httpcore5 version.
//
// A second addition the plan's dependency list missed: org.jetbrains:annotations, for
// DanbooruRipper's @Nullable (org.jetbrains.annotations.Nullable). The root build gets this
// transitively (via the Kotlin/Compose toolchain, which :core deliberately doesn't apply), so
// it has to be declared directly here. Version 23.0.0 matches what the root build resolves it to.
dependencies {
  implementation("org.jsoup:jsoup:1.23.1")
  implementation("org.json:json:20260719")
  implementation("com.j2html:j2html:1.6.0")
  implementation("org.apache.commons:commons-configuration2:2.15.1")
  implementation("org.apache.commons:commons-lang3:3.20.0")
  implementation("org.apache.commons:commons-text:1.15.0")
  implementation("commons-io:commons-io:2.22.0")
  implementation("org.apache.httpcomponents.core5:httpcore5:5.4.3")
  implementation("org.jetbrains:annotations:23.0.0")
  implementation("com.squareup.okhttp3:okhttp:5.4.0")
  // Muxes Reddit's separate video-only and audio-only DASH tracks into a single playable file.
  implementation("org.mp4parser:isoparser:1.9.56")
  implementation("org.mp4parser:muxer:1.9.56")
  implementation("org.java-websocket:Java-WebSocket:1.6.0")

  // log4j-api is exposed to consumers (:app) as `api` so Android code can log through the same
  // Logger/LogManager surface the ripper engine already uses.
  api("org.apache.logging.log4j:log4j-api:2.26.1")
  // The plan wanted log4j-core compileOnly: Utils imports org.apache.logging.log4j.core.* only
  // inside configureLogger(), which no Android code path calls, so the reasoning was "don't ship
  // a dependency for a method nobody invokes". That does NOT work in practice - proven by
  // RipperRegistryTest, not assumed. compileOnly made :core:jar succeed (compilation doesn't
  // care) but :core:test fails: JVM class verification is a whole-class, link-time step, not a
  // per-method, call-time one, and HotSpot's verifier resolves configureLogger()'s
  // ConsoleAppender$Target reference while verifying Utils.class as a whole - before, and
  // regardless of whether, that method ever runs. So the *first* reflective
  // Class.forName(fqcn) on any ripper that touches Utils (nearly all of them) throws
  // NoClassDefFoundError, even though nothing calls configureLogger(). Confirmed three ways:
  // the real test failure, a plain `java -cp` run outside Gradle's test worker, and forcing
  // Class.forName("com.rarchives.ripme.utils.Utils") alone with zero other classes involved.
  // Since AbstractRipper.getRipper's *desktop* classpath scan reflectively loads ripper classes
  // the exact same way, this bites the real desktop build too, in principle - it just never
  // surfaces there because log4j-core is always on the runtime classpath (implementation, not
  // compileOnly, in ../../build.gradle.kts).
  //
  // Given src/ can't be touched beyond the one sanctioned Utils.getConfigDir() patch, the only
  // lever available here is dependency scope, so this ships log4j-core for real. That does cost
  // real weight (and D8/R8 attention) on the Android side - Phase B must check whether ART's
  // verifier is more lenient about this than HotSpot's before deciding whether :app can drop it
  // again; if ART turns out to need it too, this is load-bearing there, not just on the JVM.
  implementation("org.apache.logging.log4j:log4j-core:2.26.1")

  testImplementation(enforcedPlatform("org.junit:junit-bom:6.1.3"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
  useJUnitPlatform()
  testLogging {
    showStackTraces = true
  }
}
