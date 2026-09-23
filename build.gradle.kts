plugins {
  alias(libs.plugins.axion.release)
  alias(libs.plugins.gradle.publish) apply false
  alias(libs.plugins.vanniktech.maven.publish) apply false
  `spotless-convention`
}

group = "uk.co.developmentanddinosaurs"

version = scmVersion.version

subprojects {
  group = rootProject.group
  version = rootProject.version
}
