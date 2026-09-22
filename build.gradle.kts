plugins {
  alias(libs.plugins.gradle.publish) apply false
  alias(libs.plugins.vanniktech.maven.publish) apply false
  `spotless-convention`
}

allprojects {
  group = "uk.co.developmentanddinosaurs.spinochart"
  version = "0.0.2"
}
