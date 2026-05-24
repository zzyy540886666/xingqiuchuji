<template>
  <view class="status-row" :class="{ light }">
    <text>{{ time }}</text>
    <view class="status-icons">
      <image class="status-icon" :src="light ? '/static/icons/status-signal-light.svg' : '/static/icons/status-signal.svg'" mode="aspectFit" />
      <image class="status-icon" :src="light ? '/static/icons/status-wifi-light.svg' : '/static/icons/status-wifi.svg'" mode="aspectFit" />
      <image class="status-icon battery" :src="light ? '/static/icons/status-battery-light.svg' : '/static/icons/status-battery.svg'" mode="aspectFit" />
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ light?: boolean; time?: string }>(), {
  light: false,
  time: "9:41",
});
</script>

<style scoped lang="scss">
.light {
  color: #fff;
}

.status-icon {
  width: 30rpx;
  height: 30rpx;
  display: block;
}

.battery {
  width: 42rpx;
}
</style>
