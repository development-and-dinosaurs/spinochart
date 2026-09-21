package uk.co.developmentanddinosaurs.spinochart.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class SpinochartPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    val extension = project.extensions.create("spinochart", SpinochartExtension::class.java)

    extension.theme.convention("spino")
    extension.outputFileName.convention("index.html")
    extension.reportsDir.convention(project.layout.buildDirectory.dir("reports/gatling"))

    val reportTask =
        project.tasks.register("spinochartReport", GenerateSpinochartReportTask::class.java) { task
          ->
          task.group = "reporting"
          task.description = "Generates a SpinoChart report from the latest Gatling simulation.log"
          task.theme.convention(extension.theme)
          task.reportsDir.convention(extension.reportsDir)
          task.outputFileName.convention(extension.outputFileName)
        }

    // Auto-hook into Gatling tasks if present
    project.tasks
        .matching { it.name.startsWith("gatlingRun") }
        .configureEach { gatlingTask -> gatlingTask.finalizedBy(reportTask) }

    // Also support when gatling plugin is applied after spinochart
    project.plugins.withId("io.gatling.gradle") {
      project.tasks
          .matching { it.name.startsWith("gatlingRun") }
          .configureEach { gatlingTask -> gatlingTask.finalizedBy(reportTask) }
    }
  }
}
