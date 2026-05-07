<template>
  <view class="page editor-page">
    <view class="panel">
      <text class="title">发布动态</text>
      <textarea placeholder="分享机器人活动现场、技能包玩法或门店案例" maxlength="500" />
      <view class="upload-box">图片/视频上传将走 COS 直传凭证</view>
      <view class="audit-tip">提交后展示审核中、已通过或未通过原因，不在前端判断违规。</view>
      <button>提交审核</button>
    </view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.editor-page { min-height: 100vh; padding: 32rpx; }
.panel { padding: 32rpx; border-radius: $radius-md; background: #fff; box-shadow: $shadow-card; }
.title { color: $color-text; font-size: 34rpx; font-weight: 900; }
textarea { width: 100%; min-height: 240rpx; margin-top: 28rpx; padding: 24rpx; border-radius: 20rpx; background: #f7f9fc; color: $color-text; font-size: 28rpx; }
.upload-box { height: 180rpx; margin-top: 22rpx; display: flex; align-items: center; justify-content: center; border-radius: 20rpx; background: $color-primary-soft; color: $color-primary; font-size: 26rpx; }
.audit-tip { margin-top: 24rpx; color: $color-subtext; font-size: 24rpx; line-height: 36rpx; }
button { height: 88rpx; margin-top: 36rpx; border-radius: 999rpx; background: $color-primary; color: #fff; font-size: 30rpx; font-weight: 800; }
</style>
