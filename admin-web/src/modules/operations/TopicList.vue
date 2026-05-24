<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>场景专题</span>
          <el-button type="primary" @click="dialogVisible = true">新增专题</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="skuCount" label="关联商品" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'" size="small">
              {{ statusMap[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveAt" label="生效时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="success" @click="handlePublish(row)" v-if="row.status === 'DRAFT'">发布</el-button>
            <el-button link type="warning" @click="handleOffline(row)" v-if="row.status === 'PUBLISHED'">下线</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="专题编辑" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="100" />
        </el-form-item>
        <el-form-item label="封面图片">
          <ImageUpload v-model="form.coverUrl" />
        </el-form-item>
        <el-form-item label="内容描述">
          <el-input v-model="form.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="生效时间">
          <el-date-picker v-model="form.effectiveAt" type="datetime" placeholder="留空立即生效" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存草稿</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import http from '@/api/http'

const loading = ref(false)
const list = ref<any[]>([])
const dialogVisible = ref(false)
const statusMap: Record<string, string> = { DRAFT: '草稿', PUBLISHED: '已发布', OFFLINE: '已下线' }

const form = reactive({ id: null as number | null, title: '', coverUrl: '', content: '', effectiveAt: '' })

async function fetchList() {
  loading.value = true
  try {
    const res = await http.get('/configs/topics')
    if (res.data.success) list.value = res.data.data.items || res.data.data
  } finally {
    loading.value = false
  }
}

function handleEdit(row: any) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSave() {
  if (form.id) {
    await http.put(`/configs/topics/${form.id}`, form)
  } else {
    await http.post('/configs/topics', form)
  }
  ElMessage.success('已保存')
  dialogVisible.value = false
  fetchList()
}

async function handlePublish(row: any) {
  await ElMessageBox.confirm(`确认发布专题「${row.title}」？`, '发布确认', { type: 'warning' })
  await http.post(`/configs/topics/${row.id}/publish`)
  ElMessage.success('已发布')
  fetchList()
}

async function handleOffline(row: any) {
  await ElMessageBox.confirm(`确认下线专题「${row.title}」？`, '下线确认')
  await http.post(`/configs/topics/${row.id}/offline`)
  ElMessage.success('已下线')
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
