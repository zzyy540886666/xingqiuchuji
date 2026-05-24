<template>
  <div>
    <el-card>
      <template #header>钱包流水</template>

      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="来源">
          <el-select v-model="filters.source" clearable placeholder="全部">
            <el-option label="充值" value="RECHARGE" />
            <el-option label="消费" value="CONSUME" />
            <el-option label="提现" value="WITHDRAW" />
            <el-option label="佣金" value="COMMISSION" />
            <el-option label="退款" value="REFUND" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker v-model="filters.dateRange" type="daterange" start-placeholder="开始" end-placeholder="结束" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户 ID" width="100" />
        <el-table-column prop="source" label="来源" width="100" />
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            <span :class="row.amountFen > 0 ? 'text-green' : 'text-red'">{{ formatMoney(row.amountFen) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="余额" width="120">
          <template #default="{ row }">{{ formatMoney(row.balanceFen) }}</template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="160" />
        <el-table-column prop="createdAt" label="时间" width="180" />
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
import { ref, reactive, onMounted } from 'vue'
import { formatMoney } from '@/utils/money'
import http from '@/api/http'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const filters = reactive({ source: '', dateRange: null as any })

async function fetchList() {
  loading.value = true
  try {
    const res = await http.get('/finance/wallet-flows', { params: { source: filters.source, page: page.value, pageSize: 20 } })
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
.text-green { color: #67c23a; }
.text-red { color: #f56c6c; }
</style>
