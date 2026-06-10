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

  <el-dialog v-model="visible" title="活动详情编辑" width="1120px" class="preview-dialog">
    <div class="dialog-editor">
      <el-form :model="form" label-width="100px" class="dialog-form">
        <div class="grid">
          <el-form-item label="活动标题"><el-input v-model="form.title" /></el-form-item>
          <el-form-item label="活动标签"><el-input v-model="form.tag" /></el-form-item>
          <el-form-item label="开始时间"><el-date-picker v-model="form.startAt" value-format="YYYY-MM-DDTHH:mm:ss" type="datetime" /></el-form-item>
          <el-form-item label="结束时间"><el-date-picker v-model="form.endAt" value-format="YYYY-MM-DDTHH:mm:ss" type="datetime" /></el-form-item>
          <el-form-item label="状态"><el-select v-model="form.status"><el-option label="草稿" value="DRAFT" /><el-option label="发布" value="PUBLISHED" /><el-option label="下线" value="OFFLINE" /></el-select></el-form-item>
          <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        </div>
        <el-form-item label="副标题"><el-input v-model="form.subtitle" /></el-form-item>
        <el-form-item label="首页封面图">
          <ImageUpload v-model="form.coverUrl" />
          <div class="field-tip">显示在小程序首页顶部活动图，不作为活动详情正文图片。</div>
        </el-form-item>
        <el-form-item label="详情首图">
          <ImageUpload v-model="form.detailImageUrl" />
          <div class="field-tip">显示在活动详情页正文顶部。</div>
        </el-form-item>
        <el-form-item label="活动视频">
          <VideoUpload v-model="form.videoUrl" />
          <el-input v-model="form.videoUrl" class="url-input" placeholder="也可粘贴视频 URL，例如 https://example.com/video.mp4" />
          <div class="field-tip">视频 URL 建议使用 HTTPS 直链；微信端优先使用 MP4/H.264，本地上传会自动生成 /uploads/videos 路径。</div>
        </el-form-item>
        <el-form-item label="立即参与去向">
          <LinkTargetPicker v-model="form.linkUrl" />
          <div class="field-tip">活动详情页底部“立即参与”按钮点击后打开的页面。</div>
        </el-form-item>
        <el-form-item label="活动简介"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-divider content-position="left">详情区块</el-divider>
        <div v-for="(section, index) in form.content" :key="index" class="section-row">
          <el-input v-model="section.title" placeholder="区块标题" />
          <el-input v-model="section.content" type="textarea" :rows="2" placeholder="区块内容" />
          <ImageUpload v-model="section.imageUrl" />
          <el-button link type="danger" @click="form.content.splice(index, 1)">删除</el-button>
        </div>
        <el-button @click="form.content.push({ title: '', content: '', imageUrl: '' })">新增区块</el-button>
      </el-form>
      <MiniAppPreview mode="activity" :data="activityPreview" />
    </div>
    <template #footer><el-button @click="visible = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import LinkTargetPicker from '@/components/LinkTargetPicker.vue'
import MiniAppPreview from '@/components/MiniAppPreview.vue'
import VideoUpload from '@/components/VideoUpload.vue'
import http from '@/api/http'

const loading = ref(false)
const visible = ref(false)
const list = ref<any[]>([])
type ActivitySection = { title: string; content: string; imageUrl?: string; kind?: string }
const form = reactive({
  id: 0, title: '', subtitle: '', tag: '', coverUrl: '', detailImageUrl: '', videoUrl: '', linkUrl: '', description: '',
  startAt: '', endAt: '', sortOrder: 0, status: 'DRAFT',
  content: [] as ActivitySection[],
})
const activityPreview = computed(() => ({ ...form }))
async function fetchList() {
  loading.value = true
  try { list.value = (await http.get('/activities')).data.data.items || [] } finally { loading.value = false }
}
function create() {
  Object.assign(form, { id: 0, title: '', subtitle: '', tag: '', coverUrl: '', detailImageUrl: '', videoUrl: '', linkUrl: '', description: '', startAt: '', endAt: '', sortOrder: 0, status: 'DRAFT', content: [] })
  visible.value = true
}
function edit(row: any) {
  let content: ActivitySection[] = []
  try { content = JSON.parse(row.contentJson || '[]') } catch { content = [] }
  const detailImage = content.find((section) => section.kind === 'DETAIL_IMAGE' || (section.imageUrl && !section.title && !section.content))
  Object.assign(form, {
    ...row,
    detailImageUrl: detailImage?.imageUrl || '',
    content: content.filter((section) => section !== detailImage),
  })
  visible.value = true
}
async function save() {
  const content = [
    ...(form.detailImageUrl ? [{ kind: 'DETAIL_IMAGE', title: '', content: '', imageUrl: form.detailImageUrl }] : []),
    ...form.content,
  ]
  const payload = { ...form, content }
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
.section-row { display: grid; grid-template-columns: 190px 1fr 150px 60px; gap: 12px; margin-bottom: 12px; align-items: start; }
.dialog-editor { display: flex; align-items: flex-start; gap: 20px; }
.dialog-form { min-width: 0; flex: 1; }
.field-tip { margin-left: 12px; color: #909399; font-size: 12px; line-height: 1.5; }
.url-input { width: 420px; margin-top: 10px; display: block; }
.section-row :deep(.upload-placeholder),
.section-row :deep(.preview-wrapper) {
  width: 140px;
  height: 86px;
}
@media (max-width: 1180px) {
  .dialog-editor { flex-direction: column; }
  .dialog-form { width: 100%; }
}
</style>
