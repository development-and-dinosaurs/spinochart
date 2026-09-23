# Themes

SpinoChart includes two built-in visual themes. Both generate self-contained, single-file HTML reports with pure inline vector SVG charts and zero external JavaScript or CSS dependencies.

---

## Spino (Flagship)

The default theme for SpinoChart. A high-density, modern performance engineering dashboard.

To use:
* **Gradle Plugin**: `theme.set("spino")` (default)
* **CLI**: `--theme spino` (default)
* **Library**: `ReportThemes.SPINO`

### Highlights

* **KPI Summary Cards** — Key metrics prominently displayed: Total Requests, Success Rate, Failure Rate, Mean Latency, Median (P50), P95, P99, and Max Latency.
* **Vector SVG Charts**:
    * **Response Time Percentiles over Time** — P50, P75, P95, and P99 latency progression.
    * **Throughput over Time** — Requests per second (OK vs KO) as a dual-series area chart.
    * **Active Users over Time** — Concurrently active user ramp and load profile per scenario.
* **Interactive Tooltip Crosshair** — Hovering over any chart draws a synchronized vertical crosshair and displays precise timestamps and values in a floating tooltip. Implemented using lightweight vanilla JavaScript with zero external libraries.
* **Dark / Light Mode Toggle** — Defaults to an elegant dark palette with an instant toggle for light backgrounds.
* **Request Statistics Table** — Tabular overview of every transaction with count, error rates, and percentiles.

---

## Classic (Gatling-Inspired)

A drop-in visual alternative for teams and stakeholders accustomed to traditional Gatling reports.

To use:
* **Gradle Plugin**: `theme.set("classic")`
* **CLI**: `--theme classic`
* **Library**: `ReportThemes.CLASSIC`

### Highlights

* **Familiar Aesthetics** — Recreates the classic Gatling Highcharts layout, color palette: OK (`#68b65c`), KO (`#f15b4f`), Total (`#ffa900`), and Blue (`#5E7BE2`).
* **100% MIT Licensed** — Delivers the familiar look and feel of Gatling's traditional dashboard while eliminating proprietary Highcharts runtime and license restrictions.
* **Air-Gapped Clean** — Uses pure inline SVG elements with no external scripts, fonts, or stylesheets.

---

## Design Principles

### Pure Vector SVG
All charts are computed on the server/JVM and rendered into inline SVG elements. They scale sharply to high-DPI retina displays and 4K monitors without blurring or pixelation.

### Zero External Dependencies
Reports can be emailed, archived in zip artifacts, or opened in strictly air-gapped CI/CD networks. There are:
* No external CSS links
* No external font downloads
* No CDN script tags (`<script src="...">`)
* No tracking or analytics telemetry
