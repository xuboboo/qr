<template>
  <div class="min-h-screen bg-gradient-to-br from-gray-50 via-blue-50 to-indigo-100">
    <!-- ============ 登录页 ============ -->
    <login-page v-if="!loggedIn" :api-status="apiStatus" :loading="loginLoading" @login="handleLogin" />

    <!-- ============ 主界面 ============ -->
    <template v-else>
      <app-header :username="username" @refresh="loadList" @logout="logout" />
      <div style="height: 64px; background: linear-gradient(to bottom right, #f9fafb, #eff6ff, #e0e7ff);"></div>

      <!-- 主内容 -->
      <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 animate-fade-in">
        <!-- 数据汇总卡片 -->
        <summary-cards :summary="summary" />

        <el-row :gutter="24">
          <!-- ======== 左列：创建/编辑 + 二维码预览 ======== -->
          <el-col :xs="24" :md="9" class="mb-6 md:mb-0">
            <div class="space-y-6">
              <qr-form :form="form" :editing-code="editingCode" :saving="saving" @submit="submitForm" @cancel="cancelEdit" />
              <qr-preview :current-qr="currentQr" @copy="copyText" @download="downloadQr" />
            </div>
          </el-col>

          <!-- ======== 右列：二维码列表 + 统计详情 ======== -->
          <el-col :xs="24" :md="15">
            <div class="space-y-6">
              <qr-table
                :qr-list="qrList"
                :loading="loading"
                @search="handleSearch"
                @reset-search="loadList"
                @select="selectQr"
                @edit="startEdit"
                @open-stats="openStats"
                @open-dashboard="openDashboard"
                @toggle="toggleEnabled"
                @delete="handleDelete"
                @export-csv="exportCsv"
                @batch-delete="handleBatchDelete"
              />
              <stats-detail :stats="stats" @refresh="openStats" />
              <advanced-stats :referer-stats="refererStats" :hour-stats="hourStats" :device-stats="deviceStats" :browser-stats="browserStats" :location-stats="locationStats" />
            </div>
          </el-col>
        </el-row>
      </main>

      <!-- 页脚 -->
      <footer class="bg-white/80 backdrop-blur-md border-t border-gray-200/60 py-6 mt-8">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div class="text-center text-sm text-gray-400">
            Copyright©2026 会火AI. All Rights Reserved
          </div>
        </div>
      </footer>

      <!-- ============ 全屏大屏 ============ -->
      <dashboard-overlay
        :visible="dashboardVisible"
        :qr-info="dashboardQrInfo"
        :stats="dashboardStats"
        :referer-stats="dashboardRefererStats"
        :hour-stats="dashboardHourStats"
        :device-stats="dashboardDeviceStats"
        :browser-stats="dashboardBrowserStats"
        :location-stats="dashboardLocationStats"
        :loading="dashboardLoading"
        @close="dashboardVisible = false"
      />
    </template>
  </div>
</template>

<script>
import {
  batchDeleteQr,
  checkApiStatus,
  clearAuth,
  createQr,
  deleteQr,
  getBrowserStats,
  getDeviceStats,
  getHourStats,
  getLocationStats,
  getRefererStats,
  getStats,
  getSummary,
  getToken,
  getUsername,
  listQr,
  login,
  logoutRequest,
  saveAuth,
  searchQr,
  updateQr,
  updateQrEnabled
} from './api/qrcode'

import LoginPage from './components/LoginPage.vue'
import AppHeader from './components/AppHeader.vue'
import SummaryCards from './components/SummaryCards.vue'
import QrForm from './components/QrForm.vue'
import QrPreview from './components/QrPreview.vue'
import QrTable from './components/QrTable.vue'
import StatsDetail from './components/StatsDetail.vue'
import AdvancedStats from './components/AdvancedStats.vue'
import DashboardOverlay from './components/DashboardOverlay.vue'

export default {
  name: 'App',
  components: {
    LoginPage,
    AppHeader,
    SummaryCards,
    QrForm,
    QrPreview,
    QrTable,
    StatsDetail,
    AdvancedStats,
    DashboardOverlay,
  },
  data() {
    return {
      loggedIn: !!getToken(),
      username: getUsername(),
      loginLoading: false,
      form: {
        name: '',
        targetUrl: ''
      },
      editingCode: '',
      saving: false,
      loading: false,
      qrList: [],
      currentQr: null,
      stats: null,
      apiStatus: 'checking',
      summary: null,
      refererStats: [],
      hourStats: [],
      deviceStats: [],
      browserStats: [],
      locationStats: [],
      // 大屏状态
      dashboardVisible: false,
      dashboardLoading: false,
      dashboardQrInfo: {},
      dashboardStats: {},
      dashboardRefererStats: [],
      dashboardHourStats: [],
      dashboardDeviceStats: [],
      dashboardBrowserStats: [],
      dashboardLocationStats: []
    }
  },
  mounted() {
    window.addEventListener('qr-auth-expired', this.handleAuthExpired)
    this.checkApiStatus()
    if (this.loggedIn) {
      this.loadList()
      this.loadSummary()
      this.loadAdvancedStats()
    }
  },
  beforeDestroy() {
    window.removeEventListener('qr-auth-expired', this.handleAuthExpired)
  },
  methods: {
    async checkApiStatus() {
      try {
        await checkApiStatus()
        this.apiStatus = 'up'
      } catch (e) {
        this.apiStatus = 'down'
      }
    },

    async loadSummary() {
      try {
        this.summary = await getSummary()
      } catch (e) {
        console.error('加载汇总失败', e)
      }
    },

    async loadAdvancedStats() {
      try {
        const [referers, hours, devices, browsers, locations] = await Promise.all([
          getRefererStats(),
          getHourStats(),
          getDeviceStats(),
          getBrowserStats(),
          getLocationStats()
        ])
        this.refererStats = referers
        this.hourStats = hours
        this.deviceStats = devices
        this.browserStats = browsers
        this.locationStats = locations
      } catch (e) {
        console.error('加载统计失败', e)
      }
    },

    async handleSearch(keyword, enabled) {
      this.loading = true
      try {
        this.qrList = await searchQr(keyword, enabled)
      } catch (e) {
        this.$message.error(e.message)
      } finally {
        this.loading = false
      }
    },

    async handleLogin(payload) {
      this.loginLoading = true
      try {
        const auth = await login(payload)
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
        await this.loadSummary()
        this.loadAdvancedStats()
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

    async handleDelete(row) {
      try {
        await this.$confirm(`确定删除二维码「${row.name}」吗？删除后将无法恢复。`, '确认删除', {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await deleteQr(row.code)
        this.$message.success('删除成功')
        if (this.currentQr && this.currentQr.code === row.code) {
          this.currentQr = null
        }
        await this.loadList()
        await this.loadSummary()
        this.loadAdvancedStats()
      } catch (e) {
        if (e !== 'cancel') {
          this.$message.error(e.message)
        }
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

    async openDashboard(row) {
      this.dashboardVisible = true
      this.dashboardLoading = true
      this.dashboardQrInfo = row
      try {
        const [statsData, referers, hours, devices, browsers, locations] = await Promise.all([
          getStats(row.code),
          getRefererStats(row.code),
          getHourStats(row.code),
          getDeviceStats(row.code),
          getBrowserStats(row.code),
          getLocationStats(row.code)
        ])
        this.dashboardStats = statsData
        this.dashboardRefererStats = referers
        this.dashboardHourStats = hours
        this.dashboardDeviceStats = devices
        this.dashboardBrowserStats = browsers
        this.dashboardLocationStats = locations
      } catch (e) {
        this.$message.error('加载大屏数据失败: ' + e.message)
      } finally {
        this.dashboardLoading = false
      }
    },

    async handleBatchDelete(codes) {
      try {
        await batchDeleteQr(codes)
        this.$message.success(`成功删除 ${codes.length} 个二维码`)
        await this.loadList()
        await this.loadSummary()
        this.loadAdvancedStats()
      } catch (e) {
        this.$message.error(e.message)
      }
    },

    copyText(text) {
      if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
          this.$message.success('已复制')
        }).catch(() => {
          this.fallbackCopy(text)
        })
      } else {
        this.fallbackCopy(text)
      }
    },

    fallbackCopy(text) {
      const input = document.createElement('input')
      input.value = text
      document.body.appendChild(input)
      input.select()
      document.execCommand('copy')
      document.body.removeChild(input)
      this.$message.success('已复制')
    },

    downloadQr(qr) {
      const imageUrl = qr.qrImageUrl
      const filename = `${qr.name || qr.code}.png`
      // 用 fetch 获取图片 Blob 后触发下载（绕过跨域限制）
      fetch(imageUrl)
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
      const escapeCsvField = (val) => {
        const str = String(val == null ? '' : val)
        if (str.includes(',') || str.includes('"') || str.includes('\n')) {
          return '"' + str.replace(/"/g, '""') + '"'
        }
        return str
      }
      const headers = ['名称', '短码', 'PV', 'UV', '今日PV', '今日UV', '状态']
      const rows = this.qrList.map(r => [
        escapeCsvField(r.name),
        r.code,
        r.pv,
        r.uv,
        r.todayPv,
        r.todayUv,
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
    }
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
