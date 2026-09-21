package uk.co.developmentanddinosaurs.spinochart.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java.io.File
import uk.co.developmentanddinosaurs.spinochart.SpinoChart
import uk.co.developmentanddinosaurs.spinochart.theme.ReportThemes

class SpinochartCommand : CliktCommand("spinochart") {

  override fun help(context: com.github.ajalt.clikt.core.Context): String =
      "Open-source, Highcharts-free performance reports for Gatling"

  private val path by
      argument(
              "path-or-dir",
              help = "Optional path to simulation.log or Gatling results directory",
          )
          .file(mustExist = false)
          .optional()

  private val output by
      argument(
              "output-file",
              help =
                  "Optional custom destination for report HTML (when processing a single log file)",
          )
          .file()
          .optional()

  private val resultsFolder by
      option(
              "-rf",
              "--results-folder",
              help = "Gatling results folder to scan (matches Gatling CLI flag)",
          )
          .file(mustExist = false)

  private val theme by
      option(
              "-t",
              "--theme",
              help = "Report theme: 'spino' (modern dark, default) or 'classic' (Gatling palette)",
          )
          .default("spino")

  private val outputName by
      option(
              "-o",
              "--output-name",
              help = "Report file name (default: index.html)",
          )
          .default("index.html")

  private val force by
      option(
              "-f",
              "--force",
              help = "Force regeneration even if existing report is up to date",
          )
          .flag(default = false)

  private val defaultSearchPaths =
      listOf(
          File("results"),
          File("build/reports/gatling"),
          File("target/gatling"),
      )

  override fun run() {
    val themeObj = ReportThemes.fromIdOrNull(theme)
    if (themeObj == null) {
      echo(
          "🦖 SpinoChart: Unknown theme '$theme'. Available themes: ${ReportThemes.all.joinToString { it.id }}",
          err = true,
      )
      throw ProgramResult(1)
    }

    val targetPath = path ?: resultsFolder
    val logsToProcess = resolveLogFiles(targetPath)

    if (logsToProcess.isEmpty()) {
      val searched = targetPath?.path ?: defaultSearchPaths.joinToString(", ") { it.path }
      echo("🦖 SpinoChart: No simulation.log files found in: $searched", err = true)
      echo("Usage: spinochart [options] [path-or-dir] [output-file]", err = true)
      echo("Run 'spinochart --help' for details.", err = true)
      throw ProgramResult(1)
    }

    var generatedCount = 0

    for (logFile in logsToProcess) {
      val targetFile =
          when {
            output != null && logsToProcess.size == 1 -> output!!
            else -> File(logFile.parentFile, outputName)
          }

      if (!force && targetFile.exists() && targetFile.lastModified() >= logFile.lastModified()) {
        echo(
            "🦖 SpinoChart: Report in ${logFile.parentFile.name} is up to date (${targetFile.name}). Skipping."
        )
        continue
      }

      echo(
          "🦖 SpinoChart: Generating ${themeObj.displayName} report for ${logFile.parentFile.name}/${logFile.name}..."
      )
      val generated = SpinoChart.generateReport(logFile, targetFile, themeObj)
      echo("✅ SpinoChart: Report successfully generated at ${generated.absolutePath}")
      generatedCount++
    }

    if (generatedCount == 0 && logsToProcess.isNotEmpty()) {
      echo("🦖 SpinoChart: All reports are up to date. Use --force to regenerate.")
    }
  }

  private fun resolveLogFiles(explicitPath: File?): List<File> {
    if (explicitPath != null) {
      if (!explicitPath.exists()) {
        echo(
            "🦖 SpinoChart: Specified path does not exist: ${explicitPath.absolutePath}",
            err = true,
        )
        throw ProgramResult(1)
      }
      if (explicitPath.isFile) {
        return listOf(explicitPath)
      }
      return explicitPath
          .walkTopDown()
          .filter { it.name == "simulation.log" }
          .sortedBy { it.parentFile?.name ?: "" }
          .toList()
    }

    for (dir in defaultSearchPaths) {
      if (dir.exists() && dir.isDirectory) {
        val found =
            dir.walkTopDown()
                .filter { it.name == "simulation.log" }
                .sortedBy { it.parentFile?.name ?: "" }
                .toList()
        if (found.isNotEmpty()) {
          return found
        }
      }
    }

    val cwd = File(".")
    return cwd.walkTopDown()
        .maxDepth(3)
        .filter { it.name == "simulation.log" }
        .sortedBy { it.parentFile?.name ?: "" }
        .toList()
  }
}
