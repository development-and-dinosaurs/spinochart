# Quick Start

Get started with SpinoChart using your preferred workflow.

---

## 1. Gradle Plugin

The fastest way to use SpinoChart in Gradle projects running Gatling simulations.

Apply the plugin to your `build.gradle.kts`:

```kotlin
plugins {
    id("uk.co.developmentanddinosaurs.spinochart") version "0.0.2"
}
```

When applied alongside the official Gatling plugin (`io.gatling.gradle`), SpinoChart automatically attaches a `spinochartReport` task that finalizes all `gatlingRun*` tasks.

Run your simulation:

```bash
./gradlew gatlingRun
```

The report is generated directly beside the Gatling `simulation.log` at:
```text
build/reports/gatling/<simulation-run>/index.html
```

---

## 2. Command-Line Interface (CLI)

The CLI is a single self-executable binary that runs on any machine with Java 17+ installed.

Download the latest binary from [GitHub Releases](https://github.com/development-and-dinosaurs/spinochart/releases):

```bash
curl -LO https://github.com/development-and-dinosaurs/spinochart/releases/latest/download/spinochart
chmod +x spinochart
```

Run SpinoChart in your project directory:

```bash
./spinochart
```

By default, SpinoChart recursively scans standard Gatling output directories (`results/`, `build/reports/gatling/`, `target/gatling/`) and creates an `index.html` report for each simulation run found.

You can also specify a specific directory or `simulation.log` file:

```bash
./spinochart path/to/simulation.log custom-report.html
```

---

## 3. Core Library

To embed SpinoChart log parsing and report generation directly in your JVM application, add the core dependency:

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

Generate an HTML report programmatically:

```kotlin
import uk.co.developmentanddinosaurs.spinochart.SpinoChart
import uk.co.developmentanddinosaurs.spinochart.theme.ReportThemes
import java.io.File

val logFile = File("path/to/simulation.log")
val outputFile = File("reports/performance.html")

SpinoChart.generateReport(
    logFile = logFile,
    outputFile = outputFile,
    theme = ReportThemes.SPINO, // or ReportThemes.CLASSIC
)
```
