package uk.co.developmentanddinosaurs.spinochart.parser

import io.gatling.charts.stats.GeneralStats
import io.gatling.charts.stats.Group
import io.gatling.charts.stats.LogFileData
import io.gatling.charts.stats.LogFileReader
import io.gatling.charts.stats.PercentilesVsTimePlot
import io.gatling.charts.stats.RequestStatsPath
import io.gatling.commons.stats.Status
import io.gatling.core.config.GatlingConfiguration
import java.io.File
import scala.Option
import scala.jdk.javaapi.CollectionConverters
import uk.co.developmentanddinosaurs.spinochart.model.ErrorInfo
import uk.co.developmentanddinosaurs.spinochart.model.PercentilesPoint
import uk.co.developmentanddinosaurs.spinochart.model.RequestStats
import uk.co.developmentanddinosaurs.spinochart.model.ResponseTimeRanges
import uk.co.developmentanddinosaurs.spinochart.model.RpsPoint
import uk.co.developmentanddinosaurs.spinochart.model.RunInfo
import uk.co.developmentanddinosaurs.spinochart.model.SimulationReport
import uk.co.developmentanddinosaurs.spinochart.model.TimeSeriesPoint

class SpinoLogReader(
    private val configuration: GatlingConfiguration = GatlingConfiguration.load()
) {

  fun read(logFile: File): SimulationReport {
    val reader = LogFileReader(logFile, configuration)
    val data = reader.read()
    return mapToReport(data, logFile)
  }

  private fun mapToReport(data: LogFileData, logFile: File): SimulationReport {
    val rawRunInfo = data.runInfo()
    val runInfo =
        RunInfo(
            simulationClassName = rawRunInfo.simulationClassName(),
            runId = logFile.parentFile?.name ?: "unknown",
            description = rawRunInfo.runDescription(),
            startTimeEpochMs = rawRunInfo.injectStart(),
            endTimeEpochMs = rawRunInfo.injectEnd(),
            durationSeconds = (rawRunInfo.injectEnd() - rawRunInfo.injectStart()) / 1000,
        )

    val globalStats = toRequestStats("Global", data, Option.empty(), Option.empty())

    val requestStats =
        CollectionConverters.asJava(data.statsPaths()).filterIsInstance<RequestStatsPath>().map {
          toRequestStats(it.request(), data, Option.apply(it.request()), it.group())
        }

    val errors =
        CollectionConverters.asJava(data.errors(Option.empty(), Option.empty())).map {
          ErrorInfo(
              message = it.message(),
              count = it.count().toLong(),
              percentage = it.percentage(),
          )
        }

    val activeUsers =
        CollectionConverters.asJava(data.numberOfActiveSessionsPerSecond(Option.empty())).map {
          TimeSeriesPoint(
              timestampEpochMs = it.time().toLong() * 1000,
              value = it.value(),
          )
        }

    val rps =
        CollectionConverters.asJava(data.numberOfRequestsPerSecond(Option.empty(), Option.empty()))
            .map {
              RpsPoint(
                  timestampEpochMs = it.time().toLong() * 1000,
                  total = it.total(),
                  ok = it.oks(),
                  ko = it.kos(),
              )
            }

    val responses =
        CollectionConverters.asJava(data.numberOfResponsesPerSecond(Option.empty(), Option.empty()))
            .map {
              RpsPoint(
                  timestampEpochMs = it.time().toLong() * 1000,
                  total = it.total(),
                  ok = it.oks(),
                  ko = it.kos(),
              )
            }

    val percentiles =
        CollectionConverters.asJava(
                data.responseTimePercentilesOverTime(
                    Status.apply("OK"),
                    Option.empty(),
                    Option.empty(),
                )
            )
            .filterIsInstance<PercentilesVsTimePlot>()
            .mapNotNull { plot ->
              val p = plot.percentiles()
              if (p.isDefined) {
                val perc = p.get()
                PercentilesPoint(
                    timestampEpochMs = plot.time().toLong() * 1000,
                    min = perc.percentile0().toDouble(),
                    p50 = perc.percentile50().toDouble(),
                    p75 = perc.percentile75().toDouble(),
                    p95 = perc.percentile95().toDouble(),
                    p99 = perc.percentile99().toDouble(),
                    max = perc.percentile100().toDouble(),
                )
              } else null
            }

    val rawRanges = data.numberOfRequestInResponseTimeRanges(Option.empty(), Option.empty())
    val ranges =
        ResponseTimeRanges(
            lowerBound = rawRanges.lowerBound(),
            higherBound = rawRanges.higherBound(),
            lowCount = rawRanges.lowCount(),
            middleCount = rawRanges.middleCount(),
            highCount = rawRanges.highCount(),
            koCount = rawRanges.koCount(),
            lowPercentage = rawRanges.lowPercentage(),
            middlePercentage = rawRanges.middlePercentage(),
            highPercentage = rawRanges.highPercentage(),
            koPercentage = rawRanges.koPercentage(),
        )

    return SimulationReport(
        runInfo = runInfo,
        globalStats = globalStats,
        requestStats = requestStats,
        errors = errors,
        activeUsersOverTime = activeUsers,
        requestsPerSecondOverTime = rps,
        responsesPerSecondOverTime = responses,
        percentilesOverTime = percentiles,
        ranges = ranges,
    )
  }

  private fun toRequestStats(
      name: String,
      data: LogFileData,
      requestName: Option<String>,
      group: Option<Group>,
  ): RequestStats {
    val allStats = data.requestGeneralStats(requestName, group, Option.empty())
    val okStats = data.requestGeneralStats(requestName, group, Option.apply(Status.apply("OK")))
    val koStats = data.requestGeneralStats(requestName, group, Option.apply(Status.apply("KO")))

    fun Double.percentileOf(stats: GeneralStats): Double =
        (stats.percentile().apply(this) as Number).toDouble()

    return RequestStats(
        name = name,
        totalCount = allStats.count(),
        okCount = okStats.count(),
        koCount = koStats.count(),
        minResponseTimeMs = allStats.min().toDouble(),
        maxResponseTimeMs = allStats.max().toDouble(),
        meanResponseTimeMs = allStats.mean().toDouble(),
        stdDevResponseTimeMs = allStats.stdDev().toDouble(),
        p50ResponseTimeMs = 50.0.percentileOf(allStats),
        p75ResponseTimeMs = 75.0.percentileOf(allStats),
        p95ResponseTimeMs = 95.0.percentileOf(allStats),
        p99ResponseTimeMs = 99.0.percentileOf(allStats),
        meanRequestsPerSec = allStats.meanRequestsPerSec(),
    )
  }
}
