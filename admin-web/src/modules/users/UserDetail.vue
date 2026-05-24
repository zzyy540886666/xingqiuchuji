<template>
  <div>
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>用户详情</span>
          <el-button @click="router.back()">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detail.nickname }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.phone || '***' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
            {{ detail.status === 'ACTIVE' ? '正常' : '冻结' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="会员等级">{{ detail.memberLevel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="原住民">{{ detail.isOriginal ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ detail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="星球卡">{{ detail.planetCardSummary || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />
      <h4>托管资产摘要</h4>
      <el-empty v-if="!detail.assets?.length" description="暂无托管资产" />
      <el-table v-else :data="detail.assets" stripe>
        <el-table-column prop="deviceName" label="设备" />
        <el-table-column prop="period" label="时段" />
        <el-table-column prop="status" label="状态" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = reactive<any>({ assets: [] })

async function fetchDetail() {
  loading.value = true
  try {
    const res = await http.get(`/users/${route.params.id}`)
    if (res.data.success) Object.assign(detail, res.data.data)
  } finally {
    loading.value = false
  }
}

onMounted(fetchDetail)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
