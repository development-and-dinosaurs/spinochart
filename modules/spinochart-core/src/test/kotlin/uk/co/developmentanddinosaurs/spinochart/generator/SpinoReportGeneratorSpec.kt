package uk.co.developmentanddinosaurs.spinochart.generator

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File
import uk.co.developmentanddinosaurs.spinochart.SpinoChart
import uk.co.developmentanddinosaurs.spinochart.theme.ReportThemes

class SpinoReportGeneratorSpec :
    BehaviorSpec({
      Given("a Gatling simulation.log file") {
        val logFile =
            File(
                SpinoReportGeneratorSpec::class.java.getResource("/sample-simulation.log")!!.toURI()
            )
        val outputDir = File("build/test-output")
        outputDir.mkdirs()

        When("generating the report with the Spino (flagship) theme") {
          val outputFile = File(outputDir, "spino-report.html")
          val generated = SpinoChart.generateReport(logFile, outputFile, theme = ReportThemes.SPINO)

          Then("the HTML file should be created and lightweight (< 100 KB)") {
            generated.shouldExist()
            (generated.length() < 100_000) shouldBe true
          }

          Then("it should contain pure vector SVG charts") {
            val content = generated.readText()
            content shouldContain "spino-svg-chart"
            content shouldContain "<svg id=\"percChart\""
            content shouldContain "<svg id=\"rpsChart\""
            content shouldContain "<svg id=\"usersChart\""
            content shouldContain "SpinoChart"
          }
        }

        When("generating the report with the Classic (Gatling palette) theme") {
          val outputFile = File(outputDir, "classic-report.html")
          val generated =
              SpinoChart.generateReport(logFile, outputFile, theme = ReportThemes.CLASSIC)

          Then("the HTML file should be created and lightweight (< 100 KB)") {
            generated.shouldExist()
            (generated.length() < 100_000) shouldBe true
          }

          Then("it should contain Gatling classic layout and SVG charts") {
            val content = generated.readText()
            content shouldContain "Gatling Stats"
            content shouldContain "Response Time Ranges"
            content shouldContain "Executions"
            content shouldContain "<svg id=\"percChart\""
            content shouldContain "<svg id=\"rpsChart\""
            content shouldContain "<svg id=\"usersChart\""
          }
        }
      }
    })
