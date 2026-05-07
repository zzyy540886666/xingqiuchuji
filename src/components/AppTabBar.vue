<script setup lang="ts">
/**
 * 底部导航与 docs/frontend/FE-01 一致：首页、分类/场景、社区、我的。
 * 图标为 SVG（无 emoji），见 .cursor/rules/xingqiu-dev-principles.mdc
 */
export type TabKey = 'home' | 'catalog' | 'community' | 'mine'

const props = defineProps<{ current: TabKey }>()

const tabs: ReadonlyArray<{ key: TabKey; text: string; icon: string; url: string }> = [
  { key: 'home', text: '首页', icon: '/static/icons/home.svg', url: '/pages/home/index' },
  { key: 'catalog', text: '分类', icon: '/static/icons/catalog.svg', url: '/pages/catalog/index' },
  { key: 'community', text: '社区', icon: '/static/icons/community.svg', url: '/pages/community/index' },
  { key: 'mine', text: '我的', icon: '/static/icons/mine.svg', url: '/pages/mine/index' }
]

function go(url: string, key: TabKey) {
  if (key === props.current) return
  uni.reLaunch({ url })
}
</script>

<template>
  <view class="tabbar">
    <view v-for="tab in tabs" :key="tab.key" class="tabbar__item" :class="{ active: current === tab.key }" @tap="go(tab.url, tab.key)">
      <image class="tabbar__icon" :src="tab.icon" mode="aspectFit" />
      <text>{{ tab.text }}</text>
    </view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 20;
  height: calc(96rpx + env(safe-area-inset-bottom));
  padding-bottom: env(safe-area-inset-bottom);
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  background: #ffffff;
  border-top: 1rpx solid #f0f0f0;
}

.tabbar__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  color: #999999;
  font-size: 20rpx;
}

.tabbar__item.active {
  color: #0052d9;
}

.tabbar__icon {
  width: 44rpx;
  height: 44rpx;
}
</style>
