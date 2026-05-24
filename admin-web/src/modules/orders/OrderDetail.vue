<template>
  <div>
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>订单详情</span>
          <el-button @click="router.back()">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small">{{ detail.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="商品">{{ detail.skuName }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ detail.userName }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ formatMoney(detail.amountFen || 0) }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ detail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ detail.paidAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="traceId">{{ detail.traceId || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />
      <h4>状态流水</h4>
      <el-timeline>
        <el-timeline-item v-for="event in detail.events" :key="event.id" :timestamp="event.createdAt">
          {{ event.description }}
        </el-timeline-item>
      </el-timeline>
      <el-empty v-if="!detail.events?.length" description="暂无流水记录" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { formatMoney } from '@/utils/money'
import http from '@/api/http'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = reactive<any>({ events: [] })

async function fetchDetail() {
  loading.value = true
  try {
    const res = await http.get(`/orders/${route.params.id}`)
    if (res.data.success) Object.assign(detail, res.data.data)
  } finally {
    loading.value = false
  }
}

onMounted(fetchDetail)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
