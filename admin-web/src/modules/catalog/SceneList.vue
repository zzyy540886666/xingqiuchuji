<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>首页场景应用</span>
          <el-button type="primary" @click="handleCreate">新增场景</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="图片" width="112">
          <template #default="{ row }">
            <el-image class="scene-cover" :src="row.imageUrl" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="场景名称" min-width="160" />
        <el-table-column label="标签" min-width="180">
          <template #default="{ row }">
            <el-tag v-for="tag in row.tags || []" :key="tag" class="scene-tag" size="small">{{ tag }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="首页排序" width="100" />
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑场景' : '新增场景'" width="620px">
      <el-form :model="sceneForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="sceneForm.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="sceneForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="展示图片">
          <ImageUpload v-model="sceneForm.imageUrl" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select
            v-model="sceneForm.tags"
            multiple
            filterable
            allow-create
            default-first-option
            :multiple-limit="4"
            placeholder="输入标签后回车，最多 4 个"
            style="width: 100%"
          >
            <el-option v-for="tag in tagOptions" :key="tag" :label="tag" :value="tag" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联 SKU">
          <el-select
            v-model="sceneForm.skuIds"
            multiple
            filterable
            collapse-tags
            collapse-tags-tooltip
            placeholder="选择该场景展示的商品"
            style="width: 100%"
          >
            <el-option v-for="sku in skuOptions" :key="sku.id" :label="sku.name" :value="sku.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="首页排序">
          <el-input-number v-model="sceneForm.sortOrder" :min="0" :max="9999" />
          <span class="field-tip">数字越小越靠前</span>
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
import { computed, ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import http from '@/api/http'

const loading = ref(false)
const list = ref<any[]>([])
const skuOptions = ref<Array<{ id: string; name: string }>>([])
const dialogVisible = ref(false)
const editingId = ref<string | null>(null)
const sceneForm = reactive({ name: '', description: '', imageUrl: '', tags: [] as string[], skuIds: [] as string[], sortOrder: 0 })
const tagOptions = computed(() => Array.from(new Set(list.value.flatMap((scene) => scene.tags || []))))

async function fetchList() {
  loading.value = true
  try {
    const res = await http.get('/scenes')
    if (res.data.success) list.value = res.data.data.items || res.data.data
  } finally {
    loading.value = false
  }
}

async function fetchSkuOptions() {
  const options: Array<{ id: string; name: string }> = []
  let page = 1
  let total = 0
  do {
    const res = await http.get('/skus', { params: { page, pageSize: 100 } })
    const data = res.data.data
    options.push(...(data?.items || []).map((sku: any) => ({ id: sku.id, name: sku.name })))
    total = data?.total || 0
    page += 1
  } while (options.length < total)
  skuOptions.value = options
}

function handleEdit(row: any) {
  editingId.value = row.id
  Object.assign(sceneForm, {
    name: row.name,
    description: row.description || '',
    imageUrl: row.imageUrl || '',
    tags: [...(row.tags || [])],
    skuIds: [...(row.skuIds || [])],
    sortOrder: row.sortOrder ?? 0,
  })
  dialogVisible.value = true
}

function handleCreate() {
  editingId.value = null
  Object.assign(sceneForm, { name: '', description: '', imageUrl: '', tags: [], skuIds: [], sortOrder: 0 })
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
  Object.assign(sceneForm, { name: '', description: '', imageUrl: '', tags: [], skuIds: [], sortOrder: 0 })
  fetchList()
}

onMounted(() => Promise.all([fetchList(), fetchSkuOptions()]))
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.scene-cover { width: 82px; height: 54px; border-radius: 6px; background: #f5f7fa; }
.scene-tag { margin-right: 6px; }
.field-tip { margin-left: 10px; color: #909399; font-size: 12px; }
</style>
