<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>运营位管理</span>
          <el-button type="primary" @click="createBanner">新增运营位</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="位置" min-width="180">
          <template #default="{ row }">{{ getPositionLabel(row.position) }}</template>
        </el-table-column>
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

    <el-dialog v-model="dialogVisible" title="运营位编辑" width="920px">
      <div class="dialog-editor">
        <el-form :model="form" label-width="110px" class="dialog-form">
          <el-alert class="position-alert" title="首页顶部活动大图请在“活动详情”中维护首页封面图。" type="info" :closable="false" show-icon />
          <el-form-item label="位置">
            <el-select v-model="form.position">
              <el-option v-for="option in positionOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
            <div class="field-tip">{{ positionHelp }}</div>
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="form.title" maxlength="50" />
          </el-form-item>
          <el-form-item label="图片">
            <ImageUpload v-model="form.imageUrl" />
          </el-form-item>
          <el-form-item label="点击后打开">
            <LinkTargetPicker v-model="form.linkUrl" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="form.sortOrder" :min="0" />
          </el-form-item>
        </el-form>
        <MiniAppPreview mode="banner" :data="bannerPreview" />
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import LinkTargetPicker from '@/components/LinkTargetPicker.vue'
import MiniAppPreview from '@/components/MiniAppPreview.vue'
import http from '@/api/http'

const loading = ref(false)
const list = ref<any[]>([])
const dialogVisible = ref(false)
const form = reactive({ id: null as number | null, position: 'HOME_RENT_CARD', title: '', imageUrl: '', linkUrl: '', sortOrder: 0 })
const bannerPreview = computed(() => ({ ...form }))
const positionOptions = [
  { label: '首页快捷入口 - 租机器人', value: 'HOME_RENT_CARD', help: '替换首页第一张“租机器人”卡片的标题、图片和点击去向。' },
  { label: '首页快捷入口 - 买机器人', value: 'HOME_BUY_CARD', help: '替换首页第二张“买机器人”卡片的标题、图片和点击去向。' },
  { label: '首页快捷入口 - 应用商店', value: 'HOME_APP_CARD', help: '替换首页第三张“应用商店”卡片的标题、图片和点击去向。' },
] as const
const positionHelp = computed(() => positionOptions.find((option) => option.value === form.position)?.help || '')

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
  Object.assign(form, { ...row, position: normalizePosition(row.position) })
  dialogVisible.value = true
}

function createBanner() {
  Object.assign(form, { id: null, position: 'HOME_RENT_CARD', title: '', imageUrl: '', linkUrl: '', sortOrder: 0 })
  dialogVisible.value = true
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确认删除「${row.title}」？`, '确认')
  await http.delete(`/configs/banners/${row.id}`)
  ElMessage.success('已删除')
  fetchList()
}

async function handleSave() {
  form.position = normalizePosition(form.position)
  if (form.id) {
    await http.put(`/configs/banners/${form.id}`, form)
  } else {
    await http.post('/configs/banners', form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchList()
}

function normalizePosition(position: unknown) {
  const value = String(position || 'HOME_RENT_CARD')
  if (value === 'HOME_CARD') return 'HOME_RENT_CARD'
  if (value === 'PLANET_CARD') return 'HOME_APP_CARD'
  return positionOptions.some((option) => option.value === value) ? value : 'HOME_RENT_CARD'
}

function getPositionLabel(position: unknown) {
  const value = String(position || '')
  return positionOptions.find((option) => option.value === value)?.label || '历史配置（未展示）'
}

onMounted(fetchList)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.dialog-editor { display: flex; align-items: flex-start; gap: 20px; }
.dialog-form { min-width: 0; flex: 1; }
.field-tip { margin-left: 12px; color: #909399; font-size: 12px; line-height: 1.5; }
.position-alert { margin-bottom: 18px; }
@media (max-width: 980px) {
  .dialog-editor { flex-direction: column; }
  .dialog-form { width: 100%; }
}
</style>
