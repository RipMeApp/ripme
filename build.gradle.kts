//    the build derives a version with the jgitver plugin out of a tag in the git history. when there is no
// git repo, the jgitver default would be 0.0.0. one can override this version with a parameter. also, permit
// to start the build setting the javac release parameter, no parameter means build for java-25:
// gradle clean build -PjavacRelease=17
// gradle clean build -PjavacRelease=26
// gradle clean build -PcustomVersion=1.0.0-10-asdf
val customVersion = (project.findProperty("customVersion") ?: "") as String
val javacRelease = (project.findProperty("javacRelease") ?: "25") as String

plugins {
  id("fr.brouillard.oss.gradle.jgitver") version "0.9.1"
  id("jacoco")
  id("java")
  id("maven-publish")
  id("com.gradleup.shadow") version "9.0.0-rc1"
}

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  implementation("com.lmax:disruptor:4.0.0")
  implementation("org.java-websocket:Java-WebSocket:1.6.0")
  implementation("org.jsoup:jsoup:1.23.1")
  implementation("org.json:json:20260719")
  implementation("com.j2html:j2html:1.6.0")
  implementation("org.apache.commons:commons-configuration2:2.15.1")
  implementation("org.apache.commons:commons-lang3:3.20.0")
  implementation("org.apache.commons:commons-text:1.15.0")
  implementation("commons-cli:commons-cli:1.11.0")
  implementation("commons-io:commons-io:2.22.0")
  implementation("org.apache.httpcomponents.client5:httpclient5:5.6.3")
  implementation("org.apache.logging.log4j:log4j-api:2.26.1")
  implementation("org.apache.logging.log4j:log4j-core:2.26.1")
  implementation("com.squareup.okhttp3:okhttp:5.4.0")
  // Muxes Reddit's separate video-only and audio-only DASH tracks into a single playable file.
  implementation("org.mp4parser:isoparser:1.9.56")
  implementation("org.mp4parser:muxer:1.9.56")
  // org.graalvm.js:js is now a thin POM whose js-language implementation (used for
  // InstagramRipper's direct com.oracle.js.parser AST access) is runtime-scope only;
  // depend on js-language directly so the parser classes are visible at compile time too.
  implementation("org.graalvm.js:js:25.2.4")
  implementation("org.graalvm.js:js-language:25.2.4")
  testImplementation(enforcedPlatform("org.junit:junit-bom:6.1.3"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

group = "com.rarchives.ripme"
version = "1.7.94"
description = "ripme"

jacoco {
  toolVersion = "0.8.15"
}

jgitver {
  gitCommitIDLength = 8
  nonQualifierBranches = "main,master"
  useGitCommitID = true
}

afterEvaluate {
  if (customVersion != "") {
    project.version = customVersion
  }
}

tasks.compileJava {
  options.release.set(Integer.parseInt(javacRelease))
}

tasks.withType<Jar> {
  duplicatesStrategy = DuplicatesStrategy.INCLUDE
  manifest {
    attributes["Main-Class"] = "com.rarchives.ripme.App"
    attributes["Implementation-Version"] =  archiveVersion
    attributes["Multi-Release"] = "true"
  }

  // To add all of the dependencies otherwise a "NoClassDefFoundError" error
  from(sourceSets.main.get().output)

  dependsOn(configurations.runtimeClasspath)
  from({
    configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
  })
}

tasks.shadowJar {
  transform<com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer>()
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])
    }
  }
}

tasks.withType<JavaCompile> {
  options.encoding = "UTF-8"
  val compilerArgs = options.compilerArgs
  compilerArgs.addAll(listOf("-Xlint:deprecation"))
}

tasks.test {
  testLogging {
    showStackTraces = true
  }
  useJUnitPlatform {
    // gradle-6.5.1 not yet allows passing this as parameter, so exclude it
    excludeTags("flaky","slow")
    includeEngines("junit-jupiter")
  }
  finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.register<Test>("testAll") {
  useJUnitPlatform {
    includeTags("any()", "none()")
  }
}

tasks.register<Test>("testFlaky") {
  useJUnitPlatform {
    includeTags("flaky")
  }
}

tasks.register<Test>("testSlow") {
  useJUnitPlatform {
    includeTags("slow")
  }
}

tasks.register<Test>("testTagged") {
  useJUnitPlatform {
    includeTags("any()")
  }
}

// make all archive tasks in the build reproducible
tasks.withType<AbstractArchiveTask>().configureEach {
  isPreserveFileTimestamps = false
  isReproducibleFileOrder = true
}

println("Build directory: ${file(layout.buildDirectory)}")

tasks.jacocoTestReport {
  dependsOn(tasks.test) // tests are required to run before generating the report
  reports {
    xml.required.set(false)
    csv.required.set(false)
    html.outputLocation.set(file("${file(layout.buildDirectory)}/jacocoHtml"))
  }
}

