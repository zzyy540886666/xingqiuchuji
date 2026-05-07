<script setup lang="ts">
import AppTabBar from '@/components/AppTabBar.vue'
import FeatureGrid from '@/components/FeatureGrid.vue'
import { useSessionStore } from '@/stores/session'
import { maskMobile } from '@/utils/format'

const session = useSessionStore()

const orderItems = [
  { title: '待付款', color: '#7c66ff', url: '/pages/order/list' },
  { title: '服务中', color: '#39d6a0', url: '/pages/order/list' },
  { title: '待评价', color: '#ffc24b', url: '/pages/order/list' },
  { title: '已取消', color: '#f25f6b', url: '/pages/order/list' },
  { title: '退款售后', color: '#ff9a46', url: '/pages/order/list' }
]

const featureItems = [
  { title: '评价中心', color: '#f25f6b' },
  { title: '我的地址', color: '#7986ff' },
  { title: '合作申请', color: '#7c66ff', url: '/pages/distribution/home' },
  { title: '供应商邀请码', color: '#39d6a0' },
  { title: '城市合伙人', color: '#5ca9ff', url: '/pages/distribution/home' },
  { title: '设置', color: '#8b9bf7' }
]

function goOrders() {
  uni.navigateTo({ url: '/pages/order/list' })
}
</script>

<template>
  <view class="page mine-page">
    <view class="profile">
      <view class="avatar" />
      <view class="profile__info">
        <text class="profile__mobile">{{ session.user ? maskMobile(session.user.mobile) : '未登录' }}</text>
        <text class="profile__level">{{ session.user?.level || '登录后查看权益' }}</text>
      </view>
      <view class="support" />
    </view>

    <view class="coupon-row">
      <view class="coupon-card">
        <text>优惠券</text>
      </view>
      <view class="coupon-card">
        <text>我要开票</text>
      </view>
    </view>

    <view class="panel">
      <view class="panel__header">
        <text>我的订单</text>
        <text @tap="goOrders">全部</text>
      </view>
      <FeatureGrid :items="orderItems" />
    </view>

    <view class="panel">
      <view class="panel__header">
        <text>常用功能</text>
      </view>
      <FeatureGrid :items="featureItems" />
    </view>

    <AppTabBar current="mine" />
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.mine-page {
  min-height: 100vh;
  padding: 92rpx 32rpx 140rpx;
}

.profile {
  display: flex;
  align-items: center;
  gap: 28rpx;
}

.avatar {
  width: 104rpx;
  height: 104rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #dceeff, #2f7cf6);
}

.profile__info {
  flex: 1;
}

.profile__mobile,
.profile__level {
  display: block;
}

.profile__mobile {
  color: $color-text;
  font-size: 38rpx;
  font-weight: 800;
}

.profile__level {
  margin-top: 10rpx;
  color: $color-subtext;
  font-size: 24rpx;
}

.support {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  border: 4rpx solid $color-text;
}

.coupon-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24rpx;
  margin-top: 54rpx;
}

.coupon-card,
.panel {
  border-radius: $radius-md;
  background: #fff;
  box-shadow: $shadow-card;
}

.coupon-card {
  height: 112rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $color-text;
  font-size: 28rpx;
}

.panel {
  margin-top: 32rpx;
  padding: 28rpx 18rpx 34rpx;
}

.panel__header {
  display: flex;
  justify-content: space-between;
  margin: 0 0 34rpx 0;
  padding: 0 10rpx;
  color: $color-text;
  font-size: 32rpx;
  font-weight: 800;
}

.panel__header text:last-child {
  color: $color-subtext;
  font-size: 24rpx;
  font-weight: 400;
}
</style>
