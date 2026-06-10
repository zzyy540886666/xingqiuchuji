<template>
  <div class="dashboard">
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="16">
        <el-card header="经营趋势">
          <div ref="chartRef" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card header="待办事项">
          <el-empty v-if="!todos.length" description="暂无待办" />
          <div v-else class="todo-list">
            <div v-for="item in todos" :key="item.id" class="todo-item">
              <el-tag :type="item.type" size="small">{{ item.tag }}</el-tag>
              <span class="todo-text">{{ item.text }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import echarts from '@/utils/echarts'

const chartRef = ref<HTMLElement>()

const stats = ref([
  { label: '今日 GMV', value: '¥0.00' },
  { label: '今日订单', value: '0' },
  { label: '待审内容', value: '0' },
  { label: '待处理工单', value: '0' },
])

const todos = ref<{ id: number; tag: string; text: string; type: any }[]>([])

onMounted(() => {
  if (chartRef.value) {
    const chart = echarts.init(chartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
      yAxis: { type: 'value' },
      series: [{ name: 'GMV', type: 'line', smooth: true, data: [0, 0, 0, 0, 0, 0, 0] }],
    })
    window.addEventListener('resize', () => chart.resize())
  }
})
</script>

<style scoped>
.stat-row { margin-bottom: 0; }
.stat-card { text-align: center; }
.stat-value { font-size: 28px; font-weight: 600; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
.todo-list { display: flex; flex-direction: column; gap: 12px; }
.todo-item { display: flex; align-items: center; gap: 8px; }
.todo-text { font-size: 14px; color: #606266; }
</style>
