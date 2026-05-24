<template>
  <view class="bottom-nav">
    <view
      v-for="item in items"
      :key="item.path"
      class="bottom-nav__item"
      :class="{ active: current === item.key }"
      @tap="go(item.path)"
    >
      <image class="bottom-nav__icon" :src="current === item.key ? item.activeIcon : item.icon" mode="aspectFit" />
      <text class="bottom-nav__label">{{ item.label }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{ current: "home" | "category" | "community" | "profile" }>();

const items = [
  { key: "home", label: "首页", path: "/pages/home/index", icon: "/static/tabbar/home.png", activeIcon: "/static/tabbar/home-active.png" },
  { key: "category", label: "分类", path: "/pages/category/index", icon: "/static/tabbar/category.png", activeIcon: "/static/tabbar/category-active.png" },
  { key: "community", label: "社区", path: "/pages/community/index", icon: "/static/tabbar/community.png", activeIcon: "/static/tabbar/community-active.png" },
  { key: "profile", label: "我的", path: "/pages/profile/index", icon: "/static/tabbar/profile.png", activeIcon: "/static/tabbar/profile-active.png" },
] as const;

function go(url: string) {
  uni.reLaunch({ url });
}
</script>

<style scoped lang="scss">
.bottom-nav {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 40;
  height: calc(108rpx + env(safe-area-inset-bottom));
  padding: 14rpx 28rpx calc(18rpx + env(safe-area-inset-bottom));
  display: flex;
  justify-content: space-around;
  background: #fff;
  border-top: 1rpx solid #eef0f3;
  box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.04);
}

.bottom-nav__item {
  width: 110rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  color: #9ca3af;
}

.bottom-nav__item.active {
  color: #0a4bfe;
  font-weight: 800;
}

.bottom-nav__label {
  font-size: 20rpx;
}

.bottom-nav__icon {
  width: 42rpx;
  height: 42rpx;
  display: block;
}
</style>
