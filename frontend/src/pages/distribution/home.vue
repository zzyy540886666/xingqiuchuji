<template>
  <view class="page">
    <view class="header-card">
      <text class="title">分销中心</text>
      <view class="stats">
        <view class="stat"><text class="stat-value">{{ info.teamStats.level1Count }}</text><text class="stat-label">一级成员</text></view>
        <view class="stat"><text class="stat-value">{{ info.teamStats.level2Count }}</text><text class="stat-label">二级成员</text></view>
        <view class="stat"><text class="stat-value">{{ totalTeam }}</text><text class="stat-label">团队总数</text></view>
      </view>
    </view>
    <view class="menu-list">
      <view class="menu-item" @tap="go('/pages/distribution/invite')"><text>推广邀请</text><text class="arrow">></text></view>
      <view class="menu-item" @tap="go('/pages/distribution/team')"><text>我的团队</text><text class="arrow">></text></view>
      <view class="menu-item" @tap="go('/pages/distribution/commission')"><text>佣金明细</text><text class="arrow">></text></view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { getDistributionMe, type DistributionInfo } from "../../services/distribution";

const info = ref<DistributionInfo>({ inviteCode: "", inviteUrl: "", teamStats: { level1Count: 0, level2Count: 0 } });
const totalTeam = computed(() => info.value.teamStats.level1Count + info.value.teamStats.level2Count);

onShow(async () => { try { info.value = await getDistributionMe(); } catch {} });

function go(url: string) { uni.navigateTo({ url }); }
</script>

<style scoped lang="scss">
.header-card { background: linear-gradient(135deg, #0a4bfe, #6366f1); margin: 28rpx; border-radius: 24rpx; padding: 40rpx; color: #fff; }
.title { font-size: 34rpx; font-weight: 800; display: block; margin-bottom: 28rpx; }
.stats { display: flex; justify-content: space-around; }
.stat { text-align: center; }
.stat-value { display: block; font-size: 36rpx; font-weight: 800; }
.stat-label { display: block; font-size: 22rpx; opacity: 0.8; margin-top: 6rpx; }
.menu-list { background: #fff; margin: 0 28rpx; border-radius: 20rpx; }
.menu-item { display: flex; justify-content: space-between; align-items: center; padding: 32rpx 28rpx; border-bottom: 1rpx solid #f9fafb; font-size: 28rpx; }
.arrow { color: #9ca3af; }
</style>
