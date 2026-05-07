<template>
  <view class="page trusteeship-page">
    <view class="panel">
      <text class="title">托管上架</text>
      <view class="row"><text>可租时段</text><text>按平台营业时间</text></view>
      <view class="row"><text>平台定价</text><text>只读展示</text></view>
      <view class="row"><text>审核状态</text><text>提交后以后端返回为准</text></view>
      <button>提交托管申请</button>
    </view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.trusteeship-page { min-height: 100vh; padding: 32rpx; }
.panel { padding: 32rpx; border-radius: $radius-md; background: #fff; box-shadow: $shadow-card; }
.title { color: $color-text; font-size: 34rpx; font-weight: 900; }
.row { display: flex; justify-content: space-between; padding: 30rpx 0; border-bottom: 1rpx solid $color-border; color: $color-text; font-size: 28rpx; }
.row text:last-child { color: $color-subtext; }
button { height: 88rpx; margin-top: 36rpx; border-radius: 999rpx; background: $color-primary; color: #fff; font-size: 30rpx; font-weight: 800; }
</style>
