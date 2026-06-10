<template>
  <view class="page edit-profile-page">
    <!-- Nav Bar -->
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-back" @tap="goBack">
        <image class="back-icon" src="/static/icons/back.svg" mode="aspectFit" />
      </view>
      <text class="nav-title">编辑资料</text>
      <view class="nav-placeholder"></view>
    </view>

    <view class="content">
      <!-- Avatar -->
      <view class="form-card">
        <text class="form-label">头像</text>
        <button class="avatar-picker" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
          <image class="avatar-preview" :src="avatarUrl || '/static/images/default-avatar.svg'" mode="aspectFill" />
          <text class="change-hint">点击更换头像</text>
        </button>
      </view>

      <!-- Nickname -->
      <view class="form-card">
        <text class="form-label">昵称</text>
        <input
          class="nickname-input"
          type="nickname"
          v-model="nickname"
          placeholder="请输入昵称"
          maxlength="32"
          @blur="onNicknameBlur"
        />
        <text class="input-hint">点击输入框后，微信将提供昵称选择器</text>
      </view>

      <!-- Save -->
      <view class="save-btn" :class="{ disabled: !canSave || saving }" @tap="handleSave">
        <text>{{ saving ? "保存中..." : "保存" }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { uploadAvatar, updateUserMe } from "../../services/user";
import { useSessionStore } from "../../stores/session";

const session = useSessionStore();
const statusBarHeight = ref(0);
const avatarUrl = ref("");
const nickname = ref("");
const originalAvatar = ref("");
const originalNickname = ref("");
const saving = ref(false);
const avatarChanged = ref(false);

const canSave = computed(() => {
  return nickname.value.trim().length > 0 && !saving.value;
});

onLoad(() => {
  const systemInfo = uni.getWindowInfo();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;
  if (session.user) {
    avatarUrl.value = session.user.avatarUrl || "";
    nickname.value = session.user.nickname || "";
    originalAvatar.value = session.user.avatarUrl || "";
    originalNickname.value = session.user.nickname || "";
  }
});

function onChooseAvatar(e: any) {
  const { avatarUrl: tempPath } = e.detail;
  if (!tempPath) return;
  avatarUrl.value = tempPath;
  avatarChanged.value = true;
}

function onNicknameBlur(e: any) {
  nickname.value = e.detail?.value || nickname.value;
}

async function handleSave() {
  if (!canSave.value) return;
  saving.value = true;

  try {
    let finalAvatarUrl = originalAvatar.value;

    // Upload new avatar if changed
    if (avatarChanged.value && avatarUrl.value) {
      const result = await uploadAvatar(avatarUrl.value);
      finalAvatarUrl = result.url;
    }

    // Only send changed fields
    const payload: { nickname?: string; avatarUrl?: string } = {};
    if (nickname.value.trim() !== originalNickname.value) {
      payload.nickname = nickname.value.trim();
    }
    if (finalAvatarUrl !== originalAvatar.value) {
      payload.avatarUrl = finalAvatarUrl;
    }

    if (Object.keys(payload).length === 0) {
      uni.showToast({ title: "未做任何修改", icon: "none" });
      saving.value = false;
      return;
    }

    const updated = await updateUserMe(payload);
    // Sync back to session store
    session.user = {
      ...session.user!,
      nickname: updated.nickname,
      avatarUrl: updated.avatarUrl,
    };

    uni.showToast({ title: "保存成功", icon: "none" });
    setTimeout(() => {
      const pages = getCurrentPages();
      if (pages.length > 1) {
        uni.navigateBack();
      } else {
        uni.switchTab({ url: "/pages/profile/index" });
      }
    }, 1000);
  } catch (e: any) {
    uni.showToast({ title: e.message || "保存失败", icon: "none" });
  } finally {
    saving.value = false;
  }
}

function goBack() {
  const pages = getCurrentPages();
  if (pages.length > 1) {
    uni.navigateBack();
  } else {
    uni.switchTab({ url: "/pages/profile/index" });
  }
}
</script>

<style scoped lang="scss">
.edit-profile-page {
  min-height: 100vh;
  background: #f5f6f8;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 28rpx;
  background: #fff;
}

.nav-back {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  width: 36rpx;
  height: 36rpx;
}

.nav-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
}

.nav-placeholder {
  width: 64rpx;
}

.content {
  padding: 24rpx 28rpx;
}

.form-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
}

.form-label {
  font-size: 28rpx;
  font-weight: 700;
  color: #111827;
  margin-bottom: 24rpx;
  display: block;
}

.avatar-picker {
  display: flex;
  align-items: center;
  gap: 24rpx;
  background: none;
  border: none;
  padding: 0;
  margin: 0;
  line-height: 1;
}

.avatar-picker::after {
  border: none;
}

.avatar-preview {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: #e2e8f0;
  flex-shrink: 0;
}

.change-hint {
  font-size: 26rpx;
  color: #6b7280;
}

.nickname-input {
  width: 100%;
  height: 80rpx;
  border: 2rpx solid #e5e7eb;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #111827;
  background: #f9fafb;
  box-sizing: border-box;
}

.input-hint {
  font-size: 22rpx;
  color: #9ca3af;
  margin-top: 16rpx;
  display: block;
}

.save-btn {
  margin-top: 48rpx;
  height: 88rpx;
  border-radius: 999rpx;
  background: #0a4bfe;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 700;
}

.save-btn.disabled {
  opacity: 0.4;
}
</style>
