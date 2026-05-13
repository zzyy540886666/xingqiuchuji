<template>
  <view class="page">
    <view class="card">
      <view class="title">发起报修</view>
      <input v-model="faultType" class="input" placeholder="故障类型（后台可配置）" />
      <input v-model="address" class="input" placeholder="报修地址" />
      <textarea v-model="description" class="textarea" placeholder="请描述故障现象" />
      <button class="btn" :disabled="submitting" @click="submitForm">
        {{ submitting ? '提交中...' : '提交工单' }}
      </button>
      <view v-if="error" class="error">{{ error }}</view>
      <view v-if="successMessage" class="success">{{ successMessage }}</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { createRepairOrder } from '@/services/repair'

const faultType = ref('')
const address = ref('')
const description = ref('')
const submitting = ref(false)
const error = ref('')
const successMessage = ref('')

async function submitForm() {
  if (!faultType.value || !address.value || !description.value) {
    error.value = '请完整填写故障类型、地址和描述'
    return
  }
  error.value = ''
  successMessage.value = ''
  submitting.value = true
  try {
    const result = await createRepairOrder({
      faultType: faultType.value,
      address: address.value,
      description: description.value,
      imageUrls: []
    })
    successMessage.value = `提交成功，工单号：${result.id}`
  } catch (e) {
    error.value = e instanceof Error ? e.message : '提交失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page { padding: 24rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; }
.title { font-size: 32rpx; font-weight: 600; margin-bottom: 20rpx; }
.input, .textarea { width: 100%; background: #f5f7fb; border-radius: 12rpx; padding: 18rpx; margin-bottom: 16rpx; font-size: 28rpx; box-sizing: border-box; }
.textarea { min-height: 180rpx; }
.btn { background: #2f6bff; color: #fff; border-radius: 12rpx; font-size: 28rpx; margin-top: 12rpx; }
.error { color: #d93025; margin-top: 12rpx; font-size: 24rpx; }
.success { color: #198754; margin-top: 12rpx; font-size: 24rpx; }
</style>
