import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  kotlin("multiplatform") version "2.2.20"
  kotlin("plugin.serialization") version "2.2.20"
  id("com.android.library") version "8.13.2"
  id("com.vanniktech.maven.publish") version "0.35.0"
}

group = "io.github.broot5"

version = "1.0.1"

android {
  namespace = "io.github.broot5.komicinfo"
  compileSdk = 36

  defaultConfig { minSdk = 26 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
  jvmToolchain(17)

  applyDefaultHierarchyTemplate {
    common {
      group("jvmAndAndroid") {
        withAndroidTarget()
        withJvm()
      }
    }
  }

  androidTarget {
    publishLibraryVariants("release")
    compilations.all {
      compileTaskProvider.configure {
        compilerOptions {
          jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
          freeCompilerArgs.add("-Xexpect-actual-classes")
        }
      }
    }
  }

  jvm("jvm") {
    compilations.all {
      compileTaskProvider.configure {
        compilerOptions {
          jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
          freeCompilerArgs.add("-Xexpect-actual-classes")
        }
      }
    }
  }

  sourceSets {
    val commonMain by getting

    val jvmAndAndroidMain by getting {
      dependencies {
        api("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")
        implementation("io.github.pdvrieze.xmlutil:core:0.91.3")
        implementation("io.github.pdvrieze.xmlutil:serialization:0.91.3")
      }
    }

    val jvmTest by getting { dependencies { implementation(kotlin("test")) } }
  }
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
  systemProperty("java.awt.headless", "true")
}

mavenPublishing {
  publishToMavenCentral()
  signAllPublications()

  coordinates(group.toString(), "komicinfo", version.toString())

  pom {
    name = "komicinfo"
    description =
        "A Kotlin library for reading and writing ComicBook archives (CBZ) with ComicInfo.xml metadata."
    inceptionYear = "2025"
    url = "https://github.com/broot5/komicinfo"

    licenses {
      license {
        name = "MIT License"
        url = "https://opensource.org/licenses/MIT"
        distribution = "https://opensource.org/licenses/MIT"
      }
    }

    developers {
      developer {
        id = "broot5"
        name = "broot5"
        url = "https://github.com/broot5"
      }
    }

    scm {
      url = "https://github.com/broot5/komicinfo"
      connection = "scm:git:git://github.com/broot5/komicinfo.git"
      developerConnection = "scm:git:ssh://git@github.com:broot5/komicinfo.git"
    }
  }
}
