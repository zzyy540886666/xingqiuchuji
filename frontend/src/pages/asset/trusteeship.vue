<template>
  <view class="page">
    <view class="card">
      <text class="title">选择可租时段</text>
      <text class="hint">选择您希望将设备上架出租的时间段</text>
      <view class="slot-row">
        <picker mode="date" @change="(e: any) => timeSlot.start = e.detail.value"><text class="picker-text">{{ timeSlot.start || '开始日期' }}</text></picker>
        <text class="slot-sep">至</text>
        <picker mode="date" @change="(e: any) => timeSlot.end = e.detail.value"><text class="picker-text">{{ timeSlot.end || '结束日期' }}</text></picker>
      </view>
      <view class="rate-row">
        <text class="hint">平台统一定价（元/天）</text>
        <view class="rate-display">{{ platformDailyYuan }}</view>
        <text class="rate-note">托管价格由平台根据设备型号统一定价，提交后由平台审核确认。</text>
      </view>
    </view>
    <view class="card">
      <text class="note">上架后平台将根据设备型号统一定价，您提交的可用时段将由平台审核确认。</text>
    </view>
    <view class="submit-btn" :class="{ disabled: !canSubmit || submitting }" @tap="handleSubmit">
      {{ submitting ? "提交中..." : "确认上架" }}
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { submitTrusteeship } from "../../services/asset";
import { useConfigStore } from "../../stores/config";

let assetId = "";
const timeSlot = ref({ start: "", end: "" });
const submitting = ref(false);
const configStore = useConfigStore();

const platformDailyMinor = computed(() => {
  const raw = (configStore.config as Record<string, unknown>)?.trusteeshipDailyRateMinor;
  const val = Number(raw);
  return Number.isFinite(val) ? val : 300; // default 300分 = 3.00元/天
});
const platformDailyYuan = computed(() => (platformDailyMinor.value / 100).toFixed(2));
const canSubmit = computed(() => timeSlot.value.start && timeSlot.value.end && timeSlot.value.start < timeSlot.value.end);

onLoad((options) => { assetId = options?.id || ""; });

async function handleSubmit() {
  if (!canSubmit.value || submitting.value) return;
  submitting.value = true;
  try {
    await submitTrusteeship(assetId, {
      startTime: `${timeSlot.value.start}T00:00:00`,
      endTime: `${timeSlot.value.end}T23:59:59`,
    });
    uni.showToast({ title: "上架申请已提交", icon: "none" });
    setTimeout(() => uni.navigateBack(), 1500);
  } catch (e: any) {
    uni.showToast({ title: e.message || "提交失败", icon: "none" });
  } finally { submitting.value = false; }
}
</script>

<style scoped lang="scss">
.card { background: #fff; margin: 28rpx; border-radius: 20rpx; padding: 28rpx; }
.title { display: block; font-size: 30rpx; font-weight: 700; margin-bottom: 12rpx; }
.hint { display: block; font-size: 24rpx; color: #6b7280; margin-bottom: 24rpx; }
.slot-row { display: flex; align-items: center; gap: 12rpx; margin-bottom: 16rpx; }
.picker-text { font-size: 26rpx; color: #374151; padding: 12rpx 20rpx; background: #f3f4f6; border-radius: 12rpx; }
.slot-sep { font-size: 24rpx; color: #9ca3af; }
.remove-btn { font-size: 22rpx; color: #ef4444; }
.add-slot { font-size: 26rpx; color: #0a4bfe; margin-top: 12rpx; }
.note { font-size: 24rpx; color: #6b7280; }
.rate-row { margin-top: 24rpx; }
.rate-display { margin: 12rpx 0 16rpx 0; padding: 16rpx 20rpx; font-size: 28rpx; font-weight: 700; color: #0a4bfe; border-radius: 12rpx; background: #ebf0ff; }
.rate-note { display: block; font-size: 22rpx; color: #9ca3af; }
.submit-btn { margin: 48rpx 28rpx; height: 88rpx; border-radius: 999rpx; background: #0a4bfe; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 30rpx; font-weight: 700; }
.submit-btn.disabled { opacity: 0.5; }
</style>
