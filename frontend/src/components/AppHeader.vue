<template>
  <header class="bg-white/80 backdrop-blur-xl fixed top-0 left-0 right-0 w-full z-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <div class="flex items-center gap-3">
          <div class="flex items-center justify-center w-10 h-10 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-xl shadow-sm">
            <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z" />
            </svg>
          </div>
          <div>
            <h1 class="text-lg font-bold text-gray-800 leading-tight">二维码访问统计</h1>
            <p class="text-xs text-gray-400">创建统计二维码，扫码后记录访问并自动跳转</p>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <span class="text-sm text-gray-400 hidden sm:inline">
            <span class="inline-flex items-center gap-1">
              <span class="w-2 h-2 bg-green-400 rounded-full"></span>
              {{ username || 'admin' }}
            </span>
          </span>
          <el-button size="mini" @click="$emit('refresh')" class="rounded-lg" icon="el-icon-refresh">刷新</el-button>
          <el-button size="mini" @click="$emit('logout')" class="rounded-lg" type="danger" plain icon="el-icon-switch-button">退出</el-button>
        </div>
      </div>
    </div>
  </header>
</template>

<script>
export default {
  name: 'AppHeader',
  props: {
    username: { type: String, default: '' }
  }
}
</script>
