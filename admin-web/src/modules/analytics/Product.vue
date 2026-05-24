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
import * as echarts from 'echarts'

const dateRange = ref<[Date, Date] | ''>('')
const exposureChartRef = ref<HTMLElement>()
const funnelChartRef = ref<HTMLElement>()

function fetchData() { /* 模拟数据与种子订单保持一致 */ }

onMounted(() => {
  if (exposureChartRef.value) {
    const chart = echarts.init(exposureChartRef.value)
    chart.setOption({
      tooltip: {},
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: ['G1-u2', 'Go2', 'H1', 'B2', 'Z1'] },
      series: [{ type: 'bar', data: [1260, 980, 720, 560, 430] }],
    })
    window.addEventListener('resize', () => chart.resize())
  }
  if (funnelChartRef.value) {
    const chart = echarts.init(funnelChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'funnel',
        data: [
          { name: '曝光', value: 1260 },
          { name: '详情访问', value: 468 },
          { name: '加入购物车', value: 96 },
          { name: '下单', value: 14 },
          { name: '支付', value: 2 },
        ],
      }],
    })
    window.addEventListener('resize', () => chart.resize())
  }
})
</script>

<style scoped>
.filter-form { margin-bottom: 16px; }
</style>
