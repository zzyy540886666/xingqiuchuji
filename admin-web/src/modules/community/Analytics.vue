<template>
  <div>
    <el-card v-loading="loading">
      <template #header>运营数据</template>

      <el-row :gutter="16" class="stat-row">
        <el-col v-for="item in summary" :key="item.label" :span="4">
          <div class="stat-item">
            <div class="stat-value">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </el-col>
      </el-row>

      <el-divider />

      <el-row :gutter="16">
        <el-col :span="12">
          <h4>发帖趋势</h4>
          <div ref="trendChartRef" class="chart"></div>
        </el-col>
        <el-col :span="12">
          <h4>状态占比</h4>
          <div ref="statusChartRef" class="chart"></div>
        </el-col>
      </el-row>

      <el-divider />

      <h4>热门帖子</h4>
      <el-table :data="hotPosts" stripe>
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column prop="circleName" label="话题" width="140" />
        <el-table-column prop="likeCount" label="点赞" width="90" />
        <el-table-column prop="commentCount" label="评论" width="90" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import http from '@/api/http'

const loading = ref(false)
const summary = ref<any[]>([])
const hotPosts = ref<any[]>([])
const trendChartRef = ref<HTMLElement>()
const statusChartRef = ref<HTMLElement>()

async function fetchData() {
  loading.value = true
  try {
    const res = await http.get('/community/analytics')
    if (res.data.success) {
      const data = res.data.data
      summary.value = data.summary || []
      hotPosts.value = data.hotPosts || []
      await nextTick()
      renderTrend(data.trend || [])
      renderStatus(data.statusSplit || [])
    }
  } finally {
    loading.value = false
  }
}

function renderTrend(rows: any[]) {
  if (!trendChartRef.value) return
  const chart = echarts.init(trendChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: rows.map((item) => item.date) },
    yAxis: { type: 'value', name: '篇' },
    series: [{ name: '帖子数', type: 'bar', data: rows.map((item) => item.count) }],
  })
  window.addEventListener('resize', () => chart.resize())
}

function renderStatus(rows: any[]) {
  if (!statusChartRef.value) return
  const chart = echarts.init(statusChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '60%',
      data: rows.map((item) => ({ name: item.label, value: item.value })),
    }],
  })
  window.addEventListener('resize', () => chart.resize())
}

onMounted(fetchData)
</script>

<style scoped>
.stat-row { margin-bottom: 16px; }
.stat-item { text-align: center; padding: 16px; background: #f5f7fa; border-radius: 6px; }
.stat-value { font-size: 24px; font-weight: 600; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.chart { height: 300px; }
</style>
