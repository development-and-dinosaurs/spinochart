package uk.co.developmentanddinosaurs.spinochart

import java.io.File
import uk.co.developmentanddinosaurs.spinochart.generator.SpinoReportGenerator
import uk.co.developmentanddinosaurs.spinochart.model.SimulationReport
import uk.co.developmentanddinosaurs.spinochart.parser.SpinoLogReader
import uk.co.developmentanddinosaurs.spinochart.theme.ReportTheme
import uk.co.developmentanddinosaurs.spinochart.theme.ReportThemes

object SpinoChart {

  fun parse(logFile: File, reader: SpinoLogReader = SpinoLogReader()): SimulationReport =
      reader.read(logFile)

  fun generateReport(
      logFile: File,
      outputFile: File = File(logFile.parentFile, "index.html"),
      theme: ReportTheme = ReportThemes.SPINO,
      reader: SpinoLogReader = SpinoLogReader(),
      generator: SpinoReportGenerator = SpinoReportGenerator(),
  ): File {
    val report = parse(logFile, reader)
    return generator.generate(report, outputFile, theme)
  }
}
