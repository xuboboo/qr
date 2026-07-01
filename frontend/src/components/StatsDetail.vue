<template>
  <div v-if="stats" class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden animate-slide-up">
    <div class="px-6 py-4 border-b border-gray-100">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <span class="w-1 h-5 bg-gradient-to-b from-amber-500 to-orange-600 rounded-full"></span>
          <span class="text-base font-semibold text-gray-800">统计详情：{{ stats.qrCode.name }}</span>
        </div>
        <el-button size="mini" @click="$emit('refresh', stats.qrCode)" class="rounded-lg" icon="el-icon-refresh">刷新</el-button>
      </div>
    </div>
    <div class="p-6">
      <!-- 统计概览卡片 -->
      <el-row :gutter="16" class="mb-6">
        <el-col :span="8">
          <div class="stat-card">
            <div class="flex items-center justify-between mb-3">
              <span class="text-sm text-gray-400 font-medium">总访问 PV</span>
              <span class="text-2xl">👁️</span>
            </div>
            <div class="text-3xl font-bold text-gray-800">{{ stats.pv }}</div>
            <div class="mt-2 h-1.5 bg-gray-100 rounded-full overflow-hidden">
              <div class="h-full bg-gradient-to-r from-blue-400 to-blue-600 rounded-full" style="width: 100%"></div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card">
            <div class="flex items-center justify-between mb-3">
              <span class="text-sm text-gray-400 font-medium">累计 UV</span>
              <span class="text-2xl">👤</span>
            </div>
            <div class="text-3xl font-bold text-gray-800">{{ stats.uv }}</div>
            <div class="mt-2 h-1.5 bg-gray-100 rounded-full overflow-hidden">
              <div class="h-full bg-gradient-to-r from-emerald-400 to-emerald-600 rounded-full" style="width: 100%"></div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card">
            <div class="flex items-center justify-between mb-3">
              <span class="text-sm text-gray-400 font-medium">今日 UV</span>
              <span class="text-2xl">👤</span>
            </div>
            <div class="text-3xl font-bold text-blue-600">{{ stats.todayUv }}</div>
            <div class="mt-2 h-1.5 bg-gray-100 rounded-full overflow-hidden">
              <div class="h-full bg-gradient-to-r from-violet-400 to-violet-600 rounded-full" style="width: 100%"></div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 最近 7 天趋势 -->
      <h3 class="text-base font-semibold text-gray-700 mb-3 flex items-center gap-2">
        <span class="w-1.5 h-1.5 bg-blue-500 rounded-full"></span>
        最近 7 天趋势
      </h3>
      <div class="bg-gray-50 rounded-xl p-4 mb-4">
        <trend-chart :data="stats.last7Days" />
      </div>
      <el-table :data="stats.last7Days" border size="small" style="width: 100%" class="mb-6">
        <el-table-column prop="date" label="日期" />
        <el-table-column prop="pv" label="PV" align="center">
          <template slot-scope="scope">
            <span class="text-blue-600 font-medium">{{ scope.row.pv }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="uv" label="UV" align="center">
          <template slot-scope="scope">
            <span class="text-emerald-600 font-medium">{{ scope.row.uv }}</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 最近访问记录 -->
      <h3 class="text-base font-semibold text-gray-700 mb-3 flex items-center gap-2">
        <span class="w-1.5 h-1.5 bg-amber-500 rounded-full"></span>
        最近访问记录
      </h3>
      <el-row :gutter="16" class="mb-4">
        <el-col :span="24" :md="10" class="mb-4 md:mb-0">
          <div class="bg-gray-50 rounded-xl p-4 h-full">
            <h4 class="text-sm text-gray-500 font-medium mb-2">浏览器分布</h4>
            <browser-chart :visits="stats.latestVisits" />
          </div>
        </el-col>
        <el-col :span="24" :md="14">
          <el-table :data="stats.latestVisits" border size="small" style="width: 100%">
            <el-table-column prop="createdAt" label="访问时间" width="165" />
            <el-table-column prop="ip" label="IP" width="130" />
            <el-table-column prop="location" label="地区" width="150" show-overflow-tooltip>
              <template slot-scope="scope">
                <span>{{ scope.row.location || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="device" label="设备" width="95" />
            <el-table-column prop="browser" label="浏览器" width="120" />
            <el-table-column prop="referer" label="来源" min-width="160" show-overflow-tooltip />
            <el-table-column prop="userAgent" label="User-Agent" min-width="220" show-overflow-tooltip />
          </el-table>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import TrendChart from './TrendChart.vue'
import BrowserChart from './BrowserChart.vue'

export default {
  name: 'StatsDetail',
  components: { TrendChart, BrowserChart },
  props: {
    stats: { type: Object, default: null }
  }
}
</script>
