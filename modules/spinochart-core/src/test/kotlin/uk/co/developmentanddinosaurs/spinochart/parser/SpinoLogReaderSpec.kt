package uk.co.developmentanddinosaurs.spinochart.parser

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import java.io.File

class SpinoLogReaderSpec :
    BehaviorSpec({
      Given("a Gatling simulation.log file") {
        val logFile =
            File(SpinoLogReaderSpec::class.java.getResource("/sample-simulation.log")!!.toURI())
        val reader = SpinoLogReader()

        When("reading the log file with SpinoLogReader") {
          val report = reader.read(logFile)

          Then("it should parse run info correctly") {
            report.runInfo.simulationClassName shouldBe
                "uk.co.developmentanddinosaurs.spinochart.sample.SampleSimulation"
            report.runInfo.runId shouldNotBe ""
            report.runInfo.startTimeEpochMs shouldBeGreaterThan 0L
            report.runInfo.endTimeEpochMs shouldBeGreaterThan report.runInfo.startTimeEpochMs
          }

          Then("it should calculate accurate global stats") {
            report.globalStats.name shouldBe "Global"
            report.globalStats.totalCount shouldBe 45
            report.globalStats.okCount shouldBe 39
            report.globalStats.koCount shouldBe 6
            report.globalStats.minResponseTimeMs shouldBeGreaterThanOrEqual 0.0
            report.globalStats.maxResponseTimeMs shouldBeGreaterThan 0.0
            report.globalStats.p95ResponseTimeMs shouldBeGreaterThan 0.0
          }

          Then("it should parse per-request stats") {
            report.requestStats shouldHaveSize 3

            val dinosaursReq = report.requestStats.first { it.name == "Get Dinosaurs" }
            dinosaursReq.totalCount shouldBe 15
            dinosaursReq.okCount shouldBe 15
            dinosaursReq.koCount shouldBe 0

            val fossilReq = report.requestStats.first { it.name == "Get Fossil Data" }
            fossilReq.totalCount shouldBe 15
            fossilReq.okCount shouldBe 15
            fossilReq.koCount shouldBe 0

            val perimeterReq = report.requestStats.first { it.name == "Check Perimeter Stability" }
            perimeterReq.totalCount shouldBe 15
            perimeterReq.okCount shouldBe 9
            perimeterReq.koCount shouldBe 6
          }

          Then("it should parse error details") {
            report.errors shouldHaveSize 1
            val error = report.errors.first()
            error.message shouldContain "status.find.is(200), but actually found 500"
            error.count shouldBe 6
            error.percentage shouldBe 100.0
          }

          Then("it should extract time-series data") {
            report.activeUsersOverTime.shouldNotBeEmpty()
            report.requestsPerSecondOverTime.shouldNotBeEmpty()
            report.responsesPerSecondOverTime.shouldNotBeEmpty()
            report.percentilesOverTime.shouldNotBeEmpty()
          }
        }
      }
    })
