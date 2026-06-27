<template>
  <div class="min-h-screen bg-gradient-to-br from-gray-50 via-blue-50 to-indigo-100">
    <!-- ============ 登录页 ============ -->
    <div v-if="!loggedIn" class="min-h-screen flex items-center justify-center p-4">
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
          <p class="text-gray-500 text-sm mt-2">后台管理需要登录，用户扫码跳转不受影响</p>
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
              :loading="loginLoading"
              @click="handleLogin"
              class="w-full rounded-xl h-11 text-base font-medium tracking-wide"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- ============ 主界面 ============ -->
    <template v-else>
      <!-- 顶部导航栏 -->
      <header class="bg-white/80 backdrop-blur-md border-b border-gray-200/60 sticky top-0 z-50 shadow-sm">
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
              <el-button size="mini" @click="loadList" class="rounded-lg" icon="el-icon-refresh">刷新</el-button>
              <el-button size="mini" @click="logout" class="rounded-lg" type="danger" plain icon="el-icon-switch-button">退出</el-button>
            </div>
          </div>
        </div>
      </header>

      <!-- 主内容 -->
      <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 animate-fade-in">
        <el-row :gutter="24">
          <!-- ======== 左列：创建/编辑 + 二维码预览 ======== -->
          <el-col :xs="24" :md="9" class="mb-6 md:mb-0">
            <div class="space-y-6">
              <!-- 创建/编辑表单卡片 -->
              <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                <div class="px-6 py-4 border-b border-gray-100">
                  <div class="flex items-center gap-2">
                    <span class="w-1 h-5 bg-gradient-to-b from-blue-500 to-indigo-600 rounded-full"></span>
                    <span class="text-base font-semibold text-gray-800">{{ editingCode ? '编辑二维码' : '创建二维码' }}</span>
                  </div>
                </div>
                <div class="p-6">
                  <el-form label-position="top" :model="form">
                    <el-form-item label="名称">
                      <el-input
                        v-model="form.name"
                        placeholder="例如：公众号6月活动"
                        maxlength="100"
                        show-word-limit
                        class="rounded-lg"
                      />
                    </el-form-item>

                    <el-form-item label="目标链接">
                      <el-input
                        v-model="form.targetUrl"
                        type="textarea"
                        :rows="4"
                        placeholder="https://www.example.com/activity"
                        class="rounded-lg"
                      />
                    </el-form-item>

                    <el-form-item class="mb-0">
                      <div class="flex gap-3">
                        <el-button type="primary" :loading="saving" @click="submitForm" class="rounded-lg flex-1">
                          {{ editingCode ? '保存修改' : '生成二维码' }}
                        </el-button>
                        <el-button v-if="editingCode" @click="cancelEdit" class="rounded-lg">取消</el-button>
                      </div>
                    </el-form-item>
                  </el-form>
                </div>
              </div>

              <!-- 二维码预览卡片 -->
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
                    <el-button @click="copyText(currentQr.trackUrl)" class="rounded-lg flex-1" icon="el-icon-document-copy">复制统计链接</el-button>
                    <el-button type="primary" @click="downloadQr(currentQr)" class="rounded-lg flex-1" icon="el-icon-download">下载二维码</el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-col>

          <!-- ======== 右列：二维码列表 + 统计详情 ======== -->
          <el-col :xs="24" :md="15">
            <div class="space-y-6">
              <!-- 二维码列表卡片 -->
              <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                <div class="px-6 py-4 border-b border-gray-100">
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-2">
                      <span class="w-1 h-5 bg-gradient-to-b from-violet-500 to-purple-600 rounded-full"></span>
                      <span class="text-base font-semibold text-gray-800">二维码列表</span>
                    </div>
                    <el-tag size="small" type="info" effect="plain" class="rounded-lg">
                      共 {{ qrList.length }} 个
                    </el-tag>
                    <el-button size="mini" @click="exportCsv" class="rounded-lg" type="success" plain icon="el-icon-download">导出</el-button>
                  </div>
                </div>
                <div class="p-4">
                  <el-table
                    :data="qrList"
                    v-loading="loading"
                    border
                    size="small"
                    style="width: 100%"
                    :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: 600 }"
                  >
                    <el-table-column prop="name" label="名称" min-width="150" show-overflow-tooltip />
                    <el-table-column prop="code" label="短码" width="90" />
                    <el-table-column prop="pv" label="PV" width="70" align="center" />
                    <el-table-column prop="uv" label="UV" width="70" align="center" />
                    <el-table-column prop="todayPv" label="今日" width="70" align="center">
                      <template slot-scope="scope">
                        <span class="text-blue-600 font-semibold">{{ scope.row.todayPv }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column label="状态" width="80" align="center">
                      <template slot-scope="scope">
                        <el-tag :type="scope.row.enabled ? 'success' : 'info'" size="mini" effect="light" class="rounded-md">
                          {{ scope.row.enabled ? '启用' : '停用' }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="290" fixed="right">
                      <template slot-scope="scope">
                        <div class="flex gap-1.5">
                          <el-button size="mini" @click="selectQr(scope.row)" class="rounded-md">查看</el-button>
                          <el-button size="mini" @click="startEdit(scope.row)" class="rounded-md">编辑</el-button>
                          <el-button size="mini" type="primary" @click="openStats(scope.row)" class="rounded-md">统计</el-button>
                          <el-button
                            size="mini"
                            :type="scope.row.enabled ? 'warning' : 'success'"
                            @click="toggleEnabled(scope.row)"
                            class="rounded-md"
                          >
                            {{ scope.row.enabled ? '停用' : '启用' }}
                          </el-button>
                        </div>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </div>

              <!-- 统计详情卡片 -->
              <div v-if="stats" class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden animate-slide-up">
                <div class="px-6 py-4 border-b border-gray-100">
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-2">
                      <span class="w-1 h-5 bg-gradient-to-b from-amber-500 to-orange-600 rounded-full"></span>
                      <span class="text-base font-semibold text-gray-800">统计详情：{{ stats.qrCode.name }}</span>
                    </div>
                    <el-button size="mini" @click="openStats(stats.qrCode)" class="rounded-lg" icon="el-icon-refresh">刷新</el-button>
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
                          <span class="text-sm text-gray-400 font-medium">大致访客 UV</span>
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
                          <span class="text-sm text-gray-400 font-medium">今日 PV</span>
                          <span class="text-2xl">📊</span>
                        </div>
                        <div class="text-3xl font-bold text-blue-600">{{ stats.todayPv }}</div>
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
                    <el-table-column prop="device" label="设备" width="95" />
                    <el-table-column prop="browser" label="浏览器" width="120" />
                    <el-table-column prop="referer" label="来源" min-width="160" show-overflow-tooltip />
                    <el-table-column prop="userAgent" label="User-Agent" min-width="220" show-overflow-tooltip />
                  </el-table>
                    </el-col>
                  </el-row>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </main>
    </template>
  </div>
</template>

<script>
import {
  clearAuth,
  createQr,
  getStats,
  getToken,
  getUsername,
  listQr,
  login,
  logoutRequest,
  saveAuth,
  updateQr,
  updateQrEnabled
} from './api/qrcode'

import TrendChart from './components/TrendChart.vue'
import BrowserChart from './components/BrowserChart.vue'

export default {
  name: 'App',
  components: {
    TrendChart,
    BrowserChart,
  },
  data() {
    return {
      loggedIn: !!getToken(),
      username: getUsername(),
      loginLoading: false,
      loginForm: {
        username: 'admin',
        password: ''
      },
      form: {
        name: '',
        targetUrl: ''
      },
      editingCode: '',
      saving: false,
      loading: false,
      qrList: [],
      currentQr: null,
      stats: null
    }
  },
  mounted() {
    window.addEventListener('qr-auth-expired', this.handleAuthExpired)
    if (this.loggedIn) {
      this.loadList()
    }
  },
  beforeDestroy() {
    window.removeEventListener('qr-auth-expired', this.handleAuthExpired)
  },
  methods: {
    async handleLogin() {
      if (!this.loginForm.username.trim()) {
        this.$message.warning('请输入用户名')
        return
      }
      if (!this.loginForm.password) {
        this.$message.warning('请输入密码')
        return
      }

      this.loginLoading = true
      try {
        const auth = await login(this.loginForm)
        saveAuth(auth.token, auth.username)
        this.loggedIn = true
        this.username = auth.username
        this.$message.success('登录成功')
        await this.loadList()
      } catch (e) {
        this.$message.error(e.message)
      } finally {
        this.loginLoading = false
      }
    },

    async logout() {
      try {
        await logoutRequest()
      } catch (e) {
        // 即使服务端退出失败，也清理本地登录状态。
      }
      clearAuth()
      this.loggedIn = false
      this.username = ''
      this.qrList = []
      this.currentQr = null
      this.stats = null
      this.cancelEdit()
      this.$message.success('已退出登录')
    },

    handleAuthExpired() {
      this.loggedIn = false
      this.username = ''
      this.qrList = []
      this.currentQr = null
      this.stats = null
      this.$message.warning('登录已失效，请重新登录')
    },

    async submitForm() {
      if (!this.form.name.trim()) {
        this.$message.warning('请输入二维码名称')
        return
      }
      if (!this.form.targetUrl.trim()) {
        this.$message.warning('请输入目标链接')
        return
      }

      this.saving = true
      try {
        let qr
        if (this.editingCode) {
          qr = await updateQr(this.editingCode, this.form)
          this.$message.success('修改成功')
        } else {
          qr = await createQr(this.form)
          this.$message.success('生成成功')
        }
        this.currentQr = qr
        this.cancelEdit()
        await this.loadList()
      } catch (e) {
        this.$message.error(e.message)
      } finally {
        this.saving = false
      }
    },

    async loadList() {
      this.loading = true
      try {
        this.qrList = await listQr()
      } catch (e) {
        this.$message.error(e.message)
      } finally {
        this.loading = false
      }
    },

    selectQr(row) {
      this.currentQr = row
    },

    startEdit(row) {
      this.editingCode = row.code
      this.form.name = row.name
      this.form.targetUrl = row.targetUrl
      this.currentQr = row
    },

    cancelEdit() {
      this.editingCode = ''
      this.form.name = ''
      this.form.targetUrl = ''
    },

    async toggleEnabled(row) {
      try {
        const updated = await updateQrEnabled(row.code, !row.enabled)
        this.$message.success(updated.enabled ? '已启用' : '已停用')
        await this.loadList()
        this.currentQr = updated
      } catch (e) {
        this.$message.error(e.message)
      }
    },

    async openStats(row) {
      try {
        this.stats = await getStats(row.code)
        this.currentQr = this.stats.qrCode
      } catch (e) {
        this.$message.error(e.message)
      }
    },

    copyText(text) {
      if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
          this.$message.success('已复制')
        })
      } else {
        const input = document.createElement('input')
        input.value = text
        document.body.appendChild(input)
        input.select()
        document.execCommand('copy')
        document.body.removeChild(input)
        this.$message.success('已复制')
      }
    },

    downloadQr(qr) {
      const imageUrl = qr.qrImageUrl
      const filename = `${qr.name || qr.code}.png`
      // 用 fetch 获取图片 Blob 后触发下载（绕过跨域限制）
      fetch(imageUrl, { credentials: 'include' })
        .then(res => res.blob())
        .then(blob => {
          const url = URL.createObjectURL(blob)
          const a = document.createElement('a')
          a.href = url
          a.download = filename
          document.body.appendChild(a)
          a.click()
          document.body.removeChild(a)
          URL.revokeObjectURL(url)
        })
        .catch(() => {
          // fallback：直接打开图片
          window.open(imageUrl, '_blank')
        })
    },
    
    exportCsv() {
      if (!this.qrList.length) {
        this.$message.warning('没有数据可导出')
        return
      }
      const headers = ['名称', '短码', 'PV', 'UV', '今日PV', '状态']
      const rows = this.qrList.map(r => [
        r.name,
        r.code,
        r.pv,
        r.uv,
        r.todayPv,
        r.enabled ? '启用' : '停用'
      ])
      const csv = [headers.join(','), ...rows.map(r => r.join(','))].join('\n')
      // 处理中文编码
      const BOM = '\uFEFF'
      const blob = new Blob([BOM + csv], { type: 'text/csv;charset=utf-8;' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `二维码列表_${new Date().toISOString().slice(0,10)}.csv`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
      this.$message.success('导出成功')
    },
  }
}
</script>

<style>
/* Element UI 圆角美化 */
.el-input__inner,
.el-textarea__inner {
  border-radius: 10px !important;
}
.el-input__inner:focus,
.el-textarea__inner:focus {
  border-color: #2563eb !important;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1) !important;
}
.el-button {
  border-radius: 10px !important;
}
.el-button--mini {
  border-radius: 8px !important;
}
.el-message {
  border-radius: 12px !important;
}
.el-loading-mask {
  border-radius: 12px !important;
}

/* 过渡动画 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter, .fade-leave-to {
  opacity: 0;
}
</style>
