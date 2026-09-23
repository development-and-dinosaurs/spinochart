import com.vanniktech.maven.publish.DeploymentValidation

plugins {
  `java-gradle-plugin`
  alias(libs.plugins.gradle.publish)
  alias(libs.plugins.vanniktech.maven.publish)
  `kotlin-convention`
  `spotless-convention`
  id("spinochart.publishing")
}

description = "Gradle plugin for generating SpinoChart reports from Gatling simulation logs"

dependencies {
  implementation(projects.modules.spinochartCore)
  testImplementation(gradleTestKit())
}

gradlePlugin {
  plugins {
    create("spinochart") {
      id = "uk.co.developmentanddinosaurs.spinochart"
      displayName = "SpinoChart"
      description = "Open-source, Highcharts-free performance reports for Gatling"
      implementationClass = "uk.co.developmentanddinosaurs.spinochart.plugin.SpinochartPlugin"
      tags.set(listOf("gatling", "reporting", "performance", "charts", "svg"))
      vcsUrl = "https://github.com/development-and-dinosaurs/spinochart"
      website = "https://github.com/development-and-dinosaurs/spinochart"
    }
  }
}

mavenPublishing {
  publishToMavenCentral(
      automaticRelease = true,
      validateDeployment = DeploymentValidation.VALIDATED,
  )
  signAllPublications()
  pom {
    name.set("SpinoChart Gradle Plugin")
    description.set("Gradle plugin for generating SpinoChart reports from Gatling simulation logs")
  }
}
