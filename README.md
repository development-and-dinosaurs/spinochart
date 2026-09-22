# SpinoChart

Open-source, Highcharts-free performance reports for Gatling.

SpinoChart reads Gatling binary `simulation.log` files and generates standalone, interactive HTML dashboards with pure vector SVG charts — 100% MIT-licensed, air-gapped, and zero external JS/CSS dependencies.

---

## Contents

- [Quick start](#quick-start)
  - [Gradle plugin](#1-gradle-plugin)
  - [Command-line interface (CLI)](#2-command-line-interface-cli)
  - [Library](#3-library)
- [Themes](#themes)
  - [Spino (default)](#spino-flagship)
  - [Classic](#classic-gatling-palette)
- [Gradle plugin reference](#gradle-plugin-reference)
- [CLI reference](#cli-reference)
- [Library usage](#library-usage)
- [Building](#building)

---

## Quick start

### 1. Gradle plugin

Apply the plugin to your build:

```kotlin
plugins {
    id("uk.co.developmentanddinosaurs.spinochart") version "<version>"
}
```

When applied alongside Gatling's Gradle plugin (`io.gatling.gradle`), SpinoChart automatically configures `spinochartReport` to finalize all `gatlingRun*` tasks.

Run your simulation normally:

```bash
./gradlew gatlingRun
```

The report is generated beside `simulation.log` in `build/reports/gatling/<simulation-run>/index.html`.

### 2. Command-line interface (CLI)

Download the standalone binary from [GitHub Releases](https://github.com/development-and-dinosaurs/spinochart/releases):

```bash
chmod +x spinochart
./spinochart
```

By default, SpinoChart scans standard Gatling results folders (`results/`, `build/reports/gatling/`, `target/gatling/`) and generates an `index.html` report for each simulation run found.

You can also pass an explicit results folder or file:

```bash
# Scan a specific results directory
./spinochart -rf build/reports/gatling

# Process a single log file with custom output destination
./spinochart path/to/simulation.log custom-report.html
```

### 3. Library

Add the dependency to your project:

```kotlin
dependencies {
    implementation("uk.co.developmentanddinosaurs.spinochart:spinochart-core:<version>")
}
```

Parse logs and generate reports programmatically:

```kotlin
import uk.co.developmentanddinosaurs.spinochart.SpinoChart
import uk.co.developmentanddinosaurs.spinochart.theme.ReportThemes
import java.io.File

// Generate an HTML report
val logFile = File("build/reports/gatling/my-sim/simulation.log")
val reportFile = SpinoChart.generateReport(
    logFile = logFile,
    outputFile = File("build/reports/gatling/my-sim/index.html"),
    theme = ReportThemes.SPINO, // or ReportThemes.CLASSIC
)

// Or parse the log to inspect stats programmatically
val report = SpinoChart.parse(logFile)
println("Total requests: ${report.globalStats.totalCount}")
println("95th percentile: ${report.globalStats.p95ResponseTimeMs} ms")
```

---

## Themes

SpinoChart includes two built-in themes. Both produce self-contained single-file HTML reports with embedded vector SVGs, dark/light mode toggling, and interactive tooltip crosshairs without any external scripts or CDNs.

### Spino (Flagship)

The default theme. A high-density performance dashboard featuring:
- Summary KPI metric cards (Requests, Success Rate, Failure Rate, Latencies: Mean, P50, P95, P99, Max).
- Vector SVG charts for response time percentiles over time (OK), throughput (req/s OK vs KO), and active users.
- Comprehensive request statistics table.
- Error breakdown and classification.

Selected with `--theme spino` (or `theme.set("spino")`).

### Classic (Gatling Palette)

A faithful reproduction of the familiar Gatling Highcharts reporting layout using 100% open-source vector SVG charts:
- Exact Gatling Highcharts color palette: OK (`#68b65c`), KO (`#f15b4f`), Total (`#ffa900`), and Blue (`#5E7BE2`).
- 2-tier statistics tables (Executions vs Response Time percentiles).
- Response time distribution ranges chart (`< 800ms`, `800 - 1200ms`, `> 1200ms`, `failed`).
- OK / KO distribution donut chart.

Selected with `--theme classic` (or `theme.set("classic")`).

---

## Gradle plugin reference

### Configuration

```kotlin
spinochart {
    // Theme to use: "spino" (default) or "classic"
    theme.set("spino")

    // Results directory to scan. Defaults to build/reports/gatling
    reportsDir.set(layout.buildDirectory.dir("reports/gatling"))

    // Output HTML filename. Defaults to "index.html"
    outputFileName.set("index.html")
}
```

### Tasks

| Task | Description |
|---|---|
| `spinochartReport` | Scans `reportsDir` for all `simulation.log` files and generates reports. Automatically attached as `finalizedBy` on `gatlingRun*` tasks. |

The task checks timestamps and skips report generation if the existing report is newer than the `simulation.log`.

---

## CLI reference

```
Usage: spinochart [<options>] [<path-or-dir>] [<output-file>]

  Open-source, Highcharts-free performance reports for Gatling

Options:
  -rf, --results-folder=<path>  Gatling results folder to scan (matches Gatling CLI flag)
  -t, --theme=<text>            Report theme: 'spino' (default) or 'classic'
  -o, --output-name=<text>      Report file name (default: index.html)
  -f, --force                   Force regeneration even if existing report is up to date
  -h, --help                    Show this message and exit

Arguments:
  <path-or-dir>  Optional path to simulation.log or Gatling results directory
  <output-file>  Optional custom destination for report HTML (single log only)
```

---

## Building

```bash
# Build everything and run all verification checks
./gradlew check

# Run tests across all modules
./gradlew test

# Assemble standalone CLI binary in modules/spinochart-cli/build/bin/spinochart
./gradlew :modules:spinochart-cli:installCli

# Run the sample simulation end-to-end
./gradlew :samples:sample-simulation:runSimulation
```

### Project layout

| Module | Directory | Published Artifact | Description |
|---|---|---|---|
| `spinochart-core` | `modules/spinochart-core` | `uk.co.developmentanddinosaurs.spinochart:spinochart-core` | Log parser, SVG renderer, and report generation engine |
| `spinochart-cli` | `modules/spinochart-cli` | `uk.co.developmentanddinosaurs.spinochart:spinochart-cli` | Standalone Clikt CLI application and fat JAR |
| `spinochart-gradle-plugin` | `modules/spinochart-gradle-plugin` | `uk.co.developmentanddinosaurs.spinochart:spinochart-gradle-plugin` | Gradle plugin for Gatling builds |
| `sample-simulation` | `samples/sample-simulation` | — | End-to-end Gatling simulation test harness |
