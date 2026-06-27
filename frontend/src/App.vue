<template>
  <div class="app">
    <div v-if="!loggedIn" class="login-page">
      <el-card class="login-card" shadow="never">
        <div class="login-title">
          <h1>二维码访问统计</h1>
          <p>后台管理需要登录；用户扫码跳转不受影响。</p>
        </div>

        <el-form label-width="70px" :model="loginForm" @keyup.enter.native="handleLogin">
          <el-form-item label="用户名">
            <el-input v-model="loginForm.username" placeholder="admin" autocomplete="username" />
          </el-form-item>

          <el-form-item label="密码">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              autocomplete="current-password"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="loginLoading" @click="handleLogin">登录</el-button>
          </el-form-item>
        </el-form>

        <div class="login-tip">
          默认账号：admin / ChangeMe123!，生产环境请在后端配置或环境变量中修改。
        </div>
      </el-card>
    </div>

    <template v-else>
      <div class="header">
        <div>
          <h1>二维码访问统计</h1>
          <p>创建统计二维码，扫码后记录访问并自动跳转到目标网站。</p>
        </div>
        <div class="header-actions">
          <span class="muted">当前用户：{{ username || 'admin' }}</span>
          <el-button type="primary" @click="loadList">刷新列表</el-button>
          <el-button @click="logout">退出登录</el-button>
        </div>
      </div>

      <el-row :gutter="20">
        <el-col :xs="24" :md="9">
          <el-card shadow="never">
            <div slot="header">
              <span>{{ editingCode ? '编辑二维码' : '创建二维码' }}</span>
            </div>

            <el-form label-width="90px" :model="form">
              <el-form-item label="名称">
                <el-input v-model="form.name" placeholder="例如：公众号6月活动" maxlength="100" show-word-limit />
              </el-form-item>

              <el-form-item label="目标链接">
                <el-input v-model="form.targetUrl" type="textarea" :rows="4" placeholder="https://www.example.com/activity" />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" :loading="saving" @click="submitForm">
                  {{ editingCode ? '保存修改' : '生成二维码' }}
                </el-button>
                <el-button v-if="editingCode" @click="cancelEdit">取消</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <el-card v-if="currentQr" class="mt20" shadow="never">
            <div slot="header">
              <span>二维码</span>
            </div>

            <div class="qr-preview">
              <img :src="currentQr.qrImageUrl" alt="二维码" />
            </div>

            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="短码">{{ currentQr.code }}</el-descriptions-item>
              <el-descriptions-item label="统计链接">
                <el-link :href="currentQr.trackUrl" target="_blank" type="primary">{{ currentQr.trackUrl }}</el-link>
              </el-descriptions-item>
              <el-descriptions-item label="目标链接">
                <el-link :href="currentQr.targetUrl" target="_blank" type="primary">{{ currentQr.targetUrl }}</el-link>
              </el-descriptions-item>
            </el-descriptions>

            <div class="actions">
              <el-button @click="copyText(currentQr.trackUrl)">复制统计链接</el-button>
              <el-button type="primary" @click="downloadQr(currentQr)">下载二维码</el-button>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="15">
          <el-card shadow="never">
            <div slot="header" class="card-header">
              <span>二维码列表</span>
              <span class="muted">共 {{ qrList.length }} 个</span>
            </div>

            <el-table :data="qrList" v-loading="loading" border size="small" style="width: 100%">
              <el-table-column prop="name" label="名称" min-width="150" show-overflow-tooltip />
              <el-table-column prop="code" label="短码" width="100" />
              <el-table-column prop="pv" label="PV" width="80" />
              <el-table-column prop="uv" label="UV" width="80" />
              <el-table-column prop="todayPv" label="今日" width="80" />
              <el-table-column label="状态" width="90">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.enabled ? 'success' : 'info'" size="mini">
                    {{ scope.row.enabled ? '启用' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="310" fixed="right">
                <template slot-scope="scope">
                  <el-button size="mini" @click="selectQr(scope.row)">查看</el-button>
                  <el-button size="mini" @click="startEdit(scope.row)">编辑</el-button>
                  <el-button size="mini" type="primary" @click="openStats(scope.row)">统计</el-button>
                  <el-button
                    size="mini"
                    :type="scope.row.enabled ? 'warning' : 'success'"
                    @click="toggleEnabled(scope.row)"
                  >
                    {{ scope.row.enabled ? '停用' : '启用' }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>

          <el-card v-if="stats" class="mt20" shadow="never">
            <div slot="header" class="card-header">
              <span>统计详情：{{ stats.qrCode.name }}</span>
              <el-button size="mini" @click="openStats(stats.qrCode)">刷新统计</el-button>
            </div>

            <el-row :gutter="12">
              <el-col :span="8">
                <div class="stat-box">
                  <div class="stat-value">{{ stats.pv }}</div>
                  <div class="stat-label">总访问 PV</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-box">
                  <div class="stat-value">{{ stats.uv }}</div>
                  <div class="stat-label">大致访客 UV</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-box">
                  <div class="stat-value">{{ stats.todayPv }}</div>
                  <div class="stat-label">今日 PV</div>
                </div>
              </el-col>
            </el-row>

            <h3>最近 7 天</h3>
            <el-table :data="stats.last7Days" border size="small">
              <el-table-column prop="date" label="日期" />
              <el-table-column prop="pv" label="PV" />
              <el-table-column prop="uv" label="UV" />
            </el-table>

            <h3>最近访问记录</h3>
            <el-table :data="stats.latestVisits" border size="small">
              <el-table-column prop="createdAt" label="访问时间" width="170" />
              <el-table-column prop="ip" label="IP" width="140" />
              <el-table-column prop="device" label="设备" width="100" />
              <el-table-column prop="browser" label="浏览器" width="130" />
              <el-table-column prop="referer" label="来源" min-width="180" show-overflow-tooltip />
              <el-table-column prop="userAgent" label="User-Agent" min-width="260" show-overflow-tooltip />
            </el-table>
          </el-card>
        </el-col>
      </el-row>
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

export default {
  name: 'App',
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
      const a = document.createElement('a')
      a.href = qr.qrImageUrl
      a.download = `${qr.name || qr.code}.png`
      a.target = '_blank'
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
    }
  }
}
</script>

<style>
body {
  margin: 0;
  background: #f5f7fa;
  font-family: Helvetica Neue, Arial, PingFang SC, Microsoft YaHei, sans-serif;
}

.app {
  padding: 24px;
}

.login-page {
  min-height: calc(100vh - 48px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 430px;
}

.login-title {
  text-align: center;
  margin-bottom: 22px;
}

.login-title h1 {
  margin: 0 0 8px;
  font-size: 26px;
}

.login-title p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.login-tip {
  margin-top: 12px;
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.header h1 {
  margin: 0 0 8px;
  font-size: 28px;
}

.header p {
  margin: 0;
  color: #606266;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mt20 {
  margin-top: 20px;
}

.qr-preview {
  text-align: center;
  margin-bottom: 18px;
}

.qr-preview img {
  width: 220px;
  height: 220px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.actions {
  margin-top: 16px;
  display: flex;
  gap: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.muted {
  color: #909399;
  font-size: 13px;
}

.stat-box {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 20px;
  text-align: center;
  margin-bottom: 20px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  line-height: 1.2;
}

.stat-label {
  margin-top: 8px;
  color: #606266;
}
</style>
