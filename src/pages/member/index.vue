<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getMembership } from '@/services/member'

const membership = ref({ level: '', progress: 0, rights: [] as Array<{ name: string; value: string }> })

onLoad(async () => {
  membership.value = await getMembership()
})
</script>

<template>
  <view class="member-page">
    <view class="hero-card">
      <text class="level">{{ membership.level }}</text>
      <text class="desc">星球卡权益看板</text>
      <view class="progress"><view :style="{ width: `${membership.progress}%` }" /></view>
    </view>
    <view class="rights-grid">
      <view v-for="right in membership.rights" :key="right.name" class="right-card">
        <text>{{ right.value }}</text>
        <text>{{ right.name }}</text>
      </view>
    </view>
    <button>升级星球卡</button>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.member-page {
  min-height: 100vh;
  padding: 32rpx;
}

.hero-card {
  padding: 42rpx;
  border-radius: 36rpx;
  background: linear-gradient(135deg, #111a35, #2f7cf6);
  color: #fff;
}

.level,
.desc {
  display: block;
}

.level {
  font-size: 42rpx;
  font-weight: 900;
}

.desc {
  margin-top: 12rpx;
  font-size: 26rpx;
}

.progress {
  height: 16rpx;
  margin-top: 42rpx;
  border-radius: 999rpx;
  background: rgba(255,255,255,.24);
  overflow: hidden;
}

.progress view {
  height: 100%;
  border-radius: inherit;
  background: #fff;
}

.rights-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 22rpx;
  margin-top: 28rpx;
}

.right-card {
  padding: 28rpx 12rpx;
  border-radius: $radius-md;
  background: #fff;
  box-shadow: $shadow-card;
  text-align: center;
}

.right-card text {
  display: block;
}

.right-card text:first-child {
  color: $color-primary;
  font-size: 30rpx;
  font-weight: 900;
}

.right-card text:last-child {
  margin-top: 10rpx;
  color: $color-subtext;
  font-size: 22rpx;
}

button {
  height: 88rpx;
  margin-top: 44rpx;
  border-radius: 999rpx;
  background: $color-primary;
  color: #fff;
  font-size: 30rpx;
  font-weight: 800;
}
</style>
