<template>
  <view class="distribution-page">
    <view class="hero-card">
      <text class="title">城市合伙人与分销中心</text>
      <text class="desc">生成邀请路径、查看团队人数与佣金账单。</text>
      <button>生成邀请海报</button>
    </view>
    <view class="stats-grid">
      <view class="stat-card"><text>36</text><text>一级伙伴</text></view>
      <view class="stat-card"><text>128</text><text>二级伙伴</text></view>
      <view class="stat-card"><text>¥8,600</text><text>可提现佣金</text></view>
    </view>
    <view class="panel">
      <text class="panel-title">佣金账单</text>
      <view class="bill-row"><text>待结算</text><text>维权期后 T+7</text></view>
      <view class="bill-row"><text>可提现</text><text>¥8,600</text></view>
      <view class="bill-row"><text>已提现</text><text>¥12,400</text></view>
    </view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.distribution-page {
  min-height: 100vh;
  padding: 32rpx;
}

.hero-card {
  padding: 42rpx;
  border-radius: 36rpx;
  background: linear-gradient(135deg, #111a35, #2f7cf6);
  color: #fff;
}

.title,
.desc {
  display: block;
}

.title {
  font-size: 40rpx;
  font-weight: 900;
}

.desc {
  margin-top: 14rpx;
  font-size: 26rpx;
  line-height: 38rpx;
}

.hero-card button {
  width: 220rpx;
  height: 70rpx;
  margin-top: 34rpx;
  border-radius: 999rpx;
  background: #fff;
  color: $color-primary;
  font-size: 26rpx;
  font-weight: 800;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18rpx;
  margin-top: 28rpx;
}

.stat-card,
.panel {
  border-radius: $radius-md;
  background: #fff;
  box-shadow: $shadow-card;
}

.stat-card {
  padding: 26rpx 8rpx;
  text-align: center;
}

.stat-card text {
  display: block;
}

.stat-card text:first-child {
  color: $color-primary;
  font-size: 30rpx;
  font-weight: 900;
}

.stat-card text:last-child {
  margin-top: 8rpx;
  color: $color-subtext;
  font-size: 22rpx;
}

.panel {
  margin-top: 28rpx;
  padding: 30rpx;
}

.panel-title {
  color: $color-text;
  font-size: 32rpx;
  font-weight: 900;
}

.bill-row {
  display: flex;
  justify-content: space-between;
  padding: 28rpx 0;
  border-bottom: 1rpx solid $color-border;
  color: $color-text;
  font-size: 28rpx;
}

.bill-row text:last-child {
  color: $color-subtext;
  font-size: 24rpx;
}
</style>
