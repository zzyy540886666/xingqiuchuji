<template>
  <div>
    <el-card>
      <template #header>订单列表</template>

      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="全部">
            <el-option label="待支付" value="PENDING_PAY" />
            <el-option label="已支付" value="PAID" />
            <el-option label="履约中" value="FULFILLING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="filters.orderNo" placeholder="订单号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="skuName" label="商品" min-width="160" />
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column label="金额" width="120">
          <template #default="{ row }">{{ formatMoney(row.amountFen) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/orders/${row.id}`)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="20"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @change="fetchList"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { formatMoney } from '@/utils/money'
import http from '@/api/http'

const router = useRouter()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const filters = reactive({ status: '', orderNo: '' })

const statusMap: Record<string, string> = {
  PENDING_PAY: '待支付', PAID: '已支付', FULFILLING: '履约中', COMPLETED: '已完成', CANCELLED: '已取消',
}

function statusType(s: string): any {
  const map: Record<string, string> = { PENDING_PAY: 'warning', PAID: '', FULFILLING: '', COMPLETED: 'success', CANCELLED: 'info' }
  return map[s] || ''
}

async function fetchList() {
  loading.value = true
  try {
    const res = await http.get('/orders', { params: { ...filters, page: page.value, pageSize: 20 } })
    if (res.data.success) {
      list.value = res.data.data.items
      total.value = res.data.data.total
    }
  } finally {
    loading.value = false
  }
}

onMounted(fetchList)
</script>

<style scoped>
.filter-form { margin-bottom: 16px; }
</style>
