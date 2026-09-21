package uk.co.developmentanddinosaurs.spinochart.theme

object ThemeAssets {

  val TOOLTIP_JS =
      """
      document.querySelectorAll('.spino-svg-chart').forEach(svg => {
        const chartId = svg.id;
        const tooltip = document.getElementById(chartId + '-tooltip');
        const crosshair = svg.querySelector('.crosshair');
        if (!tooltip || !crosshair) return;

        const paddingLeft = parseFloat(svg.dataset.paddingLeft);
        const paddingTop = parseFloat(svg.dataset.paddingTop);
        const plotWidth = parseFloat(svg.dataset.plotWidth);
        const labels = JSON.parse(svg.dataset.labels || '[]');
        const series = JSON.parse(svg.dataset.series || '[]');
        const unit = svg.dataset.unit || '';
        const numPoints = labels.length;

        if (numPoints <= 1) return;

        svg.addEventListener('mousemove', e => {
          const rect = svg.getBoundingClientRect();
          const scaleX = 600 / rect.width;
          const svgX = (e.clientX - rect.left) * scaleX;

          if (svgX < paddingLeft || svgX > paddingLeft + plotWidth) {
            crosshair.setAttribute('opacity', '0');
            tooltip.style.display = 'none';
            return;
          }

          const step = plotWidth / (svg.dataset.chartType === 'bar' ? numPoints : (numPoints - 1));
          const index = Math.min(numPoints - 1, Math.max(0, Math.floor((svgX - paddingLeft) / step)));
          const targetX = svg.dataset.chartType === 'bar' 
              ? paddingLeft + (index * step) + (step / 2)
              : paddingLeft + (index * step);

          crosshair.setAttribute('x1', targetX);
          crosshair.setAttribute('x2', targetX);
          crosshair.setAttribute('opacity', '1');

          let content = '<div class="tooltip-title">' + labels[index] + '</div>';
          series.forEach(s => {
            const val = s.values[index] !== undefined ? s.values[index] : '-';
            content += '<div class="tooltip-row">' +
              '<span><span class="tooltip-dot" style="background:' + s.color + '"></span>' + s.name + '</span>' +
              '<span style="font-weight:600">' + val + (unit ? ' ' + unit : '') + '</span>' +
            '</div>';
          });

          tooltip.innerHTML = content;
          tooltip.style.display = 'block';

          const parentRect = svg.parentElement.getBoundingClientRect();
          let tipX = e.clientX - parentRect.left + 15;
          let tipY = e.clientY - parentRect.top + 10;

          if (tipX + 160 > parentRect.width) {
            tipX -= 170;
          }
          tooltip.style.left = tipX + 'px';
          tooltip.style.top = tipY + 'px';
        });

        svg.addEventListener('mouseleave', () => {
          crosshair.setAttribute('opacity', '0');
          tooltip.style.display = 'none';
        });
      });
      """
          .trimIndent()
}
