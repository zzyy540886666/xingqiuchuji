<template>
  <div>
    <el-card>
      <template #header>佣金与结算</template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderId" label="订单 ID" width="120" />
        <el-table-column prop="beneficiaryName" label="受益人" width="120" />
        <el-table-column prop="level" label="层级" width="80" />
        <el-table-column label="佣金" width="120">
          <template #default="{ row }">{{ formatMoney(row.amountFen) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SETTLED' ? 'success' : 'warning'" size="small">
              {{ row.status === 'SETTLED' ? '已结算' : '待结算' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="settledAt" label="结算时间" width="180" />
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
import { formatMoney } from '@/utils/money'
import http from '@/api/http'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)

async function fetchList() {
  loading.value = true
  try {
    const res = await http.get('/distribution/commissions', { params: { page: page.value, pageSize: 20 } })
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
