<template>
  <div class="app">
    <div class="header">
      <div>
        <h1>二维码访问统计</h1>
        <p>创建统计二维码，扫码后记录访问并自动跳转到目标网站。</p>
      </div>
      <el-button type="primary" @click="loadList">刷新列表</el-button>
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
  </div>
</template>

<script>
import { createQr, updateQr, updateQrEnabled, listQr, getStats } from './api/qrcode'

export default {
  name: 'App',
  data() {
    return {
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
    this.loadList()
  },
  methods: {
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
