<template>
  <div style="position: relative; height: 200px; width: 100%">
    <canvas ref="canvas"></canvas>
  </div>
</template>

<script>
import { Chart, ArcElement, Tooltip, Legend } from 'chart.js'
Chart.register(ArcElement, Tooltip, Legend)

export default {
  name: 'BrowserChart',
  props: {
    visits: { type: Array, default: () => [] }
  },
  data: () => ({ chart: null }),
  watch: {
    visits: { deep: true, handler() { this.$nextTick(this.updateChart) } }
  },
  mounted() { this.$nextTick(this.updateChart) },
  beforeDestroy() {
    if (this.chart) { this.chart.destroy(); this.chart = null }
  },
  methods: {
    aggregateBrowsers(visits) {
      const map = {}
      visits.forEach(v => { const b = v.browser || '未知'; map[b] = (map[b] || 0) + 1 })
      const sorted = Object.entries(map).sort((a, b) => b[1] - a[1])
      const top = sorted.slice(0, 5)
      const rest = sorted.slice(5)
      if (rest.length) top.push(['其他', rest.reduce((s, [, c]) => s + c, 0)])
      return { labels: top.map(([n]) => n), data: top.map(([, c]) => c) }
    },
    updateChart() {
      if (!this.$refs.canvas || !this.visits || !this.visits.length) return
      if (this.chart) { this.chart.destroy() }

      const { labels, data } = this.aggregateBrowsers(this.visits)
      const colors = ['#2563eb', '#10b981', '#f59e0b', '#8b5cf6', '#ef4444', '#94a3b8']

      this.chart = new Chart(this.$refs.canvas, {
        type: 'doughnut',
        data: {
          labels,
          datasets: [{
            data, borderWidth: 2, borderColor: '#fff',
            backgroundColor: colors.slice(0, labels.length),
          }]
        },
        options: {
          responsive: true, maintainAspectRatio: false, cutout: '60%',
          plugins: {
            legend: {
              position: 'right',
              labels: { usePointStyle: true, padding: 12, font: { size: 11 } }
            },
            tooltip: {
              backgroundColor: 'rgba(15,23,42,0.9)',
              padding: 10, cornerRadius: 8,
              callbacks: {
                label(ctx) {
                  const t = ctx.dataset.data.reduce((a, b) => a + b, 0)
                  return ` ${ctx.label}: ${ctx.parsed} (${(ctx.parsed / t * 100).toFixed(1)}%)`
                }
              }
            }
          }
        }
      })
    }
  }
}
</script>
