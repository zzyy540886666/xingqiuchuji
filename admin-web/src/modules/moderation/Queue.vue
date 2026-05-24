<template>
  <div>
    <el-card>
      <template #header>审核队列</template>

      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable>
            <el-option label="待审核" value="AUDITING" />
            <el-option label="人工复审" value="MANUAL_REVIEW" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag type="warning" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" @click="handleApprove(row)">通过</el-button>
            <el-button link type="danger" @click="handleReject(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="20"
        layout="total, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @change="fetchList"
      />
    </el-card>

    <el-dialog v-model="rejectDialogVisible" title="驳回原因" width="400px">
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请填写驳回原因（必填）" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :disabled="!rejectReason.trim()">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const filters = reactive({ status: 'AUDITING' })

function statusText(status: string): string {
  const map: Record<string, string> = {
    AUDITING: '待审核',
    MANUAL_REVIEW: '人工复核',
    APPROVED: '已通过',
    REJECTED: '已驳回',
  }
  return map[status] || '待审核'
}

const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const rejectingId = ref<number | null>(null)

async function fetchList() {
  loading.value = true
  try {
    const res = await http.get('/posts', { params: { ...filters, page: page.value, pageSize: 20 } })
    if (res.data.success) {
      list.value = res.data.data.items
      total.value = res.data.data.total
    }
  } finally {
    loading.value = false
  }
}

async function handleApprove(row: any) {
  await ElMessageBox.confirm(`确认通过帖子「${row.title}」？`, '审核确认')
  await http.post(`/posts/${row.id}/approve`)
  ElMessage.success('已通过')
  fetchList()
}

function handleReject(row: any) {
  rejectingId.value = row.id
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

async function confirmReject() {
  await http.post(`/posts/${rejectingId.value}/reject`, { reason: rejectReason.value })
  ElMessage.success('已驳回')
  rejectDialogVisible.value = false
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.filter-form { margin-bottom: 16px; }
</style>
