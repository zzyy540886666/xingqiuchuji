<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>管理员列表</span>
          <el-button type="primary" @click="dialogVisible = true">新增管理员</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="账号" width="140" />
        <el-table-column prop="displayName" label="姓名" width="120" />
        <el-table-column prop="roles" label="角色" min-width="160">
          <template #default="{ row }">
            <el-tag v-for="r in row.roles" :key="r" size="small" style="margin-right: 4px">{{ r }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginAt" label="最后登录" width="180" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleResetPwd(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="管理员" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="账号">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.displayName" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roles" multiple>
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="运营管理员" value="OPERATOR" />
            <el-option label="内容审核员" value="MODERATOR" />
            <el-option label="财务结算员" value="FINANCE" />
            <el-option label="客服" value="SUPPORT" />
            <el-option label="运维管理员" value="MAINTENANCE_ADMIN" />
            <el-option label="只读分析员" value="ANALYST" />
          </el-select>
        </el-form-item>
        <el-form-item label="密码" v-if="!form.id">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'

const loading = ref(false)
const list = ref<any[]>([])
const dialogVisible = ref(false)
const form = reactive({ id: null as number | null, username: '', displayName: '', roles: [] as string[], password: '' })

async function fetchList() {
  loading.value = true
  try {
    const res = await http.get('/system/admins')
    if (res.data.success) list.value = res.data.data.items || res.data.data
  } finally {
    loading.value = false
  }
}

function handleEdit(row: any) {
  Object.assign(form, { ...row, password: '' })
  dialogVisible.value = true
}

async function handleResetPwd(row: any) {
  await ElMessageBox.confirm(`确认重置「${row.displayName}」的密码？`, '重置密码')
  await http.post(`/system/admins/${row.id}/reset-password`)
  ElMessage.success('密码已重置')
}

async function handleSave() {
  if (form.id) {
    await http.put(`/system/admins/${form.id}`, form)
  } else {
    await http.post('/system/admins', form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
