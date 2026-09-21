package uk.co.developmentanddinosaurs.spinochart.plugin

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

interface SpinochartExtension {

  /** Theme to use for report generation: "spino" or "classic". Defaults to "spino". */
  val theme: Property<String>

  /**
   * Directory containing Gatling simulation results. Defaults to
   * `${layout.buildDirectory}/reports/gatling`.
   */
  val reportsDir: DirectoryProperty

  /** Name of the generated report file. Defaults to "index.html". */
  val outputFileName: Property<String>
}
