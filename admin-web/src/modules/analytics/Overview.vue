<template>
  <div>
    <el-card>
      <template #header>经营概览</template>

      <el-row :gutter="16" class="stat-row">
        <el-col :span="6" v-for="stat in stats" :key="stat.label">
          <div class="stat-item">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-col>
      </el-row>

      <el-divider />

      <el-row :gutter="16">
        <el-col :span="12">
          <h4>成交额趋势</h4>
          <div ref="gmvChartRef" style="height: 300px"></div>
        </el-col>
        <el-col :span="12">
          <h4>订单类型占比</h4>
          <div ref="typeChartRef" style="height: 300px"></div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import echarts from '@/utils/echarts-pie'

const gmvChartRef = ref<HTMLElement>()
const typeChartRef = ref<HTMLElement>()

// ----
// Mock data — pending real analytics API
// ----
// TODO(GAP-P02): Replace with real API calls when backend analytics endpoints are available.
// Expected endpoints:
//   GET /api/v1/admin/analytics/overview/summary?from=&to=   -> { gmvMinor, paidOrders, avgOrderMinor, activeUsers }
//   GET /api/v1/admin/analytics/overview/gmv-trend?from=&to= -> { trend: [{date, amountMinor}] }
//   GET /api/v1/admin/analytics/overview/type-split?from=&to= -> { types: [{name, count}] }
// These aggregations should be computed from orders tables joined with SKU types.
// ----

const stats = ref([
  { label: '本月成交额', value: '¥6,899.00' },
  { label: '已支付订单', value: '2' },
  { label: '平均客单价', value: '¥3,449.50' },
  { label: '活跃用户', value: '2' },
])

const MOCK_GMV_TREND = [120, 280, 360, 420, 0, 680, 720, 0, 960, 1100, 0, 1380, 1580, 1760, 0, 2080, 2200, 0, 2450, 2800, 0, 3200, 3600, 3899, 0, 4200, 4600, 5100, 5600, 6899]

const MOCK_TYPE_SPLIT = [
  { name: '租赁', value: 2 },
  { name: '购买', value: 1 },
  { name: '软件服务', value: 1 },
]

onMounted(() => {
  if (gmvChartRef.value) {
    const chart = echarts.init(gmvChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: Array.from({ length: 30 }, (_, i) => `${i + 1}`) },
      yAxis: { type: 'value', name: '元' },
      series: [{ name: '成交额', type: 'bar', data: MOCK_GMV_TREND }],
    })
    window.addEventListener('resize', () => chart.resize())
  }
  if (typeChartRef.value) {
    const chart = echarts.init(typeChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '60%',
        data: MOCK_TYPE_SPLIT,
      }],
    })
    window.addEventListener('resize', () => chart.resize())
  }
})
</script>

<style scoped>
.stat-row { margin-bottom: 16px; }
.stat-item { text-align: center; padding: 16px; background: #f5f7fa; border-radius: 6px; }
.stat-value { font-size: 24px; font-weight: 600; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
</style>
