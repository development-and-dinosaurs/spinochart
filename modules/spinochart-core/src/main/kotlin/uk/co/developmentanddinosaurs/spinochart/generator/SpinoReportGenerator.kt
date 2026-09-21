package uk.co.developmentanddinosaurs.spinochart.generator

import java.io.File
import uk.co.developmentanddinosaurs.spinochart.model.SimulationReport
import uk.co.developmentanddinosaurs.spinochart.theme.ReportTheme
import uk.co.developmentanddinosaurs.spinochart.theme.ReportThemes

class SpinoReportGenerator(val defaultTheme: ReportTheme = ReportThemes.SPINO) {

  fun generate(
      report: SimulationReport,
      destinationFile: File,
      theme: ReportTheme = defaultTheme,
  ): File {
    destinationFile.parentFile?.mkdirs()
    val html = theme.render(report)
    destinationFile.writeText(html)
    return destinationFile
  }
}
