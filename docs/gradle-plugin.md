# Gradle Plugin

The SpinoChart Gradle plugin integrates directly into Gradle Gatling builds to produce clean, Highcharts-free HTML reports automatically.

---

## Installation

Add the plugin to your `build.gradle.kts` file:

```kotlin
plugins {
    id("uk.co.developmentanddinosaurs.spinochart") version "0.0.2"
}
```

The plugin is published to both the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/uk.co.developmentanddinosaurs.spinochart) and [Maven Central](https://repo1.maven.org/maven2/uk/co/developmentanddinosaurs/spinochart/spinochart-gradle-plugin/).

---

## Configuration

Configure report generation using the `spinochart` extension block:

```kotlin
spinochart {
    // Report theme: "spino" (default) or "classic"
    theme.set("spino")

    // Base directory scanned for Gatling simulation.log files
    // Default: build/reports/gatling
    reportsDir.set(layout.buildDirectory.dir("reports/gatling"))

    // Name of the generated report file inside each run directory
    // Default: index.html
    outputFileName.set("index.html")
}
```

### Configuration Options

| Property | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `theme` | `Property<String>` | `"spino"` | The dashboard visual theme (`"spino"` or `"classic"`). |
| `reportsDir` | `DirectoryProperty` | `build/reports/gatling` | The directory tree to scan for `simulation.log` files. |
| `outputFileName` | `Property<String>` | `"index.html"` | Name given to the output HTML file in each simulation directory. |

---

## How It Works

### Automatic Gatling Lifecycle Hooking
When applied to a project containing the official Gatling plugin (`io.gatling.gradle`), SpinoChart automatically attaches the `spinochartReport` task to finalize every task matching `gatlingRun*`. 

```bash
./gradlew gatlingRun
```

After Gatling finishes executing and writes `simulation.log`, `spinochartReport` runs and generates `index.html` directly in the simulation run folder.

### Manual Task Execution
You can also run the report generation task on demand:

```bash
./gradlew spinochartReport
```

### Explicit Task Configuration
If you have a custom simulation folder or want to generate a report for a specific `simulation.log`, you can register or configure the task directly:

```kotlin
tasks.register<uk.co.developmentanddinosaurs.spinochart.plugin.GenerateSpinochartReportTask>("generateCustomReport") {
    simulationLog.set(file("custom/path/simulation.log"))
    outputFileName.set("performance-summary.html")
    theme.set("spino")
}
```

---

## Up-to-Date Freshness Checks

The report task checks file timestamps before generating each report:
* If `index.html` already exists and is newer than `simulation.log`, generation is skipped.
* If a new simulation run produces a newer `simulation.log`, the report is updated immediately.

---

## Configuration Cache Support

The plugin is fully compatible with Gradle's Configuration Cache (`--configuration-cache`). It relies on standard Gradle lazy properties and inputs without accessing the `Project` model during execution.
