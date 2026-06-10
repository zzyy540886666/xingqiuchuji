<template>
  <view class="page create-post-page">
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="back-btn" @tap="goBack">
        <image class="back-icon" src="/static/icons/back-light.svg" mode="aspectFit" />
      </view>
      <text class="nav-title">发布帖子</text>
      <view class="submit-text" :class="{ disabled: submitting }" @tap="submit">发布</view>
    </view>

    <Skeleton v-if="loading" :rows="4" />
    <ErrorRetry v-else-if="error" @retry="loadCircles" />
    <view v-else class="form">
      <view class="field">
        <text class="field-label">发布到</text>
        <picker :range="circleNames" :value="circleIndex" @change="onCircleChange">
          <view class="picker-value">
            <text>{{ selectedCircleName || '请选择圈子' }}</text>
            <image class="chevron" src="/static/icons/chevron-right.svg" mode="aspectFit" />
          </view>
        </picker>
      </view>

      <view class="field">
        <input class="title-input" v-model="title" maxlength="200" placeholder="请输入标题" />
      </view>

      <view class="field content-field">
        <textarea class="content-input" v-model="content" maxlength="5000" placeholder="分享设备使用、托管收益或服务体验" />
        <text class="counter">{{ content.length }}/5000</text>
      </view>

      <view class="notice">
        <text>发布后进入审核，审核通过后会展示在社区信息流。</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import Skeleton from "../../components/PageSkeleton.vue";
import ErrorRetry from "../../components/ErrorRetry.vue";
import { createPost, getCircles, type Circle } from "../../services/community";

const statusBarHeight = ref(0);
const systemInfo = uni.getWindowInfo();
statusBarHeight.value = systemInfo.statusBarHeight || 0;

const circles = ref<Circle[]>([]);
const circleIndex = ref(0);
const title = ref("");
const content = ref("");
const loading = ref(true);
const error = ref(false);
const submitting = ref(false);

const circleNames = computed(() => circles.value.map((item) => item.name));
const selectedCircle = computed(() => circles.value[circleIndex.value]);
const selectedCircleName = computed(() => selectedCircle.value?.name || "");

onLoad(() => {
  loadCircles();
});

async function loadCircles() {
  loading.value = true;
  error.value = false;
  try {
    circles.value = await getCircles();
  } catch {
    circles.value = [];
    error.value = true;
  } finally {
    loading.value = false;
  }
}

function onCircleChange(event: any) {
  circleIndex.value = Number(event.detail.value || 0);
}

async function submit() {
  if (submitting.value) return;
  if (!selectedCircle.value) {
    uni.showToast({ title: "请选择圈子", icon: "none" });
    return;
  }
  const cleanTitle = title.value.trim();
  const cleanContent = content.value.trim();
  if (!cleanTitle) {
    uni.showToast({ title: "请输入标题", icon: "none" });
    return;
  }
  if (!cleanContent) {
    uni.showToast({ title: "请输入内容", icon: "none" });
    return;
  }

  submitting.value = true;
  try {
    await createPost({
      circleId: selectedCircle.value.id,
      title: cleanTitle,
      content: cleanContent,
      mediaUrls: [],
    });
    uni.showToast({ title: "已提交审核", icon: "none" });
    setTimeout(() => uni.navigateBack({ delta: 1 }), 600);
  } catch (e: any) {
    uni.showToast({ title: e.message || "发布失败", icon: "none" });
  } finally {
    submitting.value = false;
  }
}

function goBack() {
  uni.navigateBack({ delta: 1 });
}
</script>

<style scoped lang="scss">
.create-post-page { min-height: 100vh; background: #f5f6f8; }
.nav-bar { min-height: 88rpx; padding: 0 28rpx; display: flex; align-items: center; justify-content: space-between; background: #fff; border-bottom: 1rpx solid #eef2f7; }
.back-btn { width: 56rpx; height: 56rpx; border-radius: 50%; background: #f3f4f6; display: flex; align-items: center; justify-content: center; }
.back-icon { width: 28rpx; height: 28rpx; }
.nav-title { font-size: 32rpx; font-weight: 700; color: #111827; }
.submit-text { min-width: 56rpx; text-align: right; font-size: 28rpx; font-weight: 700; color: #0a4bfe; }
.submit-text.disabled { color: #94a3b8; }
.form { padding: 28rpx; }
.field { background: #fff; border-radius: 18rpx; padding: 26rpx; margin-bottom: 20rpx; }
.field-label { display: block; margin-bottom: 16rpx; font-size: 24rpx; color: #64748b; }
.picker-value { min-height: 44rpx; display: flex; align-items: center; justify-content: space-between; font-size: 28rpx; color: #111827; }
.chevron { width: 28rpx; height: 28rpx; }
.title-input { height: 64rpx; font-size: 32rpx; color: #111827; }
.content-field { min-height: 360rpx; }
.content-input { width: 100%; min-height: 300rpx; font-size: 28rpx; line-height: 1.55; color: #111827; }
.counter { display: block; text-align: right; font-size: 22rpx; color: #94a3b8; }
.notice { padding: 0 6rpx; font-size: 24rpx; line-height: 1.6; color: #64748b; }
</style>
