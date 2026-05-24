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
import * as echarts from 'echarts'

const topicChartRef = ref<HTMLElement>()
const bannerChartRef = ref<HTMLElement>()

onMounted(() => {
  if (topicChartRef.value) {
    const chart = echarts.init(topicChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
      yAxis: { type: 'value' },
      series: [{ name: '访问量', type: 'line', smooth: true, data: [168, 226, 310, 288, 356, 421, 390] }],
    })
    window.addEventListener('resize', () => chart.resize())
  }
  if (bannerChartRef.value) {
    const chart = echarts.init(bannerChartRef.value)
    chart.setOption({
      tooltip: {},
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: ['春季租赁', '新品上线', '巡检组合'] },
      series: [{ type: 'bar', data: [86, 72, 51] }],
    })
    window.addEventListener('resize', () => chart.resize())
  }
})
</script>
