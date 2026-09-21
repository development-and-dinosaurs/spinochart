package uk.co.developmentanddinosaurs.spinochart.theme

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import uk.co.developmentanddinosaurs.spinochart.chart.SvgChartRenderer
import uk.co.developmentanddinosaurs.spinochart.model.ResponseTimeRanges
import uk.co.developmentanddinosaurs.spinochart.model.SimulationReport
import uk.co.developmentanddinosaurs.spinochart.util.HtmlUtils.escapeHtml

class ClassicTheme : ReportTheme {

  override val id: String = "classic"
  override val displayName: String = "Classic (Gatling Palette)"
  override val description: String =
      "Faithful Gatling Highcharts reporting experience with exact colors, 2-tier stats tables, and vector SVG charts."

  private val dateFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())

  override fun render(report: SimulationReport): String {
    val run = report.runInfo
    val global = report.globalStats
    val startTime = dateFormatter.format(Instant.ofEpochMilli(run.startTimeEpochMs))
    val endTime = dateFormatter.format(Instant.ofEpochMilli(run.endTimeEpochMs))

    // 1. Response Time Ranges Chart
    val ranges =
        report.ranges
            ?: ResponseTimeRanges(
                lowerBound = 800,
                higherBound = 1200,
                lowCount = global.okCount.toInt(),
                middleCount = 0,
                highCount = 0,
                koCount = global.koCount.toInt(),
                lowPercentage =
                    if (global.totalCount > 0) (global.okCount.toDouble() / global.totalCount) * 100
                    else 0.0,
                middlePercentage = 0.0,
                highPercentage = 0.0,
                koPercentage =
                    if (global.totalCount > 0) (global.koCount.toDouble() / global.totalCount) * 100
                    else 0.0,
            )
    val rangesSvg = SvgChartRenderer.renderRangesChart(ranges, width = 380, height = 180)

    // 2. OK / KO Distribution Donut Chart
    val donutSvg =
        SvgChartRenderer.renderDonutChart(
            chartId = "distDonut",
            slices =
                listOf(
                    SvgChartRenderer.DonutSlice("OK", global.okCount.toDouble(), "#68b65c"),
                    SvgChartRenderer.DonutSlice("KO", global.koCount.toDouble(), "#f15b4f"),
                ),
            centerLabel = "Requests",
            centerValue = "${global.totalCount}",
            width = 250,
            height = 180,
        )

    // 3. Render Percentiles SVG with Gatling Highcharts colors
    val percLabels =
        report.percentilesOverTime.map {
          dateFormatter.format(Instant.ofEpochMilli(it.timestampEpochMs))
        }
    val percChartSvg =
        SvgChartRenderer.renderLineChart(
            chartId = "percChart",
            labels = percLabels,
            seriesList =
                listOf(
                    SvgChartRenderer.Series(
                        "50%",
                        "#4ea1d4",
                        report.percentilesOverTime.map { it.p50 },
                    ),
                    SvgChartRenderer.Series(
                        "75%",
                        "#487ad9",
                        report.percentilesOverTime.map { it.p75 },
                    ),
                    SvgChartRenderer.Series(
                        "95%",
                        "#7335dc",
                        report.percentilesOverTime.map { it.p95 },
                    ),
                    SvgChartRenderer.Series(
                        "99%",
                        "#c73905",
                        report.percentilesOverTime.map { it.p99 },
                    ),
                ),
            yUnit = "ms",
        )

    // 4. Render RPS Stacked Bar SVG with Gatling Green (#68b65c) and Red (#f15b4f)
    val rpsLabels =
        report.requestsPerSecondOverTime.map {
          dateFormatter.format(Instant.ofEpochMilli(it.timestampEpochMs))
        }
    val rpsChartSvg =
        SvgChartRenderer.renderStackedBarChart(
            chartId = "rpsChart",
            labels = rpsLabels,
            seriesOk = report.requestsPerSecondOverTime.map { it.ok },
            seriesKo = report.requestsPerSecondOverTime.map { it.ko },
            okColor = "#68b65c",
            koColor = "#f15b4f",
        )

    // 5. Render Active Users SVG with Gatling Amber (#FFA900)
    val userLabels =
        report.activeUsersOverTime.map {
          dateFormatter.format(Instant.ofEpochMilli(it.timestampEpochMs))
        }
    val usersChartSvg =
        SvgChartRenderer.renderLineChart(
            chartId = "usersChart",
            labels = userLabels,
            seriesList =
                listOf(
                    SvgChartRenderer.Series(
                        name = "Active Users",
                        color = "#FFA900",
                        values = report.activeUsersOverTime.map { it.value.toDouble() },
                        fillOpacity = 0.15,
                    )
                ),
            yUnit = "",
        )

    // 6. Errors Breakdown
    val errorsSvg =
        SvgChartRenderer.renderErrorBreakdown(report.errors.map { Pair(it.message, it.count) })

    // 7. Table rows matching Gatling Highcharts exact column rules
    val requestRows =
        (listOf(global) + report.requestStats).joinToString("\n") { req ->
          val isGlobal = req.name == "Global"
          val rowClass = if (isGlobal) "row-global" else "row-req"
          val nameDisplay = if (isGlobal) "All Requests" else req.name
          val koPercent =
              if (req.totalCount > 0) {
                if (req.koCount == 0L) "-"
                else String.format("%.2f%%", (req.koCount.toDouble() / req.totalCount) * 100)
              } else "-"
          val meanRps = String.format("%.2f", req.meanRequestsPerSec)
          """
          <tr class="$rowClass">
            <td class="col-name">${escapeHtml(nameDisplay)}</td>
            <td class="col-num">${req.totalCount}</td>
            <td class="col-num text-ok">${req.okCount}</td>
            <td class="col-num ${if (req.koCount > 0) "text-ko" else "text-muted"}">${req.koCount}</td>
            <td class="col-num ${if (req.koCount > 0) "text-ko" else "text-muted"}">$koPercent</td>
            <td class="col-num">$meanRps</td>
            <td class="col-num">${String.format("%.0f", req.minResponseTimeMs)}</td>
            <td class="col-num">${String.format("%.0f", req.p50ResponseTimeMs)}</td>
            <td class="col-num">${String.format("%.0f", req.p75ResponseTimeMs)}</td>
            <td class="col-num text-warn">${String.format("%.0f", req.p95ResponseTimeMs)}</td>
            <td class="col-num text-ko">${String.format("%.0f", req.p99ResponseTimeMs)}</td>
            <td class="col-num">${String.format("%.0f", req.maxResponseTimeMs)}</td>
            <td class="col-num">${String.format("%.0f", req.meanResponseTimeMs)}</td>
            <td class="col-num">${String.format("%.0f", req.stdDevResponseTimeMs)}</td>
          </tr>
          """
        }

    // 8. Errors table rows
    val errorRows =
        if (report.errors.isNotEmpty()) {
          report.errors.joinToString("\n") { err ->
            """
            <tr>
              <td class="col-name text-ko">${escapeHtml(err.message)}</td>
              <td class="col-num text-ko">${err.count}</td>
              <td class="col-num text-ko">${String.format("%.1f%%", err.percentage)}</td>
            </tr>
            """
          }
        } else ""

    return """
<!DOCTYPE html>
<html lang="en" data-theme="light">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Gatling Stats - Global Information</title>
  <style>
    :root {
      --gatling-bg: #f2f2f2;
      --gatling-surface: #ffffff;
      --gatling-surface-raised: #f7f7f7;
      --gatling-border: #dddddd;
      --gatling-text: #1f2024;
      --gatling-text-secondary: #666666;
      --gatling-text-muted: #999999;
      --gatling-orange: #f78557;
      --gatling-orange-active: #E37400;
      --gatling-success: #68b65c;
      --gatling-danger: #f15b4f;
      --gatling-total: #ffa900;
      --gatling-blue: #5E7BE2;
      --gatling-table-header: #434b56;
      --gatling-table-header-text: #ffffff;
      --gatling-row-global: rgba(247, 133, 87, 0.08);
      --gatling-row-hover: #f5f5f5;
      --gatling-border-radius: 2px;
      --gatling-font: Arial, Helvetica, sans-serif;
    }

    [data-theme="dark"] {
      --gatling-bg: #1e2225;
      --gatling-surface: #272c30;
      --gatling-surface-raised: #32383e;
      --gatling-border: #555555;
      --gatling-text: #dee2e6;
      --gatling-text-secondary: #aaaaaa;
      --gatling-text-muted: #777777;
      --gatling-orange: #fe8e5f;
      --gatling-orange-active: #FFAA22;
      --gatling-success: #5cb85c;
      --gatling-danger: #d9534f;
      --gatling-total: #ffa900;
      --gatling-blue: #5E7BE2;
      --gatling-table-header: #24292e;
      --gatling-table-header-text: #ffffff;
      --gatling-row-global: rgba(254, 142, 95, 0.12);
      --gatling-row-hover: #2f353a;
    }

    * { box-sizing: border-box; margin: 0; padding: 0; }

    body {
      font-family: var(--gatling-font);
      background-color: var(--gatling-bg);
      color: var(--gatling-text);
      font-size: 12px;
      line-height: 1.4;
    }

    .frise {
      height: 4px;
      background-color: var(--gatling-orange);
      width: 100%;
    }

    .head {
      background-color: var(--gatling-surface);
      border-bottom: 1px solid var(--gatling-border);
      padding: 10px 24px;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .head-title {
      font-size: 18px;
      font-weight: 700;
      color: var(--gatling-text);
      letter-spacing: -0.3px;
    }

    .head-actions {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .theme-toggle-btn {
      background: var(--gatling-surface-raised);
      border: 1px solid var(--gatling-border);
      color: var(--gatling-text);
      padding: 4px 10px;
      font-size: 11px;
      cursor: pointer;
      border-radius: var(--gatling-border-radius);
    }
    .theme-toggle-btn:hover {
      background: var(--gatling-border);
    }

    .container {
      max-width: 1300px;
      margin: 16px auto;
      padding: 0 16px;
    }

    .content-header {
      margin-bottom: 16px;
    }

    .onglet {
      font-size: 20px;
      font-weight: bold;
      color: var(--gatling-text);
      margin-bottom: 8px;
    }

    .sous-menu {
      display: flex;
      border-bottom: 2px solid var(--gatling-orange);
    }

    .sous-menu .item {
      padding: 8px 18px;
      font-weight: bold;
      font-size: 13px;
      cursor: pointer;
      color: var(--gatling-text-secondary);
      background: var(--gatling-surface-raised);
      border: 1px solid var(--gatling-border);
      border-bottom: none;
      margin-right: 4px;
      border-radius: var(--gatling-border-radius) var(--gatling-border-radius) 0 0;
    }

    .sous-menu .item.ouvert {
      background: var(--gatling-orange);
      color: #ffffff;
      border-color: var(--gatling-orange);
    }

    .schema-container {
      display: grid;
      grid-template-columns: 1fr 280px 320px;
      gap: 16px;
      margin-bottom: 20px;
    }

    @media (max-width: 1024px) {
      .schema-container {
        grid-template-columns: 1fr;
      }
    }

    .card {
      background: var(--gatling-surface);
      border: 1px solid var(--gatling-border);
      border-radius: var(--gatling-border-radius);
      padding: 14px 16px;
    }

    .card-title {
      font-size: 13px;
      font-weight: bold;
      color: var(--gatling-text);
      margin-bottom: 10px;
      border-bottom: 1px solid var(--gatling-border);
      padding-bottom: 6px;
    }

    .sim-info-grid {
      display: grid;
      grid-template-columns: 80px 1fr;
      row-gap: 8px;
      font-size: 12px;
    }

    .sim-info-label {
      color: var(--gatling-text-secondary);
      font-weight: bold;
    }

    .sim-info-value {
      color: var(--gatling-text);
      word-break: break-all;
    }

    /* Gatling 2-tier table */
    .section-title {
      font-size: 15px;
      font-weight: bold;
      color: var(--gatling-text);
      margin: 20px 0 8px 0;
    }

    .table-wrapper {
      background: var(--gatling-surface);
      border: 1px solid var(--gatling-border);
      border-radius: var(--gatling-border-radius);
      overflow-x: auto;
      margin-bottom: 24px;
    }

    .gatling-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 11px;
      text-align: right;
    }

    .gatling-table th.header {
      background: var(--gatling-table-header);
      color: var(--gatling-table-header-text);
      padding: 6px 8px;
      border: 1px solid #5a6472;
      font-weight: bold;
      text-align: center;
    }

    .gatling-table th.col-req-head {
      text-align: left;
    }

    .gatling-table td {
      padding: 5px 8px;
      border: 1px solid var(--gatling-border);
    }

    .gatling-table .col-name {
      text-align: left;
      font-weight: 500;
    }

    .gatling-table .row-global {
      background: var(--gatling-row-global);
      font-weight: bold;
    }

    .gatling-table tr.row-req:hover {
      background: var(--gatling-row-hover);
    }

    .text-ok { color: var(--gatling-success); font-weight: bold; }
    .text-ko { color: var(--gatling-danger); font-weight: bold; }
    .text-warn { color: #d97706; font-weight: bold; }
    .text-muted { color: var(--gatling-text-muted); }

    /* Time series charts layout */
    .charts-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(580px, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }

    @media (max-width: 640px) {
      .charts-grid {
        grid-template-columns: 1fr;
      }
    }

    .chart-panel {
      background: var(--gatling-surface);
      border: 1px solid var(--gatling-border);
      border-radius: var(--gatling-border-radius);
      padding: 12px;
    }

    .chart-panel-title {
      font-size: 13px;
      font-weight: bold;
      color: var(--gatling-text);
      margin-bottom: 8px;
    }

    .chart-container {
      background: var(--gatling-surface-raised);
      border: 1px solid var(--gatling-border);
      border-radius: var(--gatling-border-radius);
      height: 250px;
      position: relative;
    }

    .chart-tooltip {
      position: absolute;
      display: none;
      background: var(--gatling-surface);
      border: 1px solid var(--gatling-border);
      padding: 8px 12px;
      font-size: 11px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.15);
      pointer-events: none;
      z-index: 100;
      border-radius: var(--gatling-border-radius);
    }

    .tooltip-title {
      font-weight: bold;
      color: var(--gatling-text-secondary);
      margin-bottom: 4px;
      border-bottom: 1px solid var(--gatling-border);
      padding-bottom: 2px;
    }

    .tooltip-row {
      display: flex;
      justify-content: space-between;
      gap: 14px;
      margin: 2px 0;
    }

    .tooltip-dot {
      display: inline-block;
      width: 7px;
      height: 7px;
      border-radius: 50%;
      margin-right: 5px;
    }

    .app-footer {
      text-align: center;
      padding: 24px;
      color: var(--gatling-text-muted);
      font-size: 11px;
      border-top: 1px solid var(--gatling-border);
      margin-top: 24px;
    }
  </style>
</head>
<body>
  <div class="frise"></div>
  <header class="head">
    <div class="head-title">Gatling Stats</div>
    <div class="head-actions">
      <button id="themeToggleBtn" class="theme-toggle-btn" onclick="toggleTheme()">Theme: Light</button>
    </div>
  </header>

  <div class="container">
    <div class="content-header">
      <div class="onglet">${escapeHtml(run.simulationClassName)}</div>
      <div class="sous-menu">
        <div class="item ouvert">Global</div>
        <div class="item" style="opacity: 0.6; cursor: default;">Details</div>
      </div>
    </div>

    <!-- Overview schema container -->
    <div class="schema-container">
      <div class="card">
        <div class="card-title">Response Time Ranges</div>
        <div style="height: 180px;">$rangesSvg</div>
      </div>
      <div class="card">
        <div class="card-title">Requests Breakdown</div>
        <div style="height: 180px;">$donutSvg</div>
      </div>
      <div class="card">
        <div class="card-title">Run Information</div>
        <div class="sim-info-grid">
          <span class="sim-info-label">Simulation:</span>
          <span class="sim-info-value">${escapeHtml(run.simulationClassName)}</span>
          <span class="sim-info-label">Run ID:</span>
          <span class="sim-info-value">${escapeHtml(run.runId)}</span>
          <span class="sim-info-label">Date:</span>
          <span class="sim-info-value">$startTime</span>
          <span class="sim-info-label">Duration:</span>
          <span class="sim-info-value">${run.durationSeconds}s</span>
          <span class="sim-info-label">Description:</span>
          <span class="sim-info-value">${escapeHtml(run.description.ifEmpty { "—" })}</span>
        </div>
      </div>
    </div>

    <!-- Statistics Table -->
    <div class="section-title">Statistics</div>
    <div class="table-wrapper">
      <table class="gatling-table">
        <thead>
          <tr>
            <th rowspan="2" class="header col-req-head"><span>Requests</span></th>
            <th colspan="5" class="header"><span>Executions</span></th>
            <th colspan="8" class="header"><span>Response Time (ms)</span></th>
          </tr>
          <tr>
            <th class="header"><span>Total</span></th>
            <th class="header"><span>OK</span></th>
            <th class="header"><span>KO</span></th>
            <th class="header"><span>% KO</span></th>
            <th class="header"><span>Cnt/s</span></th>
            <th class="header"><span>Min</span></th>
            <th class="header"><span>50th pct</span></th>
            <th class="header"><span>75th pct</span></th>
            <th class="header"><span>95th pct</span></th>
            <th class="header"><span>99th pct</span></th>
            <th class="header"><span>Max</span></th>
            <th class="header"><span>Mean</span></th>
            <th class="header"><span>Std Dev</span></th>
          </tr>
        </thead>
        <tbody>
          $requestRows
        </tbody>
      </table>
    </div>

    <!-- Errors Table -->
    ${if (report.errors.isNotEmpty()) """
    <div class="section-title">Errors</div>
    <div class="table-wrapper">
      <table class="gatling-table">
        <thead>
          <tr>
            <th class="header col-req-head"><span>Error Message</span></th>
            <th class="header" style="width: 100px;"><span>Count</span></th>
            <th class="header" style="width: 100px;"><span>Percentage</span></th>
          </tr>
        </thead>
        <tbody>
          $errorRows
        </tbody>
      </table>
    </div>
    """ else ""}

    <!-- Time Series Charts -->
    <div class="section-title">Charts</div>
    <div class="charts-grid">
      <div class="chart-panel">
        <div class="chart-panel-title">Response Time Percentiles over Time (OK)</div>
        <div class="chart-container">
          $percChartSvg
          <div id="percChart-tooltip" class="chart-tooltip"></div>
        </div>
      </div>

      <div class="chart-panel">
        <div class="chart-panel-title">Number of responses per second</div>
        <div class="chart-container">
          $rpsChartSvg
          <div id="rpsChart-tooltip" class="chart-tooltip"></div>
        </div>
      </div>

      <div class="chart-panel">
        <div class="chart-panel-title">Active Users over Time</div>
        <div class="chart-container">
          $usersChartSvg
          <div id="usersChart-tooltip" class="chart-tooltip"></div>
        </div>
      </div>

      <div class="chart-panel">
        <div class="chart-panel-title">Error Distribution</div>
        <div class="chart-container">
          $errorsSvg
        </div>
      </div>
    </div>
  </div>

  <footer class="app-footer">
    SpinoChart Classic Report &bull; ${escapeHtml(run.simulationClassName)}
  </footer>

  <script>
    let currentTheme = 'light';

    function toggleTheme() {
      currentTheme = currentTheme === 'light' ? 'dark' : 'light';
      document.documentElement.setAttribute('data-theme', currentTheme);
      document.getElementById('themeToggleBtn').textContent = 'Theme: ' + (currentTheme === 'light' ? 'Light' : 'Dark');
    }

    ${ThemeAssets.TOOLTIP_JS}
  </script>
</body>
</html>
    """
        .trimIndent()
  }
}
