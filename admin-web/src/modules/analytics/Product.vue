<template>
  <div>
    <el-card>
      <template #header>商品分析</template>

      <el-form :inline="true" class="filter-form">
        <el-form-item label="时间范围">
          <el-date-picker v-model="dateRange" type="daterange" start-placeholder="开始日期" end-placeholder="结束日期" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="16">
        <el-col :span="12">
          <h4>商品曝光前五</h4>
          <div ref="exposureChartRef" style="height: 300px"></div>
        </el-col>
        <el-col :span="12">
          <h4>下单转化漏斗</h4>
          <div ref="funnelChartRef" style="height: 300px"></div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import echarts from '@/utils/echarts-funnel'

const dateRange = ref<[Date, Date] | ''>('')
const exposureChartRef = ref<HTMLElement>()
const funnelChartRef = ref<HTMLElement>()

// ----
// Mock data — pending real analytics API
// ----
// TODO(GAP-P02): Replace with real API calls when backend analytics endpoints are available.
// Expected endpoints:
//   GET /api/v1/admin/analytics/product/exposure?from=&to=   -> { skus: [{name, views}] }
//   GET /api/v1/admin/analytics/product/funnel?from=&to=     -> { funnel: [{stage, count}] }
// Data should come from event tracking (AnalyticsController.events / beacon pipeline)
// aggregated by SKU + event type (exposure, detail, cart_add, order_create, payment_success).
// ----

const MOCK_EXPOSURE = [
  { name: 'G1-u2', value: 1260 },
  { name: 'Go2',  value: 980 },
  { name: 'H1',   value: 720 },
  { name: 'B2',   value: 560 },
  { name: 'Z1',   value: 430 },
]

const MOCK_FUNNEL = [
  { name: '曝光',         value: 1260 },
  { name: '详情访问',     value: 468 },
  { name: '加入购物车',   value: 96 },
  { name: '下单',         value: 14 },
  { name: '支付',         value: 2 },
]

function fetchData() {
  // TODO(GAP-P02): call real analytics API when available
  // http.get('/analytics/product/exposure', { params: { from: dateRange[0], to: dateRange[1] } })
  // http.get('/analytics/product/funnel',    { params: { from: dateRange[0], to: dateRange[1] } })
}

onMounted(() => {
  if (exposureChartRef.value) {
    const chart = echarts.init(exposureChartRef.value)
    chart.setOption({
      tooltip: {},
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: MOCK_EXPOSURE.map(d => d.name) },
      series: [{ type: 'bar', data: MOCK_EXPOSURE.map(d => d.value) }],
    })
    window.addEventListener('resize', () => chart.resize())
  }
  if (funnelChartRef.value) {
    const chart = echarts.init(funnelChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'funnel',
        data: MOCK_FUNNEL,
      }],
    })
    window.addEventListener('resize', () => chart.resize())
  }
})
</script>

<style scoped>
.filter-form { margin-bottom: 16px; }
</style>
