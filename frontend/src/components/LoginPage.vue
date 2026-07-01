<template>
  <div class="min-h-screen flex items-center justify-center p-4">
    <!-- 背景装饰圆 -->
    <div class="fixed inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-40 -right-40 w-96 h-96 bg-blue-200 rounded-full mix-blend-multiply filter blur-3xl opacity-30"></div>
      <div class="absolute -bottom-40 -left-40 w-96 h-96 bg-indigo-200 rounded-full mix-blend-multiply filter blur-3xl opacity-30"></div>
    </div>

    <div class="glass rounded-2xl shadow-xl w-full max-w-md p-8 animate-fade-in relative">
      <!-- Logo / 标题 -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-2xl shadow-lg mb-4">
          <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z" />
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-gray-800">二维码访问统计</h1>
        <p class="text-gray-500 text-sm mt-2">华军软件园内部统计工具，后台管理需要登录</p>
      </div>

      <!-- 登录表单 -->
      <el-form ref="loginForm" :model="loginForm" @keyup.enter.native="handleLogin" label-position="top">
        <el-form-item label="用户名">
          <el-input
            v-model="loginForm.username"
            placeholder="admin"
            autocomplete="username"
            prefix-icon="el-icon-user"
            class="rounded-lg"
          />
        </el-form-item>

        <el-form-item label="密码">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            show-password
            prefix-icon="el-icon-lock"
            class="rounded-lg"
          />
        </el-form-item>

        <el-form-item class="mb-0">
          <el-button
            type="primary"
            :loading="loading"
            @click="handleLogin"
            class="w-full rounded-xl h-11 text-base font-medium tracking-wide"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- API 服务状态 -->
      <div class="mt-4 pt-4 border-t border-gray-100">
        <div class="flex items-center justify-center gap-2 text-sm">
          <span class="inline-flex items-center gap-1.5">
            <span
              class="w-2 h-2 rounded-full"
              :class="{
                'bg-green-500 animate-pulse': apiStatus === 'up',
                'bg-red-500': apiStatus === 'down',
                'bg-gray-400 animate-pulse': apiStatus === 'checking'
              }"
            ></span>
            <span class="text-gray-500">API 服务状态：</span>
            <span
              :class="{
                'text-green-600 font-medium': apiStatus === 'up',
                'text-red-600 font-medium': apiStatus === 'down',
                'text-gray-400': apiStatus === 'checking'
              }"
            >
              {{ apiStatus === 'up' ? '正常运行' : apiStatus === 'down' ? '服务不可用' : '检测中...' }}
            </span>
          </span>
        </div>
      </div>

      <!-- 版权信息 -->
      <div class="mt-6 text-center text-xs text-gray-400">
        Copyright©2026 会火AI. All Rights Reserved
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LoginPage',
  props: {
    apiStatus: { type: String, default: 'checking' },
    loading: { type: Boolean, default: false }
  },
  data() {
    return {
      loginForm: {
        username: 'admin',
        password: ''
      }
    }
  },
  methods: {
    handleLogin() {
      if (!this.loginForm.username.trim()) {
        this.$message.warning('请输入用户名')
        return
      }
      if (!this.loginForm.password) {
        this.$message.warning('请输入密码')
        return
      }
      this.$emit('login', { username: this.loginForm.username, password: this.loginForm.password })
    }
  }
}
</script>
