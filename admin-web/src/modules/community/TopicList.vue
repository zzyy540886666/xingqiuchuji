<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>话题管理</span>
          <el-button type="primary" @click="fetchList">刷新</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="话题名称或说明" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="编号" width="90" />
        <el-table-column prop="name" label="话题名称" min-width="160" />
        <el-table-column prop="description" label="说明" min-width="260" />
        <el-table-column prop="memberCount" label="成员数" width="100" />
        <el-table-column prop="postCount" label="帖子数" width="100" />
        <el-table-column prop="activePostCount" label="可见帖子" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
              {{ row.status === 'ACTIVE' ? '正常' : '已撤销' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'ACTIVE'" link type="danger" @click="revokeTopic(row)">撤销话题</el-button>
            <el-button v-else link type="success" @click="restoreTopic(row)">恢复话题</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next"
        :page-sizes="[10, 20, 50]"
        style="margin-top: 16px; justify-content: flex-end"
        @change="fetchList"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const filters = reactive({ keyword: '' })

async function fetchList() {
  loading.value = true
  try {
    const res = await http.get('/community/topics', { params: { ...filters, page: page.value, pageSize: pageSize.value } })
    if (res.data.success) {
      list.value = res.data.data.items
      total.value = res.data.data.total
    }
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.keyword = ''
  page.value = 1
  fetchList()
}

async function revokeTopic(row: any) {
  await ElMessageBox.confirm(`确认撤销话题「${row.name}」下的可见帖子？`, '二次确认', { type: 'warning' })
  await http.post(`/community/topics/${row.id}/revoke`)
  ElMessage.success('撤销成功')
  fetchList()
}

async function restoreTopic(row: any) {
  await http.post(`/community/topics/${row.id}/restore`)
  ElMessage.success('恢复成功')
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.filter-form { margin-bottom: 16px; }
</style>
