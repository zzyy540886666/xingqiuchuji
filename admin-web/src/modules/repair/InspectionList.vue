<template>
  <div>
    <el-card>
      <template #header>巡检管理</template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="planName" label="计划名称" min-width="160" />
        <el-table-column prop="cycle" label="周期" width="100" />
        <el-table-column prop="taskCount" label="任务数" width="80" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="inspectionStatusType(row.status)" size="small">
              {{ statusMap[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="nextRunAt" label="下次执行" width="180" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewTasks(row)">查看任务</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'

const loading = ref(false)
const list = ref<any[]>([])
const statusMap: Record<string, string> = { PENDING: '待执行', IN_PROGRESS: '进行中', COMPLETED: '已完成', OVERDUE: '逾期' }

function inspectionStatusType(s: string): any {
  const m: Record<string, string> = { PENDING: 'info', IN_PROGRESS: '', COMPLETED: 'success', OVERDUE: 'danger' }
  return m[s] || ''
}

async function fetchList() {
  loading.value = true
  try {
    const res = await http.get('/repair/inspections')
    if (res.data.success) list.value = res.data.data.items || res.data.data
  } finally {
    loading.value = false
  }
}

function handleViewTasks(row: any) {
  ElMessage.info(`查看计划「${row.planName}」的任务列表`)
}

onMounted(fetchList)
</script>
