import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.android.library)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.detekt)
  alias(libs.plugins.kover)
}

group = "io.github.broot5"

version = "1.0.2"

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
  explicitApi = ExplicitApiMode.Strict

  compilerOptions {
    freeCompilerArgs.add("-Xexpect-actual-classes")
    allWarningsAsErrors.set(true)
  }

  applyDefaultHierarchyTemplate {
    common {
      group("jvmAndAndroid") {
        withAndroidTarget()
        withJvm()
      }
    }
  }

  androidTarget { publishLibraryVariants("release") }

  jvm()

  sourceSets {
    val jvmAndAndroidMain by getting {
      dependencies {
        api(libs.kotlinx.datetime)
        implementation(libs.kotlinx.serialization.core)
        implementation(libs.xmlutil.core)
        implementation(libs.xmlutil.serialization)
      }
    }

    val jvmTest by getting { dependencies { implementation(kotlin("test")) } }
  }
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
  systemProperty("java.awt.headless", "true")
}

detekt {
  source.setFrom(
      "src/commonMain/kotlin",
      "src/jvmAndAndroidMain/kotlin",
      "src/jvmMain/kotlin",
      "src/androidMain/kotlin",
  )
  buildUponDefaultConfig = true
  config.setFrom("detekt.yml")
  parallel = true
}

kover {
  currentProject { sources { excludedSourceSets.add("debug") } }

  reports {
    filters {
      excludes {
        classes("*Test", "*Test$*", "*.TestHelper*")
        packages("*.test", "*.tests")
      }
    }
    verify { rule { minBound(70) } }
  }
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
