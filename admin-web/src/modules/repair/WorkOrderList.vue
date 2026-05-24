<template>
  <div>
    <el-card>
      <template #header>工单管理</template>

      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="全部">
            <el-option label="待派单" value="PENDING" />
            <el-option label="处理中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="deviceName" label="设备" min-width="140" />
        <el-table-column prop="faultType" label="故障类型" width="120" />
        <el-table-column prop="assigneeName" label="处理人" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="workOrderStatusType(row.status)" size="small">{{ workOrderStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleAssign(row)" v-if="row.status === 'PENDING'">派单</el-button>
            <el-button link type="warning" @click="handleUrgent(row)" v-if="row.status === 'IN_PROGRESS'">加急</el-button>
            <el-button link type="info" @click="handleClose(row)" v-if="row.status !== 'CLOSED'">关闭</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'

const loading = ref(false)
const list = ref<any[]>([])
const filters = reactive({ status: '' })

function workOrderStatusType(s: string): any {
  const m: Record<string, string> = { PENDING: 'warning', IN_PROGRESS: '', COMPLETED: 'success', CLOSED: 'info' }
  return m[s] || ''
}

function workOrderStatusText(s: string): string {
  const m: Record<string, string> = { PENDING: '待派单', IN_PROGRESS: '处理中', COMPLETED: '已完成', CLOSED: '已关闭' }
  return m[s] || '未知'
}

async function fetchList() {
  loading.value = true
  try {
    const res = await http.get('/repair/workorders', { params: filters })
    if (res.data.success) list.value = res.data.data.items || res.data.data
  } finally {
    loading.value = false
  }
}

async function handleAssign(row: any) {
  const { value } = await ElMessageBox.prompt('请输入维修员名称', '派单')
  await http.post(`/repair/workorders/${row.id}/assign`, { assignee: value })
  ElMessage.success('已派单')
  fetchList()
}

async function handleUrgent(row: any) {
  await http.post(`/repair/workorders/${row.id}/urgent`)
  ElMessage.success('已加急')
  fetchList()
}

async function handleClose(row: any) {
  await ElMessageBox.confirm('确认关闭该工单？', '关闭确认')
  await http.post(`/repair/workorders/${row.id}/close`)
  ElMessage.success('已关闭')
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.filter-form { margin-bottom: 16px; }
</style>
