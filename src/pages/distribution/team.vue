<template>
  <view class="page team-page">
    <view class="summary"><text>团队看板</text><text>一级 36 人 · 二级 128 人</text></view>
    <view class="member-row"><text>上海活动服务商</text><text>一级伙伴</text></view>
    <view class="member-row"><text>智慧零售门店</text><text>二级伙伴</text></view>
    <view class="member-row"><text>机器人运营顾问</text><text>一级伙伴</text></view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.team-page { min-height: 100vh; padding: 32rpx; }
.summary { padding: 42rpx; border-radius: 36rpx; background: linear-gradient(135deg, #2f7cf6, #8ec0ff); color: #fff; }
.summary text { display: block; }
.summary text:first-child { font-size: 40rpx; font-weight: 900; }
.summary text:last-child { margin-top: 14rpx; font-size: 26rpx; }
.member-row { display: flex; justify-content: space-between; margin-top: 22rpx; padding: 28rpx; border-radius: $radius-md; background: #fff; box-shadow: $shadow-card; color: $color-text; font-size: 28rpx; }
.member-row text:last-child { color: $color-primary; font-size: 24rpx; }
</style>
