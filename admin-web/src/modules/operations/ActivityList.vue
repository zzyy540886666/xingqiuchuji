<template>
  <el-card>
    <template #header>
      <div class="header">
        <span>活动详情管理</span>
        <el-button type="primary" @click="create">新增活动</el-button>
      </div>
    </template>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="title" label="活动标题" min-width="200" />
      <el-table-column prop="tag" label="标签" width="120" />
      <el-table-column prop="startAt" label="开始时间" width="180" />
      <el-table-column prop="endAt" label="结束时间" width="180" />
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }"><el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'">{{ row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="160"><template #default="{ row }"><el-button link type="primary" @click="edit(row)">编辑</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="visible" title="活动详情编辑" width="820px">
    <el-form :model="form" label-width="100px">
      <div class="grid">
        <el-form-item label="活动标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="活动标签"><el-input v-model="form.tag" /></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startAt" value-format="YYYY-MM-DDTHH:mm:ss" type="datetime" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.endAt" value-format="YYYY-MM-DDTHH:mm:ss" type="datetime" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="form.status"><el-option label="草稿" value="DRAFT" /><el-option label="发布" value="PUBLISHED" /><el-option label="下线" value="OFFLINE" /></el-select></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </div>
      <el-form-item label="副标题"><el-input v-model="form.subtitle" /></el-form-item>
      <el-form-item label="封面图"><ImageUpload v-model="form.coverUrl" /></el-form-item>
      <el-form-item label="视频 URL"><el-input v-model="form.videoUrl" /></el-form-item>
      <el-form-item label="跳转地址"><el-input v-model="form.linkUrl" placeholder="/pages/category/index?type=RENT" /></el-form-item>
      <el-form-item label="活动简介"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      <el-divider content-position="left">详情区块</el-divider>
      <div v-for="(section, index) in form.content" :key="index" class="section-row">
        <el-input v-model="section.title" placeholder="区块标题" />
        <el-input v-model="section.content" type="textarea" :rows="2" placeholder="区块内容" />
        <el-button link type="danger" @click="form.content.splice(index, 1)">删除</el-button>
      </div>
      <el-button @click="form.content.push({ title: '', content: '' })">新增区块</el-button>
    </el-form>
    <template #footer><el-button @click="visible = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import http from '@/api/http'

const loading = ref(false)
const visible = ref(false)
const list = ref<any[]>([])
const form = reactive({
  id: 0, title: '', subtitle: '', tag: '', coverUrl: '', videoUrl: '', linkUrl: '', description: '',
  startAt: '', endAt: '', sortOrder: 0, status: 'DRAFT',
  content: [] as Array<{ title: string; content: string }>,
})
async function fetchList() {
  loading.value = true
  try { list.value = (await http.get('/activities')).data.data.items || [] } finally { loading.value = false }
}
function create() {
  Object.assign(form, { id: 0, title: '', subtitle: '', tag: '', coverUrl: '', videoUrl: '', linkUrl: '', description: '', startAt: '', endAt: '', sortOrder: 0, status: 'DRAFT', content: [] })
  visible.value = true
}
function edit(row: any) {
  let content: Array<{ title: string; content: string }> = []
  try { content = JSON.parse(row.contentJson || '[]') } catch { content = [] }
  Object.assign(form, { ...row, content })
  visible.value = true
}
async function save() {
  const payload = { ...form, content: form.content }
  if (form.id) await http.put(`/activities/${form.id}`, payload); else await http.post('/activities', payload)
  ElMessage.success('活动已保存')
  visible.value = false
  fetchList()
}
async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除活动“${row.title}”？`, '删除确认', { type: 'warning' })
  await http.delete(`/activities/${row.id}`)
  ElMessage.success('已删除')
  fetchList()
}
onMounted(fetchList)
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
.grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 0 16px; }
.section-row { display: grid; grid-template-columns: 230px 1fr 60px; gap: 12px; margin-bottom: 12px; }
</style>
