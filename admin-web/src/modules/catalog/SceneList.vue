<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>场景组合</span>
          <el-button type="primary" @click="dialogVisible = true">新增场景</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="场景名称" min-width="160" />
        <el-table-column prop="skuCount" label="关联 SKU" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.status === 'ACTIVE' ? '生效' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑场景' : '新增场景'" width="500px">
      <el-form :model="sceneForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="sceneForm.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="sceneForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'

const loading = ref(false)
const list = ref<any[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const sceneForm = reactive({ name: '', description: '' })

async function fetchList() {
  loading.value = true
  try {
    const res = await http.get('/scenes')
    if (res.data.success) list.value = res.data.data.items || res.data.data
  } finally {
    loading.value = false
  }
}

function handleEdit(row: any) {
  editingId.value = row.id
  Object.assign(sceneForm, { name: row.name, description: row.description })
  dialogVisible.value = true
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确认删除场景「${row.name}」？`, '确认')
  await http.delete(`/scenes/${row.id}`)
  ElMessage.success('已删除')
  fetchList()
}

async function handleSave() {
  if (editingId.value) {
    await http.put(`/scenes/${editingId.value}`, sceneForm)
  } else {
    await http.post('/scenes', sceneForm)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  editingId.value = null
  Object.assign(sceneForm, { name: '', description: '' })
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
