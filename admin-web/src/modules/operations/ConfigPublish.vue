<template>
  <div>
    <el-card>
      <template #header>
        <span>配置发布记录</span>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="version" label="版本号" width="100" />
        <el-table-column prop="publishedBy" label="发布人" width="120" />
        <el-table-column prop="publishedAt" label="发布时间" width="180" />
        <el-table-column prop="description" label="变更说明" min-width="200" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.status === 'ACTIVE' ? '当前版本' : '历史' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="warning" @click="handleRollback(row)" v-if="row.status !== 'ACTIVE'">回滚</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'

const loading = ref(false)
const list = ref<any[]>([])

async function fetchList() {
  loading.value = true
  try {
    const res = await http.get('/configs/versions')
    if (res.data.success) list.value = res.data.data.items || res.data.data
  } finally {
    loading.value = false
  }
}

async function handleRollback(row: any) {
  await ElMessageBox.confirm(`确认回滚到版本 ${row.version}？此操作将生成新版本记录。`, '回滚确认', { type: 'warning' })
  await http.post(`/configs/rollback`, { targetVersion: row.version })
  ElMessage.success('回滚成功')
  fetchList()
}

onMounted(fetchList)
</script>
