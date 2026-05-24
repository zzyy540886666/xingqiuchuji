<template>
  <div>
    <el-card>
      <template #header>角色权限</template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" width="160" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="permissionCount" label="权限数" width="100" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑权限</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'

const loading = ref(false)
const list = ref<any[]>([])

async function fetchList() {
  loading.value = true
  try {
    const res = await http.get('/system/roles')
    if (res.data.success) list.value = res.data.data.items || res.data.data
  } finally {
    loading.value = false
  }
}

function handleEdit(row: any) {
  ElMessage.info(`编辑角色「${row.name}」的权限`)
}

onMounted(fetchList)
</script>
