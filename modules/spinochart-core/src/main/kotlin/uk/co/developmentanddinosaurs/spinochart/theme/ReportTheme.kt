package uk.co.developmentanddinosaurs.spinochart.theme

import uk.co.developmentanddinosaurs.spinochart.model.SimulationReport

interface ReportTheme {
  val id: String
  val displayName: String
  val description: String

  fun render(report: SimulationReport): String
}
