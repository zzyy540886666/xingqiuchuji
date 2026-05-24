<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>运营位管理</span>
          <el-button type="primary" @click="dialogVisible = true">新增运营位</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="position" label="位置" width="120" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
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

    <el-dialog v-model="dialogVisible" title="运营位编辑" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="位置">
          <el-select v-model="form.position">
            <el-option label="首页 Banner" value="HOME_BANNER" />
            <el-option label="首页卡片" value="HOME_CARD" />
            <el-option label="星球卡入口" value="PLANET_CARD" />
            <el-option label="推荐位" value="RECOMMEND" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="50" />
        </el-form-item>
        <el-form-item label="图片">
          <ImageUpload v-model="form.imageUrl" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="form.linkUrl" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
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
const form = reactive({ id: null as number | null, position: 'HOME_BANNER', title: '', imageUrl: '', linkUrl: '', sortOrder: 0 })

async function fetchList() {
  loading.value = true
  try {
    const res = await http.get('/configs/banners')
    if (res.data.success) list.value = res.data.data.items || res.data.data
  } finally {
    loading.value = false
  }
}

function handleEdit(row: any) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确认删除「${row.title}」？`, '确认')
  await http.delete(`/configs/banners/${row.id}`)
  ElMessage.success('已删除')
  fetchList()
}

async function handleSave() {
  if (form.id) {
    await http.put(`/configs/banners/${form.id}`, form)
  } else {
    await http.post('/configs/banners', form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
