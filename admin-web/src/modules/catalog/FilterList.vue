<template>
  <el-card>
    <template #header>
      <div class="header">
        <span>商品页左侧菜单管理</span>
        <el-button type="primary" @click="openCreate">新增菜单组</el-button>
      </div>
    </template>
    <el-alert title="配置将直接供小程序商品分类页筛选使用；价格金额单位为分。" type="info" :closable="false" class="notice" />
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="title" label="菜单组" width="150" />
      <el-table-column prop="code" label="编码" width="130" />
      <el-table-column prop="filterField" label="筛选字段" width="130" />
      <el-table-column label="选项">
        <template #default="{ row }">
          <el-tag v-for="option in row.options" :key="option.id" class="option-tag">{{ option.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }"><el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="visible" title="商品筛选菜单" width="760px">
    <el-form :model="form" label-width="90px">
      <div class="grid">
        <el-form-item label="名称"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="字段">
          <el-select v-model="form.filterField">
            <el-option label="品牌 ID" value="BRAND_ID" /><el-option label="价格区间" value="PRICE_RANGE" />
            <el-option label="型号 ID" value="MODEL_ID" /><el-option label="关键词" value="KEYWORD" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.enabled" /></el-form-item>
      </div>
      <el-divider content-position="left">菜单选项</el-divider>
      <div v-for="(option, index) in form.options" :key="index" class="option-row">
        <el-input v-model="option.label" placeholder="展示名称" />
        <el-input v-model="option.value" placeholder="筛选值" />
        <el-input-number v-if="form.filterField === 'PRICE_RANGE'" v-model="option.minPriceMinor" :min="0" placeholder="最低分" />
        <el-input-number v-if="form.filterField === 'PRICE_RANGE'" v-model="option.maxPriceMinor" :min="0" placeholder="最高分" />
        <el-button link type="danger" @click="form.options.splice(index, 1)">删除</el-button>
      </div>
      <el-button @click="addOption">新增选项</el-button>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'

type Option = { label: string; value: string; minPriceMinor?: number; maxPriceMinor?: number; sortOrder?: number; enabled?: boolean }
const loading = ref(false)
const visible = ref(false)
const list = ref<any[]>([])
const form = reactive({ id: 0, title: '', code: '', filterField: 'KEYWORD', sortOrder: 0, enabled: true, options: [] as Option[] })

async function fetchList() {
  loading.value = true
  try { list.value = (await http.get('/catalog-filters')).data.data || [] } finally { loading.value = false }
}
function reset() { Object.assign(form, { id: 0, title: '', code: '', filterField: 'KEYWORD', sortOrder: 0, enabled: true, options: [] }) }
function openCreate() { reset(); addOption(); visible.value = true }
function openEdit(row: any) { Object.assign(form, { ...row, options: row.options.map((item: Option) => ({ ...item })) }); visible.value = true }
function addOption() { form.options.push({ label: '', value: '', sortOrder: form.options.length, enabled: true }) }
async function save() {
  const payload = { ...form, options: form.options.map((option, index) => ({ ...option, sortOrder: index, enabled: true })) }
  if (form.id) await http.put(`/catalog-filters/${form.id}`, payload); else await http.post('/catalog-filters', payload)
  ElMessage.success('菜单配置已保存')
  visible.value = false
  fetchList()
}
async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除菜单组“${row.title}”？`, '删除确认', { type: 'warning' })
  await http.delete(`/catalog-filters/${row.id}`)
  ElMessage.success('已删除')
  fetchList()
}
onMounted(fetchList)
</script>

<style scoped>
.header { display: flex; align-items: center; justify-content: space-between; }
.notice { margin-bottom: 16px; }
.option-tag { margin: 3px 5px 3px 0; }
.grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 0 12px; }
.option-row { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
</style>
