<template>
  <div>
    <el-card>
      <template #header>运营分析</template>

      <el-row :gutter="16">
        <el-col :span="12">
          <h4>专题访问趋势</h4>
          <div ref="topicChartRef" style="height: 300px"></div>
        </el-col>
        <el-col :span="12">
          <h4>运营位点击排行</h4>
          <div ref="bannerChartRef" style="height: 300px"></div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import echarts from '@/utils/echarts'

const topicChartRef = ref<HTMLElement>()
const bannerChartRef = ref<HTMLElement>()

// ----
// Mock data — pending real analytics API
// ----
// TODO(GAP-P02): Replace with real API calls when backend analytics endpoints are available.
// Expected endpoints:
//   GET /api/v1/admin/analytics/operations/topic-trend?days=7 -> { trend: [{day, visits}] }
//   GET /api/v1/admin/analytics/operations/banner-clicks?from=&to= -> { banners: [{name, clicks}] }
// These aggregations should come from event tracking joined with banner/topic config tables.
// ----

const MOCK_TOPIC_TREND = [
  { day: '周一', visits: 168 },
  { day: '周二', visits: 226 },
  { day: '周三', visits: 310 },
  { day: '周四', visits: 288 },
  { day: '周五', visits: 356 },
  { day: '周六', visits: 421 },
  { day: '周日', visits: 390 },
]

const MOCK_BANNER_CLICKS = [
  { name: '春季租赁', clicks: 86 },
  { name: '新品上线', clicks: 72 },
  { name: '巡检组合', clicks: 51 },
]

onMounted(() => {
  if (topicChartRef.value) {
    const chart = echarts.init(topicChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: MOCK_TOPIC_TREND.map(d => d.day) },
      yAxis: { type: 'value' },
      series: [{ name: '访问量', type: 'line', smooth: true, data: MOCK_TOPIC_TREND.map(d => d.visits) }],
    })
    window.addEventListener('resize', () => chart.resize())
  }
  if (bannerChartRef.value) {
    const chart = echarts.init(bannerChartRef.value)
    chart.setOption({
      tooltip: {},
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: MOCK_BANNER_CLICKS.map(d => d.name) },
      series: [{ type: 'bar', data: MOCK_BANNER_CLICKS.map(d => d.clicks) }],
    })
    window.addEventListener('resize', () => chart.resize())
  }
})
</script>
