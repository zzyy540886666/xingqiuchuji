<template>
  <view class="page safe-bottom">
    <template v-if="pageLoading">
      <Skeleton variant="repair-create" />
    </template>
    <template v-else>
    <view class="card">
      <text class="title">报修信息</text>
      <view class="form-item">
        <text class="label">故障类型</text>
        <picker :range="faultTypes" @change="(e: any) => form.faultType = faultTypes[e.detail.value]">
          <text class="picker-text">{{ form.faultType || '请选择' }}</text>
        </picker>
      </view>
      <view class="form-item">
        <text class="label">故障描述</text>
        <textarea class="textarea" v-model="form.description" placeholder="请描述故障情况" maxlength="500" />
      </view>
      <view class="form-item">
        <text class="label">现场照片</text>
        <view class="image-list">
          <view v-for="(img, idx) in form.images" :key="idx" class="img-wrap">
            <image :src="img" mode="aspectFill" class="preview-img" />
            <view class="img-remove" @tap="form.images.splice(idx, 1)"><image class="img-icon" src="/static/icons/close.svg" mode="aspectFit" /></view>
          </view>
          <view v-if="form.images.length < 9" class="img-add" @tap="chooseImage"><image class="img-add-icon" src="/static/icons/plus.svg" mode="aspectFit" /></view>
        </view>
      </view>
      <view class="form-item">
        <text class="label">位置信息</text>
        <input class="input" v-model="form.location" placeholder="选填，设备所在位置" />
      </view>
    </view>
    <view class="submit-btn" :class="{ disabled: !canSubmit || submitting }" @tap="handleSubmit">
      {{ submitting ? "提交中..." : "提交报修" }}
    </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from "vue";
import { createWorkOrder, uploadRepairImage } from "../../services/repair";
import Skeleton from "../../components/PageSkeleton.vue";

const pageLoading = ref(true);

onMounted(() => {
  setTimeout(() => {
    pageLoading.value = false;
  }, 400);
});

const faultTypes = ["机械故障", "电气故障", "软件异常", "外观损坏", "其他"];
const form = reactive({ faultType: "", description: "", images: [] as string[], location: "" });
const submitting = ref(false);
const canSubmit = computed(() => form.faultType && form.description.length >= 5);

function chooseImage() {
  uni.chooseImage({
    count: 9 - form.images.length,
    success: async (res) => {
      uni.showLoading({ title: "上传中" });
      try {
        const urls = await Promise.all(res.tempFilePaths.map(uploadRepairImage));
        form.images.push(...urls);
      } catch (error: any) {
        uni.showToast({ title: error.message || "图片上传失败", icon: "none" });
      } finally {
        uni.hideLoading();
      }
    },
  });
}

async function handleSubmit() {
  if (!canSubmit.value || submitting.value) return;
  submitting.value = true;
  try {
    await createWorkOrder({ faultType: form.faultType, description: form.description, images: form.images, location: form.location || undefined });
    uni.showToast({ title: "报修已提交", icon: "none" });
    setTimeout(() => uni.navigateBack(), 1500);
  } catch (e: any) {
    uni.showToast({ title: e.message || "提交失败", icon: "none" });
  } finally { submitting.value = false; }
}
</script>

<style scoped lang="scss">
.card { background: #fff; margin: 28rpx; border-radius: 20rpx; padding: 28rpx; }
.title { display: block; font-size: 30rpx; font-weight: 700; margin-bottom: 24rpx; }
.form-item { margin-bottom: 28rpx; }
.label { display: block; font-size: 26rpx; color: #374151; margin-bottom: 12rpx; }
.picker-text { font-size: 26rpx; color: #111827; padding: 16rpx; background: #f9fafb; border-radius: 12rpx; display: block; }
.textarea { width: 100%; height: 200rpx; font-size: 26rpx; padding: 16rpx; background: #f9fafb; border-radius: 12rpx; }
.input { font-size: 26rpx; padding: 16rpx; background: #f9fafb; border-radius: 12rpx; }
.image-list { display: flex; flex-wrap: wrap; gap: 16rpx; }
.img-wrap { position: relative; width: 160rpx; height: 160rpx; }
.preview-img { width: 100%; height: 100%; border-radius: 12rpx; }
.img-remove { position: absolute; top: -8rpx; right: -8rpx; width: 36rpx; height: 36rpx; border-radius: 50%; background: #ef4444; color: #fff; font-size: 20rpx; display: flex; align-items: center; justify-content: center; }
.img-icon { width: 22rpx; height: 22rpx; display: block; }
.img-add { width: 160rpx; height: 160rpx; border: 2rpx dashed #d1d5db; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; background: #f8fafc; }
.img-add-icon { width: 44rpx; height: 44rpx; display: block; background: #94a3b8; border-radius: 50%; padding: 8rpx; }
.submit-btn { margin: 28rpx; height: 88rpx; border-radius: 999rpx; background: #0a4bfe; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 30rpx; font-weight: 700; }
.submit-btn.disabled { opacity: 0.5; }
</style>
