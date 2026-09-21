package uk.co.developmentanddinosaurs.spinochart.theme

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import uk.co.developmentanddinosaurs.spinochart.chart.SvgChartRenderer
import uk.co.developmentanddinosaurs.spinochart.model.SimulationReport
import uk.co.developmentanddinosaurs.spinochart.util.HtmlUtils.escapeHtml

class SpinoTheme : ReportTheme {

  override val id: String = "spino"
  override val displayName: String = "Spino (Flagship)"
  override val description: String =
      "Sleek, high-density performance dashboard with native vector SVG charts, zero JS libraries, and 100% MIT license."

  private val dateFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())

  override fun render(report: SimulationReport): String {
    val run = report.runInfo
    val global = report.globalStats
    val startTime = dateFormatter.format(Instant.ofEpochMilli(run.startTimeEpochMs))
    val endTime = dateFormatter.format(Instant.ofEpochMilli(run.endTimeEpochMs))
    val successRate =
        if (global.totalCount > 0) {
          String.format("%.2f", (global.okCount.toDouble() / global.totalCount) * 100)
        } else "0.00"
    val errorRate =
        if (global.totalCount > 0) {
          String.format("%.2f", (global.koCount.toDouble() / global.totalCount) * 100)
        } else "0.00"

    // 1. Render Percentiles SVG
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
                        "P50",
                        "#58a6ff",
                        report.percentilesOverTime.map { it.p50 },
                    ),
                    SvgChartRenderer.Series(
                        "P75",
                        "#56d4dd",
                        report.percentilesOverTime.map { it.p75 },
                    ),
                    SvgChartRenderer.Series(
                        "P95",
                        "#d29922",
                        report.percentilesOverTime.map { it.p95 },
                    ),
                    SvgChartRenderer.Series(
                        "P99",
                        "#f85149",
                        report.percentilesOverTime.map { it.p99 },
                    ),
                ),
            yUnit = "ms",
        )

    // 2. Render RPS Stacked Bar SVG
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
        )

    // 3. Render Active Users SVG
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
                        color = "#8957e5",
                        values = report.activeUsersOverTime.map { it.value.toDouble() },
                        fillOpacity = 0.12,
                    )
                ),
            yUnit = "",
        )

    // 4. Render Errors Breakdown
    val errorsSvg =
        SvgChartRenderer.renderErrorBreakdown(report.errors.map { Pair(it.message, it.count) })

    // 5. Table rows
    val requestRows =
        (listOf(global) + report.requestStats).joinToString("\n") { req ->
          val isGlobal = req.name == "Global"
          val rowClass = if (isGlobal) "row-global" else "row-req"
          val errPercent =
              if (req.totalCount > 0) {
                String.format("%.2f%%", (req.koCount.toDouble() / req.totalCount) * 100)
              } else "0.00%"
          """
          <tr class="$rowClass">
            <td class="name-cell">${escapeHtml(req.name)}</td>
            <td class="num-cell font-mono">${req.totalCount}</td>
            <td class="num-cell font-mono text-success">${req.okCount}</td>
            <td class="num-cell font-mono ${if (req.koCount > 0) "text-danger" else "text-muted"}">${req.koCount}</td>
            <td class="num-cell font-mono ${if (req.koCount > 0) "text-danger" else "text-muted"}">$errPercent</td>
            <td class="num-cell font-mono">${String.format("%.2f", req.meanRequestsPerSec)}</td>
            <td class="num-cell font-mono">${String.format("%.0f", req.minResponseTimeMs)}</td>
            <td class="num-cell font-mono">${String.format("%.0f", req.p50ResponseTimeMs)}</td>
            <td class="num-cell font-mono">${String.format("%.0f", req.p75ResponseTimeMs)}</td>
            <td class="num-cell font-mono text-warning">${String.format("%.0f", req.p95ResponseTimeMs)}</td>
            <td class="num-cell font-mono text-accent">${String.format("%.0f", req.p99ResponseTimeMs)}</td>
            <td class="num-cell font-mono">${String.format("%.0f", req.maxResponseTimeMs)}</td>
            <td class="num-cell font-mono">${String.format("%.0f", req.meanResponseTimeMs)}</td>
            <td class="num-cell font-mono">${String.format("%.0f", req.stdDevResponseTimeMs)}</td>
          </tr>
          """
        }

    return """
<!DOCTYPE html>
<html lang="en" data-theme="dark">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>SpinoChart Report: ${escapeHtml(run.simulationClassName)}</title>
  <style>
    :root {
      --font-sans: -apple-system, BlinkMacSystemFont, "Segoe UI", "Noto Sans", Helvetica, Arial, sans-serif;
      --font-mono: ui-monospace, SFMono-Regular, "SF Mono", Menlo, Consolas, "Liberation Mono", monospace;
    }

    [data-theme="dark"] {
      --bg-page: #0d1117;
      --bg-surface: #161b22;
      --bg-surface-raised: #21262d;
      --border-subtle: #30363d;
      --border-muted: #21262d;
      --text-primary: #e6edf3;
      --text-secondary: #8b949e;
      --text-muted: #6e7681;
      --accent: #58a6ff;
      --success: #3fb950;
      --danger: #f85149;
      --warning: #d29922;
      --highlight: #db6d28;
      --table-header: #161b22;
      --table-row-hover: rgba(110, 118, 129, 0.1);
      --table-row-global: rgba(56, 139, 253, 0.08);
      --tooltip-bg: #1c2128;
      --tooltip-border: #444c56;
    }

    [data-theme="light"] {
      --bg-page: #f6f8fa;
      --bg-surface: #ffffff;
      --bg-surface-raised: #f3f4f6;
      --border-subtle: #d0d7de;
      --border-muted: #eaeef2;
      --text-primary: #1f2328;
      --text-secondary: #656d76;
      --text-muted: #8c959f;
      --accent: #0969da;
      --success: #1a7f37;
      --danger: #cf222e;
      --warning: #9a6700;
      --highlight: #bc4c00;
      --table-header: #f6f8fa;
      --table-row-hover: rgba(208, 215, 222, 0.32);
      --table-row-global: rgba(9, 105, 218, 0.06);
      --tooltip-bg: #ffffff;
      --tooltip-border: #d0d7de;
    }

    * { box-sizing: border-box; margin: 0; padding: 0; }

    body {
      font-family: var(--font-sans);
      background-color: var(--bg-page);
      color: var(--text-primary);
      line-height: 1.5;
      font-size: 13px;
      -webkit-font-smoothing: antialiased;
    }

    .font-mono {
      font-family: var(--font-mono);
      font-variant-numeric: tabular-nums;
    }

    .app-header {
      background-color: var(--bg-surface);
      border-bottom: 1px solid var(--border-subtle);
      padding: 12px 24px;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .brand-section {
      display: flex;
      align-items: baseline;
      gap: 12px;
    }

    .brand-title {
      font-size: 16px;
      font-weight: 700;
      color: var(--text-primary);
      letter-spacing: -0.2px;
    }

    .brand-meta {
      font-size: 12px;
      color: var(--text-secondary);
      font-family: var(--font-mono);
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .run-meta {
      text-align: right;
      font-size: 11px;
      color: var(--text-secondary);
      font-family: var(--font-mono);
    }

    .theme-toggle {
      background: var(--bg-surface-raised);
      border: 1px solid var(--border-subtle);
      color: var(--text-secondary);
      padding: 4px 10px;
      border-radius: 6px;
      font-size: 11px;
      cursor: pointer;
      font-family: var(--font-mono);
      transition: all 0.15s ease;
    }
    .theme-toggle:hover {
      color: var(--text-primary);
      border-color: var(--text-secondary);
    }

    .container {
      max-width: 1360px;
      margin: 0 auto;
      padding: 24px;
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .metrics-bar {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
      gap: 12px;
    }

    .metric-card {
      background-color: var(--bg-surface);
      border: 1px solid var(--border-subtle);
      border-radius: 6px;
      padding: 12px 14px;
    }

    .metric-label {
      font-size: 11px;
      color: var(--text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.3px;
      font-weight: 500;
      margin-bottom: 4px;
    }

    .metric-value {
      font-size: 20px;
      font-weight: 600;
      color: var(--text-primary);
      font-family: var(--font-mono);
      font-variant-numeric: tabular-nums;
    }

    .metric-unit {
      font-size: 11px;
      color: var(--text-muted);
      font-weight: 400;
      margin-left: 2px;
    }

    .charts-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }

    @media (max-width: 1024px) {
      .charts-grid { grid-template-columns: 1fr; }
    }

    .panel {
      background-color: var(--bg-surface);
      border: 1px solid var(--border-subtle);
      border-radius: 6px;
      overflow: hidden;
      position: relative;
    }

    .panel-header {
      padding: 10px 16px;
      border-bottom: 1px solid var(--border-muted);
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .panel-title {
      font-size: 12px;
      font-weight: 600;
      color: var(--text-primary);
      letter-spacing: -0.1px;
    }

    .panel-body {
      padding: 8px 12px 12px;
      position: relative;
    }

    .chart-container {
      width: 100%;
      height: 250px;
      position: relative;
    }

    .spino-svg-chart {
      user-select: none;
      overflow: visible;
    }

    /* Floating SVG Tooltip */
    .chart-tooltip {
      position: absolute;
      display: none;
      pointer-events: none;
      background: var(--tooltip-bg);
      border: 1px solid var(--tooltip-border);
      border-radius: 6px;
      padding: 8px 10px;
      font-size: 11px;
      font-family: var(--font-mono);
      color: var(--text-primary);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
      z-index: 100;
      white-space: nowrap;
    }

    .tooltip-title {
      font-weight: 600;
      color: var(--text-secondary);
      margin-bottom: 4px;
      font-size: 10px;
    }

    .tooltip-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      margin-top: 2px;
    }

    .tooltip-dot {
      width: 8px;
      height: 8px;
      border-radius: 2px;
      display: inline-block;
      margin-right: 6px;
    }

    .table-container {
      overflow-x: auto;
    }

    table.data-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 12px;
      text-align: left;
    }

    table.data-table th {
      background-color: var(--table-header);
      color: var(--text-secondary);
      font-weight: 600;
      padding: 8px 12px;
      border-bottom: 1px solid var(--border-subtle);
      font-size: 11px;
      text-transform: uppercase;
      letter-spacing: 0.3px;
    }

    table.data-table td {
      padding: 8px 12px;
      border-bottom: 1px solid var(--border-muted);
      color: var(--text-primary);
    }

    table.data-table tr.row-req:hover {
      background-color: var(--table-row-hover);
    }

    table.data-table tr.row-global {
      background-color: var(--table-row-global);
      font-weight: 600;
    }

    table.data-table th.num-cell,
    table.data-table td.num-cell {
      text-align: right;
    }

    .text-success { color: var(--success); }
    .text-danger { color: var(--danger); }
    .text-warning { color: var(--warning); }
    .text-accent { color: var(--highlight); }
    .text-muted { color: var(--text-muted); }

    .footer {
      text-align: center;
      padding: 20px;
      font-size: 11px;
      color: var(--text-muted);
      font-family: var(--font-mono);
      border-top: 1px solid var(--border-muted);
      margin-top: 24px;
    }
  </style>
</head>
<body>
  <header class="app-header">
    <div class="brand-section">
      <div class="brand-title">SpinoChart</div>
      <div class="brand-meta">${escapeHtml(run.simulationClassName)}</div>
    </div>
    <div class="header-actions">
      <div class="run-meta">
        <div>Run: <span style="color: var(--text-primary);">${escapeHtml(run.runId)}</span></div>
        <div>Duration: ${run.durationSeconds}s ($startTime &rarr; $endTime)</div>
      </div>
      <button id="themeToggleBtn" class="theme-toggle" onclick="toggleTheme()">Theme: Dark</button>
    </div>
  </header>

  <main class="container">
    <section class="metrics-bar">
      <div class="metric-card">
        <div class="metric-label">Requests</div>
        <div class="metric-value font-mono">${global.totalCount}</div>
      </div>
      <div class="metric-card">
        <div class="metric-label">Success Rate</div>
        <div class="metric-value font-mono text-success">$successRate<span class="metric-unit">%</span></div>
      </div>
      <div class="metric-card">
        <div class="metric-label">Failure Rate</div>
        <div class="metric-value font-mono ${if (global.koCount > 0) "text-danger" else "text-muted"}">$errorRate<span class="metric-unit">%</span></div>
      </div>
      <div class="metric-card">
        <div class="metric-label">Mean Latency</div>
        <div class="metric-value font-mono">${String.format("%.0f", global.meanResponseTimeMs)}<span class="metric-unit">ms</span></div>
      </div>
      <div class="metric-card">
        <div class="metric-label">Median (P50)</div>
        <div class="metric-value font-mono">${String.format("%.0f", global.p50ResponseTimeMs)}<span class="metric-unit">ms</span></div>
      </div>
      <div class="metric-card">
        <div class="metric-label">P95 Latency</div>
        <div class="metric-value font-mono text-warning">${String.format("%.0f", global.p95ResponseTimeMs)}<span class="metric-unit">ms</span></div>
      </div>
      <div class="metric-card">
        <div class="metric-label">P99 Latency</div>
        <div class="metric-value font-mono text-accent">${String.format("%.0f", global.p99ResponseTimeMs)}<span class="metric-unit">ms</span></div>
      </div>
      <div class="metric-card">
        <div class="metric-label">Max Latency</div>
        <div class="metric-value font-mono">${String.format("%.0f", global.maxResponseTimeMs)}<span class="metric-unit">ms</span></div>
      </div>
    </section>

    <section class="charts-grid">
      <div class="panel">
        <div class="panel-header">
          <div class="panel-title">Response Time Percentiles (OK)</div>
        </div>
        <div class="panel-body">
          <div class="chart-container">
            $percChartSvg
            <div id="percChart-tooltip" class="chart-tooltip"></div>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-header">
          <div class="panel-title">Throughput (Requests / Second)</div>
        </div>
        <div class="panel-body">
          <div class="chart-container">
            $rpsChartSvg
            <div id="rpsChart-tooltip" class="chart-tooltip"></div>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-header">
          <div class="panel-title">Active Users</div>
        </div>
        <div class="panel-body">
          <div class="chart-container">
            $usersChartSvg
            <div id="usersChart-tooltip" class="chart-tooltip"></div>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-header">
          <div class="panel-title">Errors Breakdown</div>
        </div>
        <div class="panel-body">
          <div class="chart-container">
            $errorsSvg
          </div>
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <div class="panel-title">Request Statistics</div>
        <div style="font-size: 11px; color: var(--text-secondary); font-family: var(--font-mono);">
          ${report.requestStats.size} endpoints
        </div>
      </div>
      <div class="table-container">
        <table class="data-table">
          <thead>
            <tr>
              <th>Request</th>
              <th class="num-cell">Total</th>
              <th class="num-cell text-success">OK</th>
              <th class="num-cell text-danger">KO</th>
              <th class="num-cell">% KO</th>
              <th class="num-cell">Cnt/s</th>
              <th class="num-cell">Min (ms)</th>
              <th class="num-cell">50th (ms)</th>
              <th class="num-cell">75th (ms)</th>
              <th class="num-cell text-warning">95th (ms)</th>
              <th class="num-cell text-accent">99th (ms)</th>
              <th class="num-cell">Max (ms)</th>
              <th class="num-cell">Mean (ms)</th>
              <th class="num-cell">Std Dev</th>
            </tr>
          </thead>
          <tbody>
            $requestRows
          </tbody>
        </table>
      </div>
    </section>
  </main>

  <footer class="footer">
    SpinoChart
  </footer>

  <script>
    let currentTheme = 'dark';

    function toggleTheme() {
      currentTheme = currentTheme === 'dark' ? 'light' : 'dark';
      document.documentElement.setAttribute('data-theme', currentTheme);
      document.getElementById('themeToggleBtn').textContent = 'Theme: ' + (currentTheme === 'dark' ? 'Dark' : 'Light');
    }

    // Lightweight Interactive Tooltip Handler (~40 lines of zero-dependency JS)
    ${ThemeAssets.TOOLTIP_JS}
  </script>
</body>
</html>
    """
        .trimIndent()
  }
}
