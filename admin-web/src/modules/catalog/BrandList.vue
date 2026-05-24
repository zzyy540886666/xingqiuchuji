<template>
  <el-card>
    <template #header>
      <div class="header"><span>品牌主数据</span><el-button type="primary" @click="create">新增品牌</el-button></div>
    </template>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" width="100" />
      <el-table-column prop="name" label="品牌名称" />
      <el-table-column prop="logoUrl" label="Logo URL" />
      <el-table-column prop="sortOrder" label="排序" width="90" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }"><el-button link type="primary" @click="edit(row)">编辑</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template>
      </el-table-column>
    </el-table>
  </el-card>
  <el-dialog v-model="visible" title="品牌编辑" width="520px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="品牌名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="品牌 Logo"><ImageUpload v-model="form.logoUrl" /></el-form-item>
      <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
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
const form = reactive({ id: 0, name: '', logoUrl: '', sortOrder: 0 })
async function fetchList() {
  loading.value = true
  try { list.value = (await http.get('/brands')).data.data || [] } finally { loading.value = false }
}
function create() { Object.assign(form, { id: 0, name: '', logoUrl: '', sortOrder: 0 }); visible.value = true }
function edit(row: any) { Object.assign(form, row); visible.value = true }
async function save() {
  if (form.id) await http.put(`/brands/${form.id}`, form); else await http.post('/brands', form)
  ElMessage.success('品牌已保存'); visible.value = false; fetchList()
}
async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除品牌“${row.name}”？已关联商品的品牌不应删除。`, '删除确认', { type: 'warning' })
  await http.delete(`/brands/${row.id}`); ElMessage.success('品牌已删除'); fetchList()
}
onMounted(fetchList)
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
</style>
