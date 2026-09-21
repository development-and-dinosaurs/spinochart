package uk.co.developmentanddinosaurs.spinochart.chart

import kotlin.math.max
import kotlin.math.pow
import uk.co.developmentanddinosaurs.spinochart.model.ResponseTimeRanges
import uk.co.developmentanddinosaurs.spinochart.util.HtmlUtils

object SvgChartRenderer {

  data class Series(
      val name: String,
      val color: String,
      val values: List<Double>,
      val fillOpacity: Double = 0.0,
  )

  data class DonutSlice(
      val label: String,
      val value: Double,
      val color: String,
  )

  fun renderLineChart(
      chartId: String,
      labels: List<String>,
      seriesList: List<Series>,
      yUnit: String = "ms",
      width: Int = 600,
      height: Int = 250,
  ): String {
    if (labels.isEmpty() || seriesList.isEmpty()) {
      return emptyChartSvg("No timeseries data available", width, height)
    }

    val paddingLeft = 55.0
    val paddingRight = 20.0
    val paddingTop = 30.0
    val paddingBottom = 35.0
    val plotWidth = width - paddingLeft - paddingRight
    val plotHeight = height - paddingTop - paddingBottom

    val rawMaxY = seriesList.flatMap { it.values }.maxOrNull() ?: 1.0
    val maxY = calculateNiceMax(rawMaxY)
    val numYTicks = 4

    // Y Axis grid lines & labels
    val yGridLines = StringBuilder()
    for (i in 0..numYTicks) {
      val yVal = (maxY / numYTicks) * i
      val yPos = paddingTop + plotHeight - (yVal / maxY) * plotHeight
      yGridLines.append(
          """
          <line x1="$paddingLeft" y1="$yPos" x2="${width - paddingRight}" y2="$yPos" stroke="var(--border-subtle)" stroke-dasharray="3 3" stroke-width="1"/>
          <text x="${paddingLeft - 8}" y="${yPos + 3}" fill="var(--text-secondary)" font-size="10" font-family="var(--font-mono)" text-anchor="end">${String.format("%.0f", yVal)}$yUnit</text>
          """
              .trimIndent()
      )
    }

    // X Axis ticks
    val xTicks = StringBuilder()
    val xStep = if (labels.size > 1) plotWidth / (labels.size - 1) else 0.0
    val labelInterval = max(1, (labels.size / 5))
    for (i in labels.indices) {
      if (i % labelInterval == 0 || i == labels.size - 1) {
        val xPos = paddingLeft + (i * xStep)
        val shortLabel = labels[i].substringAfter(" ")
        xTicks.append(
            """
            <text x="$xPos" y="${height - 12}" fill="var(--text-secondary)" font-size="10" font-family="var(--font-mono)" text-anchor="middle">$shortLabel</text>
            """
                .trimIndent()
        )
      }
    }

    // Series paths
    val pathsSvg = StringBuilder()
    for (series in seriesList) {
      val pathD = StringBuilder()
      val areaPoints = StringBuilder()
      val bottomY = paddingTop + plotHeight

      for (i in series.values.indices) {
        val x = paddingLeft + (i * xStep)
        val clampedVal = max(0.0, series.values[i])
        val y = bottomY - ((clampedVal / maxY) * plotHeight)

        if (i == 0) {
          pathD.append("M $x $y")
          areaPoints.append("$x,$bottomY $x,$y")
        } else {
          pathD.append(" L $x $y")
          areaPoints.append(" $x,$y")
        }

        if (i == series.values.size - 1) {
          areaPoints.append(" $x,$bottomY")
        }
      }

      if (series.fillOpacity > 0.0) {
        pathsSvg.append(
            """
            <polygon points="$areaPoints" fill="${series.color}" opacity="${series.fillOpacity}"/>
            """
                .trimIndent()
        )
      }

      pathsSvg.append(
          """
          <path d="$pathD" fill="none" stroke="${series.color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          """
              .trimIndent()
      )
    }

    // Legends
    val legendSvg = StringBuilder()
    var legendX = paddingLeft
    for (series in seriesList) {
      legendSvg.append(
          """
          <rect x="$legendX" y="10" width="10" height="10" rx="2" fill="${series.color}"/>
          <text x="${legendX + 14}" y="19" fill="var(--text-secondary)" font-size="11" font-family="var(--font-sans)">${escapeXml(series.name)}</text>
          """
              .trimIndent()
      )
      legendX += series.name.length * 7 + 30
    }

    // JSON metadata for tooltip interaction
    val jsonLabels = labels.joinToString(",") { "\"$it\"" }
    val jsonSeries =
        seriesList.joinToString(",") { s ->
          "{\"name\":\"${escapeXml(s.name)}\",\"color\":\"${s.color}\",\"values\":[${s.values.joinToString(",")}]}"
        }

    return """
    <svg id="$chartId" viewBox="0 0 $width $height" width="100%" height="100%" class="spino-svg-chart"
         data-chart-type="line"
         data-padding-left="$paddingLeft"
         data-padding-top="$paddingTop"
         data-plot-width="$plotWidth"
         data-plot-height="$plotHeight"
         data-labels='[$jsonLabels]'
         data-series='[$jsonSeries]'
         data-unit="$yUnit">
      <g class="grid-lines">$yGridLines</g>
      <g class="x-ticks">$xTicks</g>
      <g class="paths">$pathsSvg</g>
      <g class="legend">$legendSvg</g>
      <line class="crosshair" x1="0" y1="$paddingTop" x2="0" y2="${paddingTop + plotHeight}" stroke="var(--text-secondary)" stroke-width="1" stroke-dasharray="2 2" opacity="0"/>
    </svg>
    """
        .trimIndent()
  }

  fun renderStackedBarChart(
      chartId: String,
      labels: List<String>,
      seriesOk: List<Int>,
      seriesKo: List<Int>,
      okColor: String = "#2ea043",
      koColor: String = "#cf222e",
      width: Int = 600,
      height: Int = 250,
  ): String {
    if (labels.isEmpty()) {
      return emptyChartSvg("No throughput data available", width, height)
    }

    val paddingLeft = 50.0
    val paddingRight = 20.0
    val paddingTop = 30.0
    val paddingBottom = 35.0
    val plotWidth = width - paddingLeft - paddingRight
    val plotHeight = height - paddingTop - paddingBottom

    val totals =
        labels.indices.map {
          (seriesOk.getOrElse(it) { 0 } + seriesKo.getOrElse(it) { 0 }).toDouble()
        }
    val rawMaxY = totals.maxOrNull() ?: 1.0
    val maxY = calculateNiceMax(rawMaxY)
    val numYTicks = 4

    // Y Axis grid lines & labels
    val yGridLines = StringBuilder()
    for (i in 0..numYTicks) {
      val yVal = (maxY / numYTicks) * i
      val yPos = paddingTop + plotHeight - (yVal / maxY) * plotHeight
      yGridLines.append(
          """
          <line x1="$paddingLeft" y1="$yPos" x2="${width - paddingRight}" y2="$yPos" stroke="var(--border-subtle)" stroke-dasharray="3 3" stroke-width="1"/>
          <text x="${paddingLeft - 8}" y="${yPos + 3}" fill="var(--text-secondary)" font-size="10" font-family="var(--font-mono)" text-anchor="end">${String.format("%.0f", yVal)}</text>
          """
              .trimIndent()
      )
    }

    // Bars
    val barSlotWidth = plotWidth / labels.size
    val barWidth = max(2.0, barSlotWidth * 0.6)
    val barOffset = (barSlotWidth - barWidth) / 2.0
    val bottomY = paddingTop + plotHeight

    val barsSvg = StringBuilder()
    val xTicks = StringBuilder()
    val labelInterval = max(1, (labels.size / 5))

    for (i in labels.indices) {
      val x = paddingLeft + (i * barSlotWidth) + barOffset
      val okVal = seriesOk.getOrElse(i) { 0 }
      val koVal = seriesKo.getOrElse(i) { 0 }

      val okHeight = (okVal / maxY) * plotHeight
      val koHeight = (koVal / maxY) * plotHeight

      val okY = bottomY - okHeight
      val koY = okY - koHeight

      if (okHeight > 0) {
        barsSvg.append(
            """<rect x="$x" y="$okY" width="$barWidth" height="$okHeight" fill="$okColor" rx="1"/>"""
        )
      }
      if (koHeight > 0) {
        barsSvg.append(
            """<rect x="$x" y="$koY" width="$barWidth" height="$koHeight" fill="$koColor" rx="1"/>"""
        )
      }

      if (i % labelInterval == 0 || i == labels.size - 1) {
        val centerX = paddingLeft + (i * barSlotWidth) + (barSlotWidth / 2.0)
        val shortLabel = labels[i].substringAfter(" ")
        xTicks.append(
            """
            <text x="$centerX" y="${height - 12}" fill="var(--text-secondary)" font-size="10" font-family="var(--font-mono)" text-anchor="middle">$shortLabel</text>
            """
                .trimIndent()
        )
      }
    }

    // Legend
    val legendSvg =
        """
      <rect x="$paddingLeft" y="10" width="10" height="10" rx="2" fill="$okColor"/>
      <text x="${paddingLeft + 14}" y="19" fill="var(--text-secondary)" font-size="11" font-family="var(--font-sans)">OK</text>
      <rect x="${paddingLeft + 55}" y="10" width="10" height="10" rx="2" fill="$koColor"/>
      <text x="${paddingLeft + 69}" y="19" fill="var(--text-secondary)" font-size="11" font-family="var(--font-sans)">KO</text>
    """
            .trimIndent()

    val jsonLabels = labels.joinToString(",") { "\"$it\"" }
    val jsonSeries =
        """[{"name":"OK","color":"$okColor","values":[${seriesOk.joinToString(",")}]},{"name":"KO","color":"$koColor","values":[${seriesKo.joinToString(",")}]}]"""

    return """
    <svg id="$chartId" viewBox="0 0 $width $height" width="100%" height="100%" class="spino-svg-chart"
         data-chart-type="bar"
         data-padding-left="$paddingLeft"
         data-padding-top="$paddingTop"
         data-plot-width="$plotWidth"
         data-plot-height="$plotHeight"
         data-labels='[$jsonLabels]'
         data-series='$jsonSeries'
         data-unit="req/s">
      <g class="grid-lines">$yGridLines</g>
      <g class="x-ticks">$xTicks</g>
      <g class="bars">$barsSvg</g>
      <g class="legend">$legendSvg</g>
      <line class="crosshair" x1="0" y1="$paddingTop" x2="0" y2="${paddingTop + plotHeight}" stroke="var(--text-secondary)" stroke-width="1" stroke-dasharray="2 2" opacity="0"/>
    </svg>
    """
        .trimIndent()
  }

  fun renderErrorBreakdown(
      errors: List<Pair<String, Long>>,
      width: Int = 600,
      height: Int = 250,
  ): String {
    if (errors.isEmpty()) {
      return """
      <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; min-height: 220px; color: var(--success); font-family: var(--font-mono);">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-bottom: 8px;">
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
          <polyline points="22 4 12 14.01 9 11.01"></polyline>
        </svg>
        <span style="font-size: 13px; font-weight: 500;">Zero errors recorded during simulation</span>
      </div>
      """
          .trimIndent()
    }

    val totalErrors = errors.sumOf { it.second }.toDouble()
    val colors = listOf("#cf222e", "#d29922", "#db6d28", "#8957e5", "#0969da")

    val rowsSvg = StringBuilder()
    var colorIdx = 0

    for ((msg, count) in errors) {
      val color = colors[colorIdx % colors.size]
      val pct = if (totalErrors > 0) (count / totalErrors) * 100 else 0.0
      rowsSvg.append(
          """
          <div style="margin-bottom: 12px;">
            <div style="display: flex; justify-content: space-between; font-size: 11px; margin-bottom: 4px;">
              <span style="color: var(--text-primary); font-family: var(--font-mono); word-break: break-all;">${escapeXml(msg)}</span>
              <span style="color: var(--text-secondary); font-family: var(--font-mono); margin-left: 12px; white-space: nowrap;">$count (${String.format("%.1f", pct)}%)</span>
            </div>
            <div style="background: var(--border-muted); border-radius: 3px; height: 6px; width: 100%; overflow: hidden;">
              <div style="background: $color; width: ${String.format("%.1f", pct)}%; height: 100%;"></div>
            </div>
          </div>
          """
              .trimIndent()
      )
      colorIdx++
    }

    return """
    <div style="padding: 12px 16px; height: 100%; overflow-y: auto;">
      $rowsSvg
    </div>
    """
        .trimIndent()
  }

  fun renderDonutChart(
      chartId: String,
      slices: List<DonutSlice>,
      centerLabel: String = "",
      centerValue: String = "",
      width: Int = 260,
      height: Int = 180,
  ): String {
    val total = slices.sumOf { it.value }
    if (total <= 0.0) {
      return emptyChartSvg("No distribution data", width, height)
    }

    val cx = 85.0
    val cy = 90.0
    val radius = 55.0
    val strokeWidth = 22.0
    val circumference = 2 * Math.PI * radius

    var currentOffset = 0.0
    val circlesSvg = StringBuilder()
    val legendSvg = StringBuilder()
    var legendY = 45.0

    for (slice in slices) {
      if (slice.value <= 0) continue
      val fraction = slice.value / total
      val dash = fraction * circumference
      val pctStr = String.format("%.1f%%", fraction * 100)

      circlesSvg.append(
          """
          <circle cx="$cx" cy="$cy" r="$radius" fill="none" stroke="${slice.color}" stroke-width="$strokeWidth"
                  stroke-dasharray="$dash $circumference" stroke-dashoffset="-$currentOffset"
                  transform="rotate(-90 $cx $cy)"/>
          """
              .trimIndent()
      )
      currentOffset += dash

      legendSvg.append(
          """
          <rect x="165" y="${legendY}" width="10" height="10" rx="2" fill="${slice.color}"/>
          <text x="182" y="${legendY + 9}" fill="var(--text-primary)" font-size="11" font-family="var(--font-sans)">${escapeXml(slice.label)}: <tspan font-weight="bold">$pctStr</tspan></text>
          """
              .trimIndent()
      )
      legendY += 24.0
    }

    return """
    <svg id="$chartId" viewBox="0 0 $width $height" width="100%" height="100%">
      <g class="donut-slices">$circlesSvg</g>
      <text x="$cx" y="${cy - 4}" fill="var(--text-primary)" font-size="16" font-weight="bold" font-family="var(--font-sans)" text-anchor="middle">$centerValue</text>
      <text x="$cx" y="${cy + 14}" fill="var(--text-secondary)" font-size="11" font-family="var(--font-sans)" text-anchor="middle">$centerLabel</text>
      <g class="donut-legend">$legendSvg</g>
    </svg>
    """
        .trimIndent()
  }

  fun renderRangesChart(
      ranges: ResponseTimeRanges,
      width: Int = 420,
      height: Int = 180,
  ): String {
    val items =
        listOf(
            Triple("< ${ranges.lowerBound} ms", ranges.lowCount, "#68b65c"),
            Triple(
                "${ranges.lowerBound} - ${ranges.higherBound} ms",
                ranges.middleCount,
                "#FFDD00",
            ),
            Triple("> ${ranges.higherBound} ms", ranges.highCount, "#FFA900"),
            Triple("failed", ranges.koCount, "#f15b4f"),
        )
    val maxCount = max(1.0, items.maxOf { it.second }.toDouble())
    val paddingLeft = 35.0
    val paddingRight = 15.0
    val paddingTop = 25.0
    val paddingBottom = 35.0
    val plotWidth = width - paddingLeft - paddingRight
    val plotHeight = height - paddingTop - paddingBottom

    val slotWidth = plotWidth / items.size
    val barWidth = 32.0

    val barsSvg = StringBuilder()
    for (i in items.indices) {
      val (label, count, color) = items[i]
      val x = paddingLeft + (i * slotWidth) + (slotWidth - barWidth) / 2.0
      val h = (count / maxCount) * plotHeight
      val y = paddingTop + plotHeight - h

      if (h > 0) {
        barsSvg.append(
            """<rect x="$x" y="$y" width="$barWidth" height="$h" fill="$color" rx="2"/>"""
        )
      }
      barsSvg.append(
          """
          <text x="${x + barWidth / 2.0}" y="${y - 5}" fill="var(--text-primary)" font-size="10" font-weight="bold" font-family="var(--font-sans)" text-anchor="middle">$count</text>
          <text x="${x + barWidth / 2.0}" y="${height - 12}" fill="var(--text-secondary)" font-size="10" font-family="var(--font-sans)" text-anchor="middle">$label</text>
          """
              .trimIndent()
      )
    }

    return """
    <svg viewBox="0 0 $width $height" width="100%" height="100%">
      <line x1="$paddingLeft" y1="${paddingTop + plotHeight}" x2="${width - paddingRight}" y2="${paddingTop + plotHeight}" stroke="var(--border-subtle)" stroke-width="1"/>
      <g class="bars">$barsSvg</g>
    </svg>
    """
        .trimIndent()
  }

  private fun emptyChartSvg(message: String, width: Int, height: Int): String =
      """
      <svg viewBox="0 0 $width $height" width="100%" height="100%">
        <text x="${width / 2}" y="${height / 2}" fill="var(--text-secondary)" font-size="12" font-family="var(--font-mono)" text-anchor="middle">$message</text>
      </svg>
      """
          .trimIndent()

  private fun calculateNiceMax(rawMax: Double): Double {
    if (rawMax <= 0.0) return 1.0
    val target = rawMax * 1.15
    val magnitude = 10.0.pow(kotlin.math.floor(kotlin.math.log10(target)))
    val normalized = target / magnitude
    val niceNormalized =
        when {
          normalized <= 1.0 -> 1.0
          normalized <= 2.0 -> 2.0
          normalized <= 2.5 -> 2.5
          normalized <= 5.0 -> 5.0
          else -> 10.0
        }
    return niceNormalized * magnitude
  }

  private fun escapeXml(value: String): String =
      HtmlUtils.escapeHtml(value).replace("&#39;", "&apos;")
}
