package uk.co.developmentanddinosaurs.spinochart.model

data class SimulationReport(
    val runInfo: RunInfo,
    val globalStats: RequestStats,
    val requestStats: List<RequestStats>,
    val errors: List<ErrorInfo>,
    val activeUsersOverTime: List<TimeSeriesPoint<Int>>,
    val requestsPerSecondOverTime: List<RpsPoint>,
    val responsesPerSecondOverTime: List<RpsPoint>,
    val percentilesOverTime: List<PercentilesPoint>,
    val ranges: ResponseTimeRanges? = null,
)

data class ResponseTimeRanges(
    val lowerBound: Int,
    val higherBound: Int,
    val lowCount: Int,
    val middleCount: Int,
    val highCount: Int,
    val koCount: Int,
    val lowPercentage: Double,
    val middlePercentage: Double,
    val highPercentage: Double,
    val koPercentage: Double,
)

data class RunInfo(
    val simulationClassName: String,
    val runId: String,
    val description: String,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long,
    val durationSeconds: Long,
)

data class RequestStats(
    val name: String,
    val totalCount: Long,
    val okCount: Long,
    val koCount: Long,
    val minResponseTimeMs: Double,
    val maxResponseTimeMs: Double,
    val meanResponseTimeMs: Double,
    val stdDevResponseTimeMs: Double,
    val p50ResponseTimeMs: Double,
    val p75ResponseTimeMs: Double,
    val p95ResponseTimeMs: Double,
    val p99ResponseTimeMs: Double,
    val meanRequestsPerSec: Double = 0.0,
)

data class ErrorInfo(
    val message: String,
    val count: Long,
    val percentage: Double,
)

data class TimeSeriesPoint<T>(
    val timestampEpochMs: Long,
    val value: T,
)

data class RpsPoint(
    val timestampEpochMs: Long,
    val total: Int,
    val ok: Int,
    val ko: Int,
)

data class PercentilesPoint(
    val timestampEpochMs: Long,
    val min: Double,
    val p50: Double,
    val p75: Double,
    val p95: Double,
    val p99: Double,
    val max: Double,
)
