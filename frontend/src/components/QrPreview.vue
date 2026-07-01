<template>
  <div v-if="currentQr" class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden animate-slide-up">
    <div class="px-6 py-4 border-b border-gray-100">
      <div class="flex items-center gap-2">
        <span class="w-1 h-5 bg-gradient-to-b from-emerald-500 to-teal-600 rounded-full"></span>
        <span class="text-base font-semibold text-gray-800">二维码预览</span>
      </div>
    </div>
    <div class="p-6">
      <div class="flex justify-center mb-4">
        <div class="p-3 bg-gray-50 rounded-xl">
          <img :src="currentQr.qrImageUrl" alt="二维码" class="w-48 h-48" />
        </div>
      </div>

      <div class="bg-gray-50 rounded-xl p-4 space-y-3 text-sm">
        <div class="flex items-start gap-2">
          <span class="text-gray-400 font-medium w-12 shrink-0">短码</span>
          <span class="text-gray-700 font-mono">{{ currentQr.code }}</span>
        </div>
        <div class="flex items-start gap-2">
          <span class="text-gray-400 font-medium w-12 shrink-0">统计</span>
          <a :href="currentQr.trackUrl" target="_blank" class="text-blue-600 hover:text-blue-700 truncate">{{ currentQr.trackUrl }}</a>
        </div>
        <div class="flex items-start gap-2">
          <span class="text-gray-400 font-medium w-12 shrink-0">目标</span>
          <a :href="currentQr.targetUrl" target="_blank" class="text-blue-600 hover:text-blue-700 truncate">{{ currentQr.targetUrl }}</a>
        </div>
      </div>

      <div class="flex gap-3 mt-4">
        <el-button @click="$emit('copy', currentQr.trackUrl)" class="rounded-lg flex-1" icon="el-icon-document-copy">复制统计链接</el-button>
        <el-button type="primary" @click="$emit('download', currentQr)" class="rounded-lg flex-1" icon="el-icon-download">下载二维码</el-button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'QrPreview',
  props: {
    currentQr: { type: Object, default: null }
  }
}
</script>
