<script setup lang="ts">
type Hotspot = {
  className: string
  url?: string
  label: string
}

const hotspots: Hotspot[] = [
  { className: 'hotspot--settings', label: '设置' },
  { className: 'hotspot--service', label: '客服与设置' },
  { className: 'hotspot--member-rights', url: '/pages/member/index', label: '查看会员权益' },
  { className: 'hotspot--growth', url: '/pages/member/index', label: '成长中心' },
  { className: 'hotspot--orders', url: '/pages/order/list', label: '我的订单' },
  { className: 'hotspot--wallet', url: '/pages/wallet/index', label: '钱包' },
  { className: 'hotspot--member', url: '/pages/member/index', label: '会员中心' },
  { className: 'hotspot--assets', url: '/pages/asset/list', label: '分身资产' },
  { className: 'hotspot--trusteeship', url: '/pages/asset/trusteeship', label: '托管收益' },
  { className: 'hotspot--distribution', url: '/pages/distribution/home', label: '分销中心' },
  { className: 'hotspot--invite', url: '/pages/distribution/invite', label: '邀请好友' },
  { className: 'hotspot--asset-wallet', url: '/pages/wallet/index', label: '进入钱包' },
  { className: 'hotspot--asset-all', url: '/pages/asset/list', label: '查看全部分身资产' }
]

function go(item: Hotspot) {
  if (!item.url) return
  uni.navigateTo({ url: item.url })
}
</script>

<template>
  <view class="mine-page">
    <image class="mine-page__image" src="/static/images/mine/mine-page.png" mode="widthFix" />
    <button
      v-for="item in hotspots"
      :key="item.className"
      class="hotspot"
      :class="item.className"
      :aria-label="item.label"
      @tap="go(item)"
    />
  </view>
</template>

<style scoped lang="scss">
.mine-page {
  position: relative;
  width: 750rpx;
  min-height: 1331.91rpx;
  margin: 0 auto;
  overflow: hidden;
  background: #ffffff;
}

.mine-page__image {
  display: block;
  width: 750rpx;
  height: auto;
}

.hotspot {
  position: absolute;
  z-index: 2;
  border: 0;
  opacity: 0;
}

.hotspot--settings {
  top: 76rpx;
  right: 100rpx;
  width: 74rpx;
  height: 91rpx;
}

.hotspot--service {
  top: 76rpx;
  right: 26rpx;
  width: 74rpx;
  height: 91rpx;
}

.hotspot--member-rights {
  top: 285rpx;
  right: 38rpx;
  width: 175rpx;
  height: 54rpx;
}

.hotspot--growth {
  top: 377rpx;
  right: 42rpx;
  width: 129rpx;
  height: 52rpx;
}

.hotspot--orders,
.hotspot--wallet,
.hotspot--member,
.hotspot--assets,
.hotspot--trusteeship,
.hotspot--distribution,
.hotspot--invite {
  width: 132rpx;
  height: 108rpx;
}

.hotspot--orders {
  top: 638rpx;
  left: 83rpx;
}

.hotspot--wallet {
  top: 638rpx;
  left: 252rpx;
}

.hotspot--member {
  top: 638rpx;
  left: 421rpx;
}

.hotspot--assets {
  top: 638rpx;
  left: 588rpx;
}

.hotspot--trusteeship {
  top: 749rpx;
  left: 83rpx;
}

.hotspot--distribution {
  top: 749rpx;
  left: 252rpx;
}

.hotspot--invite {
  top: 749rpx;
  left: 421rpx;
}

.hotspot--asset-wallet {
  top: 928rpx;
  right: 38rpx;
  width: 105rpx;
  height: 44rpx;
}

.hotspot--asset-all {
  top: 1107rpx;
  right: 38rpx;
  width: 105rpx;
  height: 44rpx;
}
</style>
