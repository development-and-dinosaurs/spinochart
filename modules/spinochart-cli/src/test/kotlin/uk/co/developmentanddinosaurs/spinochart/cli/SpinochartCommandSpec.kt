package uk.co.developmentanddinosaurs.spinochart.cli

import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File
import kotlin.io.path.createTempDirectory

class SpinochartCommandSpec :
    BehaviorSpec({
      val sampleLogBytes =
          SpinochartCommandSpec::class
              .java
              .getResourceAsStream("/sample-simulation.log")!!
              .readBytes()

      Given("the Spinochart Clikt command") {
        When("running with --help") {
          Then("it should request help message display") {
            shouldThrow<PrintHelpMessage> { SpinochartCommand().parse(listOf("--help")) }
          }
        }

        When("running with an unknown theme") {
          Then("it should fail with exit code 1") {
            val error =
                shouldThrow<ProgramResult> {
                  SpinochartCommand().parse(listOf("-t", "non-existent-theme"))
                }
            error.statusCode shouldBe 1
          }
        }

        When("running with a path that does not exist") {
          Then("it should fail with exit code 1") {
            val error =
                shouldThrow<ProgramResult> {
                  SpinochartCommand().parse(listOf("non-existent-dir-12345"))
                }
            error.statusCode shouldBe 1
          }
        }

        When("running with a single simulation.log file") {
          val tempDir = createTempDirectory("spinochart-cli-single").toFile()
          tempDir.deleteOnExit()
          val logFile = File(tempDir, "simulation.log")
          logFile.writeBytes(sampleLogBytes)

          SpinochartCommand().parse(listOf(logFile.absolutePath))

          Then("it should generate index.html in the log directory") {
            val report = File(tempDir, "index.html")
            report.shouldExist()
            (report.length() > 0) shouldBe true
          }
        }

        When("running with a results directory containing multiple simulation runs") {
          val resultsDir = createTempDirectory("spinochart-cli-results").toFile()
          resultsDir.deleteOnExit()
          val run1 = File(resultsDir, "run-1").apply { mkdirs() }
          val run2 = File(resultsDir, "run-2").apply { mkdirs() }
          File(run1, "simulation.log").writeBytes(sampleLogBytes)
          File(run2, "simulation.log").writeBytes(sampleLogBytes)

          SpinochartCommand().parse(listOf(resultsDir.absolutePath))

          Then("it should generate reports in both run folders") {
            val report1 = File(run1, "index.html")
            val report2 = File(run2, "index.html")
            report1.shouldExist()
            report2.shouldExist()
          }

          Then("subsequent invocation should skip up-to-date reports") {
            val report1Time = File(run1, "index.html").lastModified()
            SpinochartCommand().parse(listOf(resultsDir.absolutePath))
            File(run1, "index.html").lastModified() shouldBe report1Time
          }

          Then("invocation with --force should regenerate reports") {
            Thread.sleep(50)
            SpinochartCommand().parse(listOf(resultsDir.absolutePath, "--force"))
            File(run1, "index.html").shouldExist()
          }
        }

        When("running with Gatling's -rf results-folder flag and --theme classic") {
          val resultsDir = createTempDirectory("spinochart-cli-rf").toFile()
          resultsDir.deleteOnExit()
          val runDir = File(resultsDir, "my-sim-run").apply { mkdirs() }
          File(runDir, "simulation.log").writeBytes(sampleLogBytes)

          SpinochartCommand()
              .parse(
                  listOf(
                      "-rf",
                      resultsDir.absolutePath,
                      "-t",
                      "classic",
                      "-o",
                      "custom-report.html",
                  )
              )

          Then("it should generate custom-report.html with classic Gatling theme") {
            val report = File(runDir, "custom-report.html")
            report.shouldExist()
            val html = report.readText()
            html shouldContain "Gatling Stats"
          }
        }
      }
    })
