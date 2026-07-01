<template>
  <div v-if="summary" class="mb-6">
    <el-row :gutter="16">
      <el-col :xs="12" :md="6">
        <div class="bg-white rounded-xl p-4 shadow-sm border border-gray-100">
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-400">总二维码</span>
            <span class="text-xl">📊</span>
          </div>
          <div class="text-2xl font-bold text-gray-800 mt-2">{{ summary.totalQrCodes }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="bg-white rounded-xl p-4 shadow-sm border border-gray-100">
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-400">总访问 PV</span>
            <span class="text-xl">👁️</span>
          </div>
          <div class="text-2xl font-bold text-blue-600 mt-2">{{ summary.totalPv }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="bg-white rounded-xl p-4 shadow-sm border border-gray-100">
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-400">总访客 UV</span>
            <span class="text-xl">👤</span>
          </div>
          <div class="text-2xl font-bold text-emerald-600 mt-2">{{ summary.totalUv }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="bg-white rounded-xl p-4 shadow-sm border border-gray-100">
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-400">今日 UV</span>
            <span class="text-xl">👤</span>
          </div>
          <div class="text-2xl font-bold text-violet-600 mt-2">{{ summary.todayUv }}</div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'SummaryCards',
  props: {
    summary: { type: Object, default: null }
  }
}
</script>
