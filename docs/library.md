# Library Usage

The `spinochart-core` library provides programmatic APIs for parsing Gatling `simulation.log` files, computing statistical summaries, and generating HTML reports.

---

## Dependency

Add the core library dependency:

=== "Gradle (Kotlin DSL)"

    ```kotlin
    dependencies {
        implementation("uk.co.developmentanddinosaurs.spinochart:spinochart-core:0.0.2")
    }
    ```

=== "Maven"

    ```xml
    <dependency>
        <groupId>uk.co.developmentanddinosaurs.spinochart</groupId>
        <artifactId>spinochart-core</artifactId>
        <version>0.0.2</version>
    </dependency>
    ```

---

## Generating HTML Reports

To generate a self-contained HTML report from a Gatling log:

```kotlin
import uk.co.developmentanddinosaurs.spinochart.SpinoChart
import uk.co.developmentanddinosaurs.spinochart.theme.ReportThemes
import java.io.File

val logFile = File("build/reports/gatling/my-simulation/simulation.log")
val outputFile = File("build/reports/gatling/my-simulation/index.html")

// Generate report with default Spino theme
val result = SpinoChart.generateReport(
    logFile = logFile,
    outputFile = outputFile,
    theme = ReportThemes.SPINO, // or ReportThemes.CLASSIC
)

println("Report written to: ${result.absolutePath}")
```

---

## Inspecting Parsed Metrics

If you want to validate assertions, push metrics to external databases, or build custom dashboards, parse the log directly into a `SimulationReport`:

```kotlin
import uk.co.developmentanddinosaurs.spinochart.SpinoChart
import java.io.File

val report = SpinoChart.parse(File("path/to/simulation.log"))

// Run metadata
println("Simulation: ${report.runInfo.simulationClassName}")
println("Duration: ${(report.runInfo.endTimeEpochMs - report.runInfo.startTimeEpochMs) / 1000}s")

// Global metrics
val global = report.globalStats
println("Total Requests: ${global.totalCount}")
println("Success Rate:   ${"%.2f".format((global.okCount.toDouble() / global.totalCount) * 100)}%")
println("P95 Latency:    ${global.p95ResponseTimeMs} ms")
println("P99 Latency:    ${global.p99ResponseTimeMs} ms")

// Per-request breakdown
for (req in report.requestStats) {
    println("Request: ${req.name}")
    println("  Count: ${req.totalCount} (OK: ${req.okCount}, KO: ${req.koCount})")
    println("  P95:   ${req.p95ResponseTimeMs} ms")
}

// Time-series data
report.requestsPerSecondOverTime.forEach { point ->
    println("Time: ${point.timestampEpochMs} -> Total: ${point.total}, OK: ${point.ok}, KO: ${point.ko}")
}
```

---

## Data Model Reference

### `SimulationReport`

Contains the complete parsed and aggregated representation of a Gatling simulation run:

* `runInfo: RunInfo` — Simulation class name, run ID, description, start/end timestamps, and duration.
* `globalStats: RequestStats` — Aggregated metrics across all requests in the simulation.
* `requestStats: List<RequestStats>` — Metrics partitioned by individual request name.
* `errors: List<ErrorInfo>` — Grouped error messages, counts, and percentages.
* `activeUsersOverTime: List<TimeSeriesPoint<Int>>` — Concurrently active user counts over time.
* `requestsPerSecondOverTime: List<RpsPoint>` — Requests dispatched per second (total, OK, KO) over time.
* `responsesPerSecondOverTime: List<RpsPoint>` — Responses received per second (total, OK, KO) over time.
* `percentilesOverTime: List<PercentilesPoint>` — Bucketized response time percentiles (min, P50, P75, P95, P99, max) over time.
* `ranges: ResponseTimeRanges?` — Response time distribution counts and percentages (low, middle, high, KO).

### `RequestStats`

Calculated metrics for global simulation or individual request endpoints:

| Property | Type | Description |
| :--- | :--- | :--- |
| `name` | `String` | Request name or `"Global"` |
| `totalCount` | `Long` | Total executed requests |
| `okCount` | `Long` | Number of successful (`OK`) requests |
| `koCount` | `Long` | Number of failed (`KO`) requests |
| `minResponseTimeMs` | `Double` | Minimum response time recorded (ms) |
| `maxResponseTimeMs` | `Double` | Maximum response time recorded (ms) |
| `meanResponseTimeMs` | `Double` | Arithmetic mean of response times (ms) |
| `stdDevResponseTimeMs` | `Double` | Standard deviation of response times |
| `p50ResponseTimeMs` | `Double` | 50th percentile (median) response time (ms) |
| `p75ResponseTimeMs` | `Double` | 75th percentile response time (ms) |
| `p95ResponseTimeMs` | `Double` | 95th percentile response time (ms) |
| `p99ResponseTimeMs` | `Double` | 99th percentile response time (ms) |
| `meanRequestsPerSec` | `Double` | Average requests per second across simulation duration |

---

## Custom Themes

You can implement custom rendering logic by extending the `ReportTheme` interface:

```kotlin
import uk.co.developmentanddinosaurs.spinochart.theme.ReportTheme
import uk.co.developmentanddinosaurs.spinochart.model.SimulationReport

class CustomJsonTheme : ReportTheme {
    override val id: String = "json"
    override val displayName: String = "JSON Summary"
    override val description: String = "Exports report as JSON"

    override fun render(report: SimulationReport): String {
        return """
        {
          "simulation": "${report.runInfo.simulationClassName}",
          "totalRequests": ${report.globalStats.totalCount},
          "p95": ${report.globalStats.p95ResponseTimeMs}
        }
        """.trimIndent()
    }
}
```
