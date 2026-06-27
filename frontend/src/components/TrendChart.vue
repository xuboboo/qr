<template>
  <div style="position: relative; height: 240px; width: 100%">
    <canvas ref="canvas"></canvas>
  </div>
</template>

<script>
import {
  Chart,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js'

Chart.register(
  CategoryScale, LinearScale, PointElement, LineElement,
  Title, Tooltip, Legend, Filler
)

export default {
  name: 'TrendChart',
  props: {
    data: { type: Array, default: () => [] }
  },
  data: () => ({ chart: null }),
  watch: {
    data: { deep: true, handler() { this.$nextTick(this.updateChart) } }
  },
  mounted() { this.$nextTick(this.updateChart) },
  beforeDestroy() {
    if (this.chart) { this.chart.destroy(); this.chart = null }
  },
  methods: {
    updateChart() {
      if (!this.$refs.canvas || !this.data || !this.data.length) return
      if (this.chart) { this.chart.destroy() }

      this.chart = new Chart(this.$refs.canvas, {
        type: 'line',
        data: {
          labels: this.data.map(d => d.date),
          datasets: [
            {
              label: 'PV', data: this.data.map(d => d.pv),
              borderColor: '#2563eb',
              backgroundColor: 'rgba(37,99,235,0.08)',
              borderWidth: 2.5, pointRadius: 4, pointHoverRadius: 6,
              pointBackgroundColor: '#2563eb',
              pointBorderColor: '#fff', pointBorderWidth: 2,
              tension: 0.3, fill: true,
            },
            {
              label: 'UV', data: this.data.map(d => d.uv),
              borderColor: '#10b981',
              backgroundColor: 'rgba(16,185,129,0.08)',
              borderWidth: 2.5, pointRadius: 4, pointHoverRadius: 6,
              pointBackgroundColor: '#10b981',
              pointBorderColor: '#fff', pointBorderWidth: 2,
              tension: 0.3, fill: true,
            }
          ]
        },
        options: {
          responsive: true, maintainAspectRatio: false,
          interaction: { intersect: false, mode: 'index' },
          plugins: {
            legend: {
              position: 'top', align: 'end',
              labels: { usePointStyle: true, padding: 16, font: { size: 12 } }
            },
            tooltip: {
              backgroundColor: 'rgba(15,23,42,0.9)',
              padding: 10, cornerRadius: 8,
            }
          },
          scales: {
            x: {
              grid: { display: false },
              ticks: { font: { size: 11 }, color: '#94a3b8' }
            },
            y: {
              beginAtZero: true,
              grid: { color: 'rgba(0,0,0,0.04)' },
              ticks: { font: { size: 11 }, color: '#94a3b8', precision: 0 }
            }
          }
        }
      })
    }
  }
}
</script>
