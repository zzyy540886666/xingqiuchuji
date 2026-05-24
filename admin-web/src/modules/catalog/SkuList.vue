<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>SKU 列表</span>
          <el-button type="primary" @click="handleCreate">新增 SKU</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="类型">
          <el-select v-model="filters.type" clearable placeholder="全部">
            <el-option label="租赁" value="RENT" />
            <el-option label="购买" value="BUY" />
            <el-option label="软件" value="SOFTWARE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="全部">
            <el-option label="上架" value="ON_SHELF" />
            <el-option label="下架" value="OFF_SHELF" />
            <el-option label="草稿" value="DRAFT" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="名称/品牌" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" min-width="160" />
        <el-table-column prop="brand" label="品牌" width="120" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small">{{ typeMap[row.type] || row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="价格(元)" width="120">
          <template #default="{ row }">{{ formatMoney(row.priceFen) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ON_SHELF' ? 'success' : 'info'" size="small">
              {{ statusMap[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 'ON_SHELF' ? 'warning' : 'success'" @click="handleToggleShelf(row)">
              {{ row.status === 'ON_SHELF' ? '下架' : '上架' }}
            </el-button>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { formatMoney } from '@/utils/money'
import http from '@/api/http'

const router = useRouter()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)

const filters = reactive({ type: '', status: '', keyword: '' })
const typeMap: Record<string, string> = { RENT: '租赁', BUY: '购买', SOFTWARE: '软件' }
const statusMap: Record<string, string> = { ON_SHELF: '上架', OFF_SHELF: '下架', DRAFT: '草稿' }

async function fetchList() {
  loading.value = true
  try {
    const res = await http.get('/skus', { params: { ...filters, page: page.value, pageSize: pageSize.value } })
    if (res.data.success) {
      list.value = res.data.data.items
      total.value = res.data.data.total
    }
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  Object.assign(filters, { type: '', status: '', keyword: '' })
  page.value = 1
  fetchList()
}

function handleCreate() {
  router.push('/catalog/sku/new')
}

function handleEdit(row: any) {
  router.push(`/catalog/sku/${row.id}`)
}

async function handleToggleShelf(row: any) {
  const action = row.status === 'ON_SHELF' ? '下架' : '上架'
  await ElMessageBox.confirm(`确认${action}「${row.name}」？`, '二次确认', { type: 'warning' })
  try {
    await http.post(`/skus/${row.id}/toggle-shelf`)
    ElMessage.success(`${action}成功`)
    fetchList()
  } catch { /* handled by interceptor */ }
}

onMounted(fetchList)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.filter-form { margin-bottom: 16px; }
</style>
