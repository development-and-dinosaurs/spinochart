plugins {
  `kotlin-convention`
  `spotless-convention`
}

description = "Sample Gatling simulation to generate simulation.log test data"

dependencies {
  implementation(libs.gatling.charts.highcharts)
  implementation(projects.modules.spinochartCli)
}

val spinochartReport =
    tasks.register<JavaExec>("spinochartReport") {
      group = "reporting"
      description =
          "Generates a SpinoChart report in the Gatling results folder beside simulation.log"
      mainClass.set("uk.co.developmentanddinosaurs.spinochart.cli.SpinochartCliKt")
      classpath = sourceSets["main"].runtimeClasspath
      jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
      val resultsDir = layout.buildDirectory.dir("reports/gatling")
      argumentProviders.add(
          CommandLineArgumentProvider {
            val resultsFile = resultsDir.get().asFile
            if (resultsFile.exists()) {
              listOf(resultsFile.absolutePath)
            } else {
              emptyList()
            }
          }
      )
    }

val classicReport =
    tasks.register<JavaExec>("classicReport") {
      group = "reporting"
      description =
          "Generates a SpinoChart report with Classic Gatling theme in the Gatling results folder"
      mainClass.set("uk.co.developmentanddinosaurs.spinochart.cli.SpinochartCliKt")
      classpath = sourceSets["main"].runtimeClasspath
      jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
      val resultsDir = layout.buildDirectory.dir("reports/gatling")
      argumentProviders.add(
          CommandLineArgumentProvider {
            val resultsFile = resultsDir.get().asFile
            if (resultsFile.exists()) {
              listOf(
                  "-rf",
                  resultsFile.absolutePath,
                  "-t",
                  "classic",
                  "-o",
                  "classic.html",
                  "--force",
              )
            } else {
              emptyList()
            }
          }
      )
    }

tasks.register<JavaExec>("runSimulation") {
  group = "simulation"
  description = "Runs the Gatling sample simulation and generates SpinoChart reports"
  mainClass.set("io.gatling.app.Gatling")
  classpath = sourceSets["main"].runtimeClasspath
  jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
  args(
      "-s",
      "uk.co.developmentanddinosaurs.spinochart.sample.SampleSimulation",
      "-rf",
      layout.buildDirectory.dir("reports/gatling").get().asFile.absolutePath,
      "-nr", // --no-reports: Gatling only generates simulation.log
  )
  finalizedBy("spinochartReport")
}

tasks.register<JavaExec>("generateHighchartsReport") {
  group = "gatling"
  description = "Generates the standard Gatling Highcharts report for comparison"
  mainClass.set("io.gatling.app.Gatling")
  classpath = sourceSets["main"].runtimeClasspath
  jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
  val resultsDir = layout.buildDirectory.dir("reports/gatling").get().asFile
  val runFolder =
      resultsDir
          .listFiles()
          ?.firstOrNull { it.isDirectory && it.name.startsWith("samplesimulation") }
          ?.name ?: ""
  args("-ro", runFolder, "-rf", resultsDir.absolutePath)
}
