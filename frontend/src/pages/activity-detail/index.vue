<template>
  <view class="activity-page">
    <view class="nav">
      <view class="back" @tap="back"><image class="back-icon" src="/static/icons/back.svg" mode="aspectFit" /></view>
      <text>活动详情</text>
    </view>
    <image v-if="activity?.coverUrl" class="cover" :src="activity.coverUrl" mode="aspectFill" />
    <view v-if="activity" class="card">
      <text v-if="activity.tag" class="tag">{{ activity.tag }}</text>
      <text class="title">{{ activity.title }}</text>
      <text v-if="activity.subtitle" class="subtitle">{{ activity.subtitle }}</text>
      <text class="date">{{ formatDate(activity.startAt) }} - {{ formatDate(activity.endAt) }}</text>
      <text v-if="activity.description" class="description">{{ activity.description }}</text>
      <view v-for="section in sections" :key="section.title" class="section">
        <text class="section-title">{{ section.title }}</text>
        <text class="section-content">{{ section.content }}</text>
      </view>
      <view v-if="activity.linkUrl" class="action" @tap="goAction">立即参与</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { getActivityDetail } from "../../services/activity";
import type { ActivityDetail } from "../../services/activity";

const activity = ref<ActivityDetail | null>(null);
const sections = ref<{ title: string; content: string }[]>([]);

onLoad(async (options) => {
  const id = Number(options?.id || 0);
  if (!id) return;
  try {
    activity.value = await getActivityDetail(id);
    sections.value = parseSections(activity.value.contentJson);
  } catch {
    uni.showToast({ title: "活动不存在或已结束", icon: "none" });
  }
});

function parseSections(contentJson?: string) {
  if (!contentJson) return [];
  try {
    return JSON.parse(contentJson) as { title: string; content: string }[];
  } catch {
    return [];
  }
}
function formatDate(value?: string) { return value ? value.slice(0, 10) : "长期有效"; }
function back() { uni.navigateBack({ delta: 1 }); }
function goAction() {
  if (activity.value?.linkUrl) uni.navigateTo({ url: activity.value.linkUrl });
}
</script>

<style scoped lang="scss">
.activity-page { min-height: 100vh; background: #f5f6f8; padding-bottom: 40rpx; }
.nav { height: 112rpx; padding: 70rpx 28rpx 0; box-sizing: border-box; background: #fff; display: flex; align-items: center; justify-content: center; font-size: 32rpx; font-weight: 700; position: relative; }
.back { position: absolute; left: 24rpx; bottom: 0; width: 62rpx; height: 62rpx; display: flex; align-items: center; justify-content: center; }
.cover { width: 100%; height: 420rpx; display: block; }
.card { margin: -34rpx 24rpx 0; padding: 30rpx 28rpx; border-radius: 26rpx; background: #fff; position: relative; }
.tag { display: inline-block; padding: 6rpx 16rpx; color: #0a4bfe; background: #e0edff; font-size: 22rpx; border-radius: 20rpx; }
.title { display: block; margin-top: 20rpx; color: #111827; font-size: 37rpx; font-weight: 700; }
.subtitle { display: block; margin-top: 13rpx; color: #374151; font-size: 27rpx; line-height: 1.55; }
.date { display: block; margin-top: 18rpx; color: #9ca3af; font-size: 23rpx; }
.description { display: block; margin-top: 26rpx; line-height: 1.75; color: #4b5563; font-size: 26rpx; }
.section { margin-top: 30rpx; padding-top: 26rpx; border-top: 1rpx solid #eef2f7; }
.section-title { display: block; color: #111827; font-size: 29rpx; font-weight: 700; }
.section-content { display: block; margin-top: 12rpx; color: #4b5563; font-size: 25rpx; line-height: 1.75; }
.action { margin-top: 36rpx; padding: 23rpx 0; text-align: center; border-radius: 42rpx; background: #0a4bfe; color: #fff; font-size: 28rpx; font-weight: 600; }
</style>
