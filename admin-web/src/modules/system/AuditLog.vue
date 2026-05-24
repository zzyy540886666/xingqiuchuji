<template>
  <div>
    <el-card>
      <template #header>审计日志</template>

      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="操作者">
          <el-input v-model="filters.operator" placeholder="管理员名称" clearable />
        </el-form-item>
        <el-form-item label="动作">
          <el-select v-model="filters.action" clearable placeholder="全部">
            <el-option label="登录" value="LOGIN" />
            <el-option label="发布" value="PUBLISH" />
            <el-option label="审核" value="MODERATE" />
            <el-option label="上下架" value="SHELF_TOGGLE" />
            <el-option label="提现审批" value="WITHDRAW_AUDIT" />
            <el-option label="权限变更" value="PERMISSION_CHANGE" />
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
        <el-table-column prop="operatorName" label="操作者" width="120" />
        <el-table-column prop="action" label="动作" width="120" />
        <el-table-column prop="resourceType" label="资源类型" width="120" />
        <el-table-column prop="resourceId" label="资源 ID" width="100" />
        <el-table-column prop="description" label="说明" min-width="200" />
        <el-table-column prop="traceId" label="traceId" width="180" />
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
import http from '@/api/http'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const filters = reactive({ operator: '', action: '', dateRange: null as any })

async function fetchList() {
  loading.value = true
  try {
    const res = await http.get('/system/audit-logs', { params: { operator: filters.operator, action: filters.action, page: page.value, pageSize: 20 } })
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
</style>
