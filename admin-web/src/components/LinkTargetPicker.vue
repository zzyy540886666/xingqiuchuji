<template>
  <div class="link-picker">
    <el-select v-model="targetType" class="target-select" @change="emitUrl">
      <el-option label="不跳转" value="NONE" />
      <el-option label="租赁分类页" value="RENT" />
      <el-option label="购买分类页" value="BUY" />
      <el-option label="应用商店" value="SOFTWARE" />
      <el-option label="活动详情页" value="ACTIVITY" />
      <el-option label="搜索结果页" value="SEARCH" />
      <el-option label="会员/星球卡页" value="MEMBER" />
      <el-option label="自定义页面路径" value="CUSTOM" />
    </el-select>

    <el-input
      v-if="targetType === 'ACTIVITY'"
      v-model="activityId"
      class="target-input"
      placeholder="活动 ID，例如 1"
      @input="emitUrl"
    />
    <el-input
      v-else-if="targetType === 'SEARCH'"
      v-model="keyword"
      class="target-input"
      placeholder="搜索关键词，例如 工业巡检"
      @input="emitUrl"
    />
    <el-input
      v-else-if="targetType === 'CUSTOM'"
      v-model="customUrl"
      class="target-input"
      placeholder="/pages/category/index?type=RENT"
      @input="emitUrl"
    />

    <div class="link-help">
      用户点击后打开的页面。当前生成：<span>{{ modelValue || '不跳转' }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{ modelValue?: string }>()
const emit = defineEmits<{ (e: 'update:modelValue', value: string): void }>()

const targetType = ref('NONE')
const activityId = ref('')
const keyword = ref('')
const customUrl = ref('')

watch(() => props.modelValue, parseUrl, { immediate: true })

function parseUrl(url?: string) {
  const value = url || ''
  if (!value) {
    targetType.value = 'NONE'
  } else if (value === '/pages/category/index?type=RENT') {
    targetType.value = 'RENT'
  } else if (value === '/pages/category/index?type=BUY') {
    targetType.value = 'BUY'
  } else if (value === '/pages/category/index?type=SOFTWARE') {
    targetType.value = 'SOFTWARE'
  } else if (value === '/pages/member/index' || value === '/pages/membership-center/index') {
    targetType.value = 'MEMBER'
  } else if (value.startsWith('/pages/activity-detail/index?id=')) {
    targetType.value = 'ACTIVITY'
    activityId.value = value.split('id=')[1] || ''
  } else if (value.startsWith('/pages/category/index?keyword=')) {
    targetType.value = 'SEARCH'
    keyword.value = decodeURIComponent(value.split('keyword=')[1] || '')
  } else {
    targetType.value = 'CUSTOM'
    customUrl.value = value
  }
}

function emitUrl() {
  const nextUrl = buildUrl()
  if (nextUrl !== (props.modelValue || '')) {
    emit('update:modelValue', nextUrl)
  }
}

function buildUrl() {
  if (targetType.value === 'RENT') return '/pages/category/index?type=RENT'
  if (targetType.value === 'BUY') return '/pages/category/index?type=BUY'
  if (targetType.value === 'SOFTWARE') return '/pages/category/index?type=SOFTWARE'
  if (targetType.value === 'MEMBER') return '/pages/membership-center/index'
  if (targetType.value === 'ACTIVITY') return activityId.value ? `/pages/activity-detail/index?id=${activityId.value}` : ''
  if (targetType.value === 'SEARCH') return keyword.value ? `/pages/category/index?keyword=${encodeURIComponent(keyword.value)}` : ''
  if (targetType.value === 'CUSTOM') return customUrl.value
  return ''
}
</script>

<style scoped>
.link-picker { width: 100%; }
.target-select { width: 220px; margin-right: 10px; }
.target-input { width: 320px; }
.link-help { margin-top: 8px; color: #909399; font-size: 12px; line-height: 1.5; }
.link-help span { color: #606266; }
</style>
