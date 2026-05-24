<template>
  <div>
    <el-card>
      <template #header>用户列表</template>

      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="昵称/手机号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="全部">
            <el-option label="正常" value="ACTIVE" />
            <el-option label="冻结" value="FROZEN" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="memberLevel" label="会员等级" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
              {{ row.status === 'ACTIVE' ? '正常' : '冻结' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/users/${row.id}`)">详情</el-button>
            <el-button link :type="row.status === 'ACTIVE' ? 'danger' : 'success'" @click="handleToggleStatus(row)">
              {{ row.status === 'ACTIVE' ? '冻结' : '解冻' }}
            </el-button>
          </template>
        </el-table-column>
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'

const router = useRouter()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const filters = reactive({ keyword: '', status: '' })

async function fetchList() {
  loading.value = true
  try {
    const res = await http.get('/users', { params: { ...filters, page: page.value, pageSize: 20 } })
    if (res.data.success) {
      list.value = res.data.data.items
      total.value = res.data.data.total
    }
  } finally {
    loading.value = false
  }
}

async function handleToggleStatus(row: any) {
  const action = row.status === 'ACTIVE' ? '冻结' : '解冻'
  const { value: reason } = await ElMessageBox.prompt(`请输入${action}原因`, `${action}确认`, {
    inputPattern: /.+/,
    inputErrorMessage: '原因不能为空',
  })
  await http.post(`/users/${row.id}/toggle-status`, { reason })
  ElMessage.success(`${action}成功`)
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.filter-form { margin-bottom: 16px; }
</style>
