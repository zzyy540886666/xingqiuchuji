<template>
  <view class="activity-page">
    <view class="nav">
      <view class="back" @tap="back"><image class="back-icon" src="/static/icons/back.svg" mode="aspectFit" /></view>
      <text>活动详情</text>
    </view>
    <image v-if="detailImageUrl" class="cover" :src="detailImageUrl" mode="aspectFill" />
    <view v-if="activity" class="card">
      <text v-if="activity.tag" class="tag">{{ activity.tag }}</text>
      <text class="title">{{ activity.title }}</text>
      <text v-if="activity.subtitle" class="subtitle">{{ activity.subtitle }}</text>
      <text class="date">{{ formatDate(activity.startAt) }} - {{ formatDate(activity.endAt) }}</text>
      <text v-if="activity.description" class="description">{{ activity.description }}</text>
      <view v-if="activity.videoUrl" class="video-entry" @tap="previewVideo">
        <text class="video-play">▶</text>
        <text>查看活动视频</text>
      </view>
      <view v-for="section in sections" :key="section.title || section.content || section.imageUrl" class="section">
        <image v-if="section.imageUrl" class="section-image" :src="section.imageUrl" mode="aspectFill" />
        <text class="section-title">{{ section.title }}</text>
        <text class="section-content">{{ section.content }}</text>
      </view>
      <view v-if="activity.linkUrl" class="action" @tap="goAction">立即参与</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { getActivityDetail } from "../../services/activity";
import type { ActivityDetail } from "../../services/activity";
import { navigateToPage } from "../../utils/navigation";

const activity = ref<ActivityDetail | null>(null);
const allSections = ref<ActivitySection[]>([]);
const sections = ref<ActivitySection[]>([]);
const detailImageUrl = computed(() => {
  const imageSection = allSections.value.find((section) => section.kind === "DETAIL_IMAGE" || (section.imageUrl && !section.title && !section.content));
  return imageSection?.imageUrl || "";
});

onLoad(async (options) => {
  const id = Number(options?.id || 0);
  if (!id) return;
  try {
    activity.value = await getActivityDetail(id);
    allSections.value = parseSections(activity.value.contentJson);
    sections.value = allSections.value.filter((section) => section.kind !== "DETAIL_IMAGE" && (section.title || section.content || !section.imageUrl));
  } catch {
    uni.showToast({ title: "活动不存在或已结束", icon: "none" });
  }
});

interface ActivitySection {
  title?: string;
  content?: string;
  imageUrl?: string;
  kind?: string;
}

function parseSections(contentJson?: string) {
  if (!contentJson) return [];
  try {
    return JSON.parse(contentJson) as ActivitySection[];
  } catch {
    return [];
  }
}
function formatDate(value?: string) { return value ? value.slice(0, 10) : "长期有效"; }
function back() { uni.navigateBack({ delta: 1 }); }
function goAction() {
  if (activity.value?.linkUrl) navigateToPage(activity.value.linkUrl);
}
function previewVideo() {
  if (activity.value?.videoUrl) uni.previewMedia({ sources: [{ url: activity.value.videoUrl, type: "video" }] });
}
</script>

<style scoped lang="scss">
.activity-page { min-height: 100vh; background: #f5f6f8; padding-bottom: 40rpx; }
.nav { height: 112rpx; padding: 70rpx 28rpx 0; box-sizing: border-box; background: #fff; display: flex; align-items: center; justify-content: center; font-size: 32rpx; font-weight: 700; position: relative; }
.back { position: absolute; left: 24rpx; bottom: 0; width: 62rpx; height: 62rpx; display: flex; align-items: center; justify-content: center; }
.cover { width: 100%; height: 420rpx; display: block; }
.card { margin: 24rpx 24rpx 0; padding: 30rpx 28rpx; border-radius: 26rpx; background: #fff; position: relative; }
.cover + .card { margin-top: -34rpx; }
.tag { display: inline-block; padding: 6rpx 16rpx; color: #0a4bfe; background: #e0edff; font-size: 22rpx; border-radius: 20rpx; }
.title { display: block; margin-top: 20rpx; color: #111827; font-size: 37rpx; font-weight: 700; }
.subtitle { display: block; margin-top: 13rpx; color: #374151; font-size: 27rpx; line-height: 1.55; }
.date { display: block; margin-top: 18rpx; color: #9ca3af; font-size: 23rpx; }
.description { display: block; margin-top: 26rpx; line-height: 1.75; color: #4b5563; font-size: 26rpx; }
.video-entry { margin-top: 24rpx; height: 84rpx; padding: 0 24rpx; border-radius: 18rpx; background: #111827; color: #fff; display: flex; align-items: center; gap: 14rpx; font-size: 26rpx; }
.video-play { width: 42rpx; height: 42rpx; border-radius: 50%; background: rgba(255,255,255,.18); display: flex; align-items: center; justify-content: center; font-size: 20rpx; }
.section { margin-top: 30rpx; padding-top: 26rpx; border-top: 1rpx solid #eef2f7; }
.section-image { width: 100%; height: 300rpx; display: block; margin-bottom: 18rpx; border-radius: 18rpx; }
.section-title { display: block; color: #111827; font-size: 29rpx; font-weight: 700; }
.section-content { display: block; margin-top: 12rpx; color: #4b5563; font-size: 25rpx; line-height: 1.75; }
.action { margin-top: 36rpx; padding: 23rpx 0; text-align: center; border-radius: 42rpx; background: #0a4bfe; color: #fff; font-size: 28rpx; font-weight: 600; }
</style>
