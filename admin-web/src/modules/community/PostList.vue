<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>帖子内容</span>
          <el-button type="primary" @click="fetchList">刷新</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="全部" style="width: 140px">
            <el-option label="已发布" value="APPROVED" />
            <el-option label="待审核" value="AUDITING" />
            <el-option label="人工复核" value="MANUAL_REVIEW" />
            <el-option label="已撤销" value="REVOKED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="标题或内容" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="编号" width="90" />
        <el-table-column label="帖子内容" min-width="300">
          <template #default="{ row }">
            <div class="post-title">{{ row.title || '未命名帖子' }}</div>
            <div class="post-content">{{ row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column prop="circleName" label="话题" width="140" />
        <el-table-column label="互动" width="120">
          <template #default="{ row }">
            <span>{{ row.likeCount }} 赞 / {{ row.commentCount }} 评</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.statusText || statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="置顶" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.pinned" type="success" size="small">是</el-tag>
            <span v-else>否</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status !== 'REVOKED'" link type="danger" @click="revokePost(row)">撤销</el-button>
            <el-button v-else link type="success" @click="restorePost(row)">恢复</el-button>
            <el-button v-if="!row.pinned && row.status === 'APPROVED'" link type="primary" @click="pinPost(row)">置顶</el-button>
            <el-button v-if="row.pinned" link type="warning" @click="unpinPost(row)">取消置顶</el-button>
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
const filters = reactive({ status: '', keyword: '' })

function statusText(status: string): string {
  const map: Record<string, string> = {
    APPROVED: '已发布',
    AUDITING: '待审核',
    MANUAL_REVIEW: '人工复核',
    REVOKED: '已撤销',
    REJECTED: '已拒绝',
  }
  return map[status] || '未知'
}

function statusType(status: string): any {
  if (status === 'APPROVED') return 'success'
  if (status === 'REVOKED' || status === 'REJECTED') return 'danger'
  if (status === 'MANUAL_REVIEW') return 'warning'
  return 'info'
}

async function fetchList() {
  loading.value = true
  try {
    const res = await http.get('/community/posts', { params: { ...filters, page: page.value, pageSize: pageSize.value } })
    if (res.data.success) {
      list.value = res.data.data.items
      total.value = res.data.data.total
    }
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  Object.assign(filters, { status: '', keyword: '' })
  page.value = 1
  fetchList()
}

async function revokePost(row: any) {
  await ElMessageBox.confirm(`确认撤销「${row.title || '未命名帖子'}」？`, '二次确认', { type: 'warning' })
  await http.post(`/community/posts/${row.id}/revoke`)
  ElMessage.success('撤销成功')
  fetchList()
}

async function restorePost(row: any) {
  await http.post(`/community/posts/${row.id}/restore`)
  ElMessage.success('恢复成功')
  fetchList()
}

async function pinPost(row: any) {
  await http.post(`/community/posts/${row.id}/pin`)
  ElMessage.success('置顶成功')
  fetchList()
}

async function unpinPost(row: any) {
  await http.post(`/community/posts/${row.id}/unpin`)
  ElMessage.success('已取消置顶')
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.filter-form { margin-bottom: 16px; }
.post-title { font-weight: 600; color: #303133; margin-bottom: 6px; }
.post-content {
  color: #606266;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
