<template>
  <div>
    <el-card>
      <template #header>提现审核</template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户 ID" width="100" />
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column label="金额" width="120">
          <template #default="{ row }">{{ formatMoney(row.amountFen) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button link type="success" @click="handleApprove(row)">通过</el-button>
              <el-button link type="danger" @click="handleReject(row)">拒绝</el-button>
            </template>
            <el-button link type="warning" @click="handleRetry(row)" v-if="row.status === 'FAILED'">重试</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatMoney } from '@/utils/money'
import http from '@/api/http'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)

const statusMap: Record<string, string> = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝', PROCESSING: '处理中', FAILED: '失败', COMPLETED: '已完成' }
function statusType(s: string): any {
  const m: Record<string, string> = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', FAILED: 'danger', COMPLETED: 'success' }
  return m[s] || 'info'
}

async function fetchList() {
  loading.value = true
  try {
    const res = await http.get('/finance/withdrawals', { params: { page: page.value, pageSize: 20 } })
    if (res.data.success) {
      list.value = res.data.data.items
      total.value = res.data.data.total
    }
  } finally {
    loading.value = false
  }
}

async function handleApprove(row: any) {
  await ElMessageBox.confirm(`确认通过提现 ${formatMoney(row.amountFen)}？`, '审批确认')
  await http.post(`/finance/withdrawals/${row.id}/approve`)
  ElMessage.success('已通过')
  fetchList()
}

async function handleReject(row: any) {
  const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝', { inputPattern: /.+/, inputErrorMessage: '原因不能为空' })
  await http.post(`/finance/withdrawals/${row.id}/reject`, { reason })
  ElMessage.success('已拒绝')
  fetchList()
}

async function handleRetry(row: any) {
  await ElMessageBox.confirm('确认重试该提现？', '重试确认')
  await http.post(`/finance/withdrawals/${row.id}/retry`)
  ElMessage.success('已提交重试')
  fetchList()
}

onMounted(fetchList)
</script>
