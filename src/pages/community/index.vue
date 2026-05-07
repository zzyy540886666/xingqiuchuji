<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import AppTabBar from '@/components/AppTabBar.vue'
import { listPosts } from '@/services/community'

const tabs = ['推荐', '关注', '话题']
const active = ref('推荐')
const posts = ref<Array<{ id: string; title: string; status: string; likes: number }>>([])

onLoad(async () => {
  posts.value = await listPosts()
})

function goEditor() {
  uni.navigateTo({ url: '/pages/community/editor' })
}
</script>

<template>
  <view class="community-page">
    <view class="tabs">
      <text v-for="tab in tabs" :key="tab" :class="{ active: active === tab }" @tap="active = tab">{{ tab }}</text>
    </view>
    <view class="composer">
      <text>分享机器人活动现场、技能包玩法或门店案例</text>
      <button type="default" @tap="goEditor">发布动态</button>
    </view>
    <view v-for="post in posts" :key="post.id" class="post-card">
      <view class="post-card__cover" />
      <text class="post-card__title">{{ post.title }}</text>
      <view class="post-card__meta">
        <text>{{ post.status }}</text>
        <text>{{ post.likes }} 人喜欢</text>
      </view>
    </view>

    <view class="page-bottom" />
    <AppTabBar current="community" />
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.community-page {
  min-height: 100vh;
  padding: 28rpx 32rpx calc(120rpx + env(safe-area-inset-bottom));
}

.page-bottom {
  height: 8rpx;
}

.tabs {
  display: flex;
  gap: 42rpx;
  margin-bottom: 28rpx;
}

.tabs text {
  color: $color-subtext;
  font-size: 30rpx;
}

.tabs text.active {
  color: $color-primary;
  font-weight: 900;
}

.composer,
.post-card {
  border-radius: $radius-md;
  background: #fff;
  box-shadow: $shadow-card;
}

.composer {
  padding: 28rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: $color-subtext;
  font-size: 24rpx;
}

.composer button {
  width: 156rpx;
  height: 64rpx;
  border-radius: 999rpx;
  background: $color-primary;
  color: #fff;
  font-size: 24rpx;
}

.post-card {
  overflow: hidden;
  margin-top: 24rpx;
}

.post-card__cover {
  height: 260rpx;
  background: linear-gradient(135deg, #e8f1ff, #2f7cf6);
}

.post-card__title {
  display: block;
  padding: 24rpx 24rpx 0;
  color: $color-text;
  font-size: 30rpx;
  font-weight: 800;
}

.post-card__meta {
  display: flex;
  justify-content: space-between;
  padding: 18rpx 24rpx 24rpx;
  color: $color-subtext;
  font-size: 24rpx;
}
</style>
