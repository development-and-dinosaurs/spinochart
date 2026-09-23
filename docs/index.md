![SpinoChart](assets/banner.png){ .spino-banner }

# SpinoChart

**Open-source, Highcharts-free performance reports for Gatling.**

SpinoChart reads Gatling binary `simulation.log` files and generates standalone, interactive HTML dashboards with pure vector SVG charts — 100% MIT-licensed, air-gapped safe, and zero external JS/CSS dependencies.

---

## Why SpinoChart?

While Gatling's core load-testing engine is open-source (Apache 2.0), the standard HTML report generator (`gatling-charts-highcharts`) is **not open source**—it is distributed under the proprietary "Gatling Highcharts License," which strictly prohibits modifying the reporting code or extracting the bundled charting library.

SpinoChart provides a **100% open-source, MIT-licensed reporting engine**:

* **True Open Source (MIT)** — Completely free and open source. Easily passes corporate open-source review boards and legal compliance scanners without non-OSI proprietary licensing exceptions.
* **Zero External Dependencies** — Single-file HTML output with embedded CSS and inline vector SVGs. No CDN calls, no tracking, and fully air-gapped compliant.
* **Interactive Visualizations** — Clean SVG graphs with custom tooltip crosshairs for response time percentiles, throughput (RPS OK vs KO), and active users over time.
* **Dark & Light Mode** — Built-in theme toggling with high-density layouts optimized for performance engineering.
* **Flexible Consumption** — Use as a [Gradle plugin](gradle-plugin.md), a [standalone CLI binary](cli.md), or embed the [core library](library.md) in your own tooling.

---

## Artifacts

SpinoChart is distributed across three modules:

| Artifact | Coordinates / Asset | Purpose |
| :--- | :--- | :--- |
| **Gradle Plugin** | `uk.co.developmentanddinosaurs.spinochart` | Automatically generates reports during Gradle Gatling builds |
| **Standalone CLI** | `spinochart` (executable binary) | Zero-dependency command-line binary for CI/CD and terminal workflows |
| **Core Library** | `uk.co.developmentanddinosaurs.spinochart:spinochart-core` | Direct Kotlin/Java API for parsing logs and generating HTML |

---

## Next Steps

* [Quick Start](quick-start.md) — Get up and running in under two minutes
* [Gradle Plugin](gradle-plugin.md) — Automate report generation in Gradle projects
* [Command-line Interface](cli.md) — Run the standalone CLI on any machine
* [Library Usage](library.md) — Parse logs and render reports programmatically
* [Themes](themes.md) — Explore the Spino and Classic themes
