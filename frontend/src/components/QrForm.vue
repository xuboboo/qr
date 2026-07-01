<template>
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
            <el-button type="primary" :loading="saving" @click="$emit('submit')" class="rounded-lg flex-1">
              {{ editingCode ? '保存修改' : '生成二维码' }}
            </el-button>
            <el-button v-if="editingCode" @click="$emit('cancel')" class="rounded-lg">取消</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'QrForm',
  props: {
    form: { type: Object, required: true },
    editingCode: { type: String, default: '' },
    saving: { type: Boolean, default: false }
  }
}
</script>
