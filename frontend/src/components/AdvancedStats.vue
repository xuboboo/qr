<template>
  <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
    <div class="px-6 py-4 border-b border-gray-100">
      <div class="flex items-center gap-2">
        <span class="w-1 h-5 bg-gradient-to-b from-pink-500 to-rose-600 rounded-full"></span>
        <span class="text-base font-semibold text-gray-800">高级统计分析</span>
      </div>
    </div>
    <div class="p-6">
      <el-row :gutter="16">
        <!-- 访问来源统计 -->
        <el-col :xs="24" :md="12" class="mb-4">
          <div class="bg-gray-50 rounded-xl p-4">
            <h4 class="text-sm text-gray-500 font-medium mb-3">访问来源 Top10</h4>
            <div v-if="refererStats.length" class="space-y-2">
              <div v-for="item in refererStats" :key="item.referer" class="flex items-center gap-2">
                <span class="text-xs text-gray-500 w-24 truncate" :title="item.referer">{{ formatReferer(item.referer) }}</span>
                <div class="flex-1 bg-gray-200 rounded-full h-4 overflow-hidden">
                  <div class="h-full bg-gradient-to-r from-blue-400 to-blue-600 rounded-full" :style="{ width: getBarWidth(item.count, refererStats.length > 0 ? refererStats[0].count : 0) }"></div>
                </div>
                <span class="text-xs text-gray-600 w-12 text-right">{{ item.count }}</span>
              </div>
            </div>
            <div v-else class="text-center text-gray-400 text-sm py-4">暂无数据</div>
          </div>
        </el-col>
        <!-- 时段分布 -->
        <el-col :xs="24" :md="12" class="mb-4">
          <div class="bg-gray-50 rounded-xl p-4">
            <h4 class="text-sm text-gray-500 font-medium mb-3">24小时访问分布</h4>
            <div v-if="hourStats.length" class="flex items-end gap-1 h-32">
              <div v-for="h in 24" :key="h" class="flex-1 flex flex-col items-center">
                <div class="w-full bg-gradient-to-t from-violet-400 to-violet-600 rounded-t" :style="{ height: getHourHeight(h-1) + 'px' }"></div>
                <span class="text-xs text-gray-400 mt-1" v-if="h % 4 === 1">{{ h-1 }}</span>
              </div>
            </div>
            <div v-else class="text-center text-gray-400 text-sm py-4">暂无数据</div>
          </div>
        </el-col>
        <!-- 设备分布 -->
        <el-col :xs="24" :md="12" class="mb-4">
          <div class="bg-gray-50 rounded-xl p-4">
            <h4 class="text-sm text-gray-500 font-medium mb-3">设备分布</h4>
            <div v-if="deviceStats.length" class="space-y-2">
              <div v-for="item in deviceStats" :key="item.device" class="flex items-center gap-2">
                <span class="text-xs text-gray-500 w-16">{{ item.device }}</span>
                <div class="flex-1 bg-gray-200 rounded-full h-4 overflow-hidden">
                  <div class="h-full bg-gradient-to-r from-emerald-400 to-emerald-600 rounded-full" :style="{ width: getBarWidth(item.count, deviceStats.length > 0 ? deviceStats[0].count : 0) }"></div>
                </div>
                <span class="text-xs text-gray-600 w-12 text-right">{{ item.count }}</span>
              </div>
            </div>
            <div v-else class="text-center text-gray-400 text-sm py-4">暂无数据</div>
          </div>
        </el-col>
        <!-- 浏览器分布 -->
        <el-col :xs="24" :md="12" class="mb-4">
          <div class="bg-gray-50 rounded-xl p-4">
            <h4 class="text-sm text-gray-500 font-medium mb-3">浏览器分布</h4>
            <div v-if="browserStats.length" class="space-y-2">
              <div v-for="item in browserStats" :key="item.browser" class="flex items-center gap-2">
                <span class="text-xs text-gray-500 w-16">{{ item.browser }}</span>
                <div class="flex-1 bg-gray-200 rounded-full h-4 overflow-hidden">
                  <div class="h-full bg-gradient-to-r from-amber-400 to-orange-600 rounded-full" :style="{ width: getBarWidth(item.count, browserStats.length > 0 ? browserStats[0].count : 0) }"></div>
                </div>
                <span class="text-xs text-gray-600 w-12 text-right">{{ item.count }}</span>
              </div>
            </div>
            <div v-else class="text-center text-gray-400 text-sm py-4">暂无数据</div>
          </div>
        </el-col>
        <!-- 地区分布 -->
        <el-col :xs="24" :md="12" class="mb-4">
          <div class="bg-gray-50 rounded-xl p-4">
            <h4 class="text-sm text-gray-500 font-medium mb-3">地区分布 Top10</h4>
            <div v-if="locationStats.length" class="space-y-2">
              <div v-for="item in locationStats" :key="item.location" class="flex items-center gap-2">
                <span class="text-xs text-gray-500 w-24 truncate" :title="item.location">{{ item.location }}</span>
                <div class="flex-1 bg-gray-200 rounded-full h-4 overflow-hidden">
                  <div class="h-full bg-gradient-to-r from-rose-400 to-pink-600 rounded-full" :style="{ width: getBarWidth(item.count, locationStats.length > 0 ? locationStats[0].count : 0) }"></div>
                </div>
                <span class="text-xs text-gray-600 w-12 text-right">{{ item.count }}</span>
              </div>
            </div>
            <div v-else class="text-center text-gray-400 text-sm py-4">暂无数据</div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdvancedStats',
  props: {
    refererStats: { type: Array, default: () => [] },
    hourStats: { type: Array, default: () => [] },
    deviceStats: { type: Array, default: () => [] },
    browserStats: { type: Array, default: () => [] },
    locationStats: { type: Array, default: () => [] }
  },
  methods: {
    formatReferer(referer) {
      if (!referer || referer === 'direct') return '直接访问'
      try {
        const url = new URL(referer)
        return url.hostname
      } catch (e) {
        return referer.substring(0, 20)
      }
    },
    getBarWidth(count, maxCount) {
      if (!maxCount) return '0%'
      return Math.round((count / maxCount) * 100) + '%'
    },
    getHourHeight(hour) {
      const item = this.hourStats.find(h => h.hour === hour)
      if (!item) return 4
      const maxCount = Math.max(...this.hourStats.map(h => h.count))
      if (maxCount === 0) return 4
      return Math.round((item.count / maxCount) * 100) + 4
    }
  }
}
</script>
