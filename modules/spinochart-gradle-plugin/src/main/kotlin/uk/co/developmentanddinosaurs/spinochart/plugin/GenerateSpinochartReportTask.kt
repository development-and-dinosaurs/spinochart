package uk.co.developmentanddinosaurs.spinochart.plugin

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import uk.co.developmentanddinosaurs.spinochart.SpinoChart
import uk.co.developmentanddinosaurs.spinochart.theme.ReportThemes

@DisableCachingByDefault(
    because = "Generates HTML reports directly beside simulation logs with freshness checks"
)
abstract class GenerateSpinochartReportTask : DefaultTask() {

  @get:InputFile
  @get:PathSensitive(PathSensitivity.RELATIVE)
  @get:Optional
  abstract val simulationLog: RegularFileProperty

  @get:InputDirectory
  @get:PathSensitive(PathSensitivity.RELATIVE)
  @get:Optional
  abstract val reportsDir: DirectoryProperty

  @get:Input abstract val theme: Property<String>

  @get:Input abstract val outputFileName: Property<String>

  @TaskAction
  fun generate() {
    val logFiles = resolveLogFiles()
    if (logFiles.isEmpty()) {
      logger.warn(
          "🦖 SpinoChart: No simulation.log found in ${reportsDir.orNull?.asFile?.absolutePath ?: "configured path"}. Skipping report generation."
      )
      return
    }

    val themeName = theme.getOrElse("spino")
    val themeObj =
        ReportThemes.fromIdOrNull(themeName)
            ?: run {
              logger.error(
                  "🦖 SpinoChart: Unknown theme '$themeName'. Available themes: ${ReportThemes.all.joinToString { it.id }}"
              )
              return
            }
    val fileName = outputFileName.getOrElse("index.html")

    var generatedCount = 0
    for (logFile in logFiles) {
      val targetFile = File(logFile.parentFile, fileName)

      if (targetFile.exists() && targetFile.lastModified() >= logFile.lastModified()) {
        logger.info(
            "🦖 SpinoChart: Report in ${logFile.parentFile.name} is up to date (${targetFile.name}). Skipping."
        )
        continue
      }

      logger.lifecycle(
          "🦖 SpinoChart: Generating ${themeObj.displayName} report for ${logFile.parentFile.name}/${logFile.name}..."
      )
      val result = SpinoChart.generateReport(logFile, targetFile, themeObj)
      logger.lifecycle("✅ SpinoChart: Report successfully generated at ${result.absolutePath}")
      generatedCount++
    }

    if (generatedCount == 0 && logFiles.isNotEmpty()) {
      logger.lifecycle("🦖 SpinoChart: All reports are up to date.")
    }
  }

  private fun resolveLogFiles(): List<File> {
    if (simulationLog.isPresent) {
      val singleLog = simulationLog.get().asFile
      return if (singleLog.exists()) listOf(singleLog) else emptyList()
    }

    val dir = reportsDir.orNull?.asFile ?: return emptyList()
    if (!dir.exists()) return emptyList()

    return dir.walkTopDown()
        .filter { it.name == "simulation.log" }
        .sortedBy { it.parentFile?.name ?: "" }
        .toList()
  }
}
