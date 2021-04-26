plugins {
  id("jacoco")
  id("java")
  id("maven-publish")
}

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  implementation("org.java-websocket:Java-WebSocket:1.5.1")
  implementation("org.jsoup:jsoup:1.8.1")
  implementation("org.json:json:20190722")
  implementation("commons-configuration:commons-configuration:1.7")
  implementation("log4j:log4j:1.2.17")
  implementation("commons-cli:commons-cli:1.2")
  implementation("commons-io:commons-io:1.3.2")
  implementation("org.apache.httpcomponents:httpclient:4.3.6")
  implementation("org.apache.httpcomponents:httpmime:4.3.3")
  implementation("org.graalvm.js:js:20.1.0")
  testImplementation(enforcedPlatform("org.junit:junit-bom:5.6.2"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("junit:junit:4.13")
}

group = "com.rarchives.ripme"
version = "1.7.94"
description = "ripme"

java {                                      
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<Jar> {
  manifest {
    attributes["Main-Class"] = "com.rarchives.ripme.App"
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
}

tasks.test {
  useJUnitPlatform {
    // gradle-6.5.1 not yet allows passing this as parameter, so exclude it
    excludeTags("flaky","slow")
    includeEngines("junit-jupiter")
    includeEngines("junit-vintage")
  }
  finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.register<Test>("slowTests") {
  useJUnitPlatform {
    includeTags("slow")
  }
}

// make all archive tasks in the build reproducible
tasks.withType<AbstractArchiveTask>().configureEach {
  isPreserveFileTimestamps = false
  isReproducibleFileOrder = true
}

tasks.jacocoTestReport {
  dependsOn(tasks.test) // tests are required to run before generating the report
  reports {
    xml.isEnabled = false
    csv.isEnabled = false
    html.destination = file("${buildDir}/jacocoHtml")
  }
}

