//    the build derives a version with the jgitver plugin out of a tag in the git history. when there is no
// git repo, the jgitver default would be 0.0.0. one can override this version with a parameter. also, permit
// to start the build setting the javac release parameter, no parameter means build for java-17:
// gradle clean build -PjavacRelease=21
// gradle clean build -PcustomVersion=1.0.0-10-asdf
val customVersion = (project.findProperty("customVersion") ?: "") as String
val javacRelease = (project.findProperty("javacRelease") ?: "17") as String

plugins {
  id("fr.brouillard.oss.gradle.jgitver") version "0.9.1"
  id("jacoco")
  id("java")
  id("maven-publish")
}

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  implementation("com.lmax:disruptor:3.4.4")
  implementation("org.java-websocket:Java-WebSocket:1.5.3")
  implementation("org.jsoup:jsoup:1.16.1")
  implementation("org.json:json:20211205")
  implementation("com.j2html:j2html:1.6.0")
  implementation("commons-configuration:commons-configuration:1.10")
  implementation("commons-cli:commons-cli:1.5.0")
  implementation("commons-io:commons-io:2.13.0")
  implementation("org.apache.httpcomponents:httpclient:4.5.14")
  implementation("org.apache.httpcomponents:httpmime:4.5.14")
  implementation("org.apache.logging.log4j:log4j-api:2.20.0")
  implementation("org.apache.logging.log4j:log4j-core:2.20.0")
  implementation("org.graalvm.js:js:22.3.2")
  testImplementation(enforcedPlatform("org.junit:junit-bom:5.10.0"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

group = "com.rarchives.ripme"
version = "1.7.94"
description = "ripme"

jacoco {
  toolVersion = "0.8.10"
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
    includeEngines("junit-vintage")
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

