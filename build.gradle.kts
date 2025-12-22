import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  kotlin("multiplatform") version "2.2.20"
  kotlin("plugin.serialization") version "2.2.20"
  id("com.android.library") version "8.13.2"
  id("maven-publish")
}

group = "io.github.broot5"

version = "1.0-SNAPSHOT"

android {
  namespace = "io.github.broot5.komicinfo"
  compileSdk = 36

  defaultConfig { minSdk = 21 }

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
        api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")
        api("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
        api("io.github.pdvrieze.xmlutil:core:0.91.3")
        api("io.github.pdvrieze.xmlutil:serialization:0.91.3")
      }
    }

    val jvmTest by getting { dependencies { implementation(kotlin("test")) } }
  }
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }

publishing {
  publications {
    withType<MavenPublication>().configureEach {
      groupId = "io.github.broot5"
      version = project.version.toString()
    }
  }
}
