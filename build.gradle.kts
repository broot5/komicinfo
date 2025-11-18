plugins {
  kotlin("jvm") version "2.2.20"

  kotlin("plugin.serialization") version "2.2.20"
}

group = "io.github.broot5"

version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
  testImplementation(kotlin("test"))

  implementation("org.apache.commons:commons-compress:1.28.0")

  implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")
  implementation("io.github.pdvrieze.xmlutil:core:0.91.3")
  implementation("io.github.pdvrieze.xmlutil:serialization:0.91.3")
}

tasks.test { useJUnitPlatform() }

kotlin { jvmToolchain(21) }
