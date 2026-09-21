rootProject.name = "spinochart"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement { repositories { mavenCentral() } }

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

include("modules:spinochart-core")

include("modules:spinochart-cli")

include("modules:spinochart-gradle-plugin")

include("samples:sample-simulation")
