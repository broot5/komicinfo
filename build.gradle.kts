plugins {
  kotlin("jvm") version "2.2.20"

  id("org.unbroken-dome.xjc") version "2.0.0"
}

group = "io.github.broot5"

version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
  testImplementation(kotlin("test"))

  implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.4")
  implementation("org.apache.commons:commons-compress:1.28.0")

  runtimeOnly("org.glassfish.jaxb:jaxb-runtime:4.0.6")
}

tasks.test { useJUnitPlatform() }

kotlin { jvmToolchain(21) }

xjc {
  xjcVersion.set("3.0")
  srcDirName.set("resources/schema")
}
