<template>
  <div class="bg-white rounded-xl shadow-sm border border-gray-100">
    <div class="px-6 py-4 border-b border-gray-100">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <span class="w-1 h-5 bg-gradient-to-b from-violet-500 to-purple-600 rounded-full"></span>
          <span class="text-base font-semibold text-gray-800">二维码列表</span>
        </div>
        <el-tag size="small" type="info" effect="plain" class="rounded-lg">
          共 {{ qrList.length }} 个
        </el-tag>
        <el-button size="mini" @click="$emit('export-csv')" class="rounded-lg" type="success" plain icon="el-icon-download">导出</el-button>
        <el-button
          v-if="selectedCodes.length > 0"
          size="mini"
          type="danger"
          @click="handleBatchDelete"
          class="rounded-lg"
          icon="el-icon-delete"
        >
          批量删除 ({{ selectedCodes.length }})
        </el-button>
      </div>
    </div>
    <div class="px-4 pb-4">
      <!-- 搜索栏 -->
      <div class="flex gap-2 mb-4">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索二维码名称..."
          prefix-icon="el-icon-search"
          size="small"
          class="flex-1"
          clearable
          @keyup.enter.native="handleSearch"
          @clear="resetSearch"
        />
        <el-select
          v-model="searchEnabled"
          placeholder="状态"
          size="small"
          style="width: 100px"
          clearable
          @change="handleSearch"
        >
          <el-option label="启用" :value="true" />
          <el-option label="停用" :value="false" />
        </el-select>
        <el-button size="small" type="primary" @click="handleSearch" icon="el-icon-search">搜索</el-button>
        <el-button size="small" @click="resetSearch" icon="el-icon-refresh">重置</el-button>
      </div>
    </div>
    <div class="px-4 pb-4">
      <el-table
        :data="qrList"
        v-loading="loading"
        border
        size="small"
        style="width: 100%"
        :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: 600 }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="42" />
        <el-table-column prop="name" label="名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="短码" width="90" />
        <el-table-column prop="pv" label="PV" width="60" align="center" />
        <el-table-column prop="uv" label="UV" width="60" align="center" />
        <el-table-column prop="todayPv" label="今日PV" width="65" align="center">
          <template slot-scope="scope">
            <span class="text-blue-600 font-semibold">{{ scope.row.todayPv }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="todayUv" label="今日UV" width="65" align="center">
          <template slot-scope="scope">
            <span class="text-violet-600 font-semibold">{{ scope.row.todayUv }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="70" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'info'" size="mini" effect="light" class="rounded-md">
              {{ scope.row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="210" align="left">
          <template slot-scope="scope">
            <div class="ops-row">
              <button class="op-btn op-see" @click="$emit('select', scope.row)" title="查看详情">
                <i class="el-icon-view"></i>
              </button>
              <button class="op-btn op-stats" @click="$emit('open-stats', scope.row)" title="统计数据">
                <i class="el-icon-data-analysis"></i>
              </button>
              <button class="op-btn op-dashboard" @click="$emit('open-dashboard', scope.row)" title="数据大屏">
                <i class="el-icon-monitor"></i>
              </button>
              <el-dropdown trigger="click" @command="cmd => handleCommand(cmd, scope.row)" size="small">
                <button class="op-btn op-more" title="更多操作">
                  <i class="el-icon-more"></i>
                </button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item icon="el-icon-edit" command="edit">编辑</el-dropdown-item>
                  <el-dropdown-item icon="el-icon-switch-button" :command="scope.row.enabled ? 'disable' : 'enable'">
                    {{ scope.row.enabled ? '停用' : '启用' }}
                  </el-dropdown-item>
                  <el-dropdown-item icon="el-icon-delete" command="delete" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
export default {
  name: 'QrTable',
  props: {
    qrList: { type: Array, default: () => [] },
    loading: { type: Boolean, default: false }
  },
  data() {
    return {
      searchKeyword: '',
      searchEnabled: null,
      selectedCodes: []
    }
  },
  watch: {
    qrList() {
      this.selectedCodes = []
    }
  },
  methods: {
    handleSearch() {
      this.$emit('search', this.searchKeyword, this.searchEnabled)
    },
    resetSearch() {
      this.searchKeyword = ''
      this.searchEnabled = null
      this.$emit('reset-search')
    },
    handleSelectionChange(selection) {
      this.selectedCodes = selection.map(row => row.code)
    },
    handleCommand(cmd, row) {
      if (cmd === 'edit') this.$emit('edit', row)
      else if (cmd === 'delete') this.$emit('delete', row)
      else if (cmd === 'enable' || cmd === 'disable') this.$emit('toggle', row)
    },
    async handleBatchDelete() {
      if (this.selectedCodes.length === 0) {
        this.$message.warning('请先选择要删除的二维码')
        return
      }
      try {
        await this.$confirm(
          `确定删除选中的 ${this.selectedCodes.length} 个二维码吗？删除后将无法恢复。`,
          '批量删除',
          { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
        )
        this.$emit('batch-delete', [...this.selectedCodes])
      } catch (e) {
        // 用户取消
      }
    }
  }
}
</script>

<style scoped>
.ops-row {
  display: flex;
  align-items: center;
  gap: 6px;
}
.op-btn {
  width: 30px;
  height: 30px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  color: #6b7280;
  cursor: pointer;
  transition: all .15s;
  padding: 0;
  font-size: 13px;
}
.op-btn:hover { border-color: #d1d5db; background: #f9fafb; }
.op-see:hover { color: #2563eb; border-color: #bfdbfe; background: #eff6ff; }
.op-stats:hover { color: #7c3aed; border-color: #ddd6fe; background: #f5f3ff; }
.op-dashboard:hover { color: #059669; border-color: #a7f3d0; background: #ecfdf5; }
.op-more { color: #9ca3af; }
.op-more:hover { color: #374151; border-color: #9ca3af; background: #f3f4f6; }
</style>
