import com.vanniktech.maven.publish.DeploymentValidation

plugins {
  alias(libs.plugins.vanniktech.maven.publish)
  `kotlin-convention`
  `spotless-convention`
  id("spinochart.publishing")
}

description = "Core engine for SpinoChart log parsing and report generation"

dependencies {
  implementation(libs.gatling.charts)
  implementation(libs.gatling.core)
}

mavenPublishing {
  publishToMavenCentral(
      automaticRelease = true,
      validateDeployment = DeploymentValidation.PUBLISHED,
  )
  signAllPublications()
  pom {
    name.set("SpinoChart Core")
    description.set("Core engine for SpinoChart log parsing and report generation")
  }
}
