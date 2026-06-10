<template>
  <view class="page">
    <view class="poster-section">
      <view class="poster-label">预览海报</view>
      <canvas
        canvas-id="posterCanvas"
        id="posterCanvas"
        class="poster-canvas"
        :style="{ width: canvasWidth + 'px', height: canvasHeight + 'px' }"
      />
      <view class="poster-actions">
        <view class="btn primary" @tap="savePoster">保存到相册</view>
        <view class="btn outline" @tap="sharePoster">分享给好友</view>
      </view>
    </view>
    <view class="loading-mask" v-if="loading">
      <view class="loading-spinner" />
      <text class="loading-text">海报生成中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onReady } from "@dcloudio/uni-app";
import { getDistributionMe } from "../../services/distribution";

const loading = ref(true);
const canvasWidth = 300;
const canvasHeight = 500;
const inviteUrl = ref("");
const inviteCode = ref("");

onReady(async () => {
  try {
    const info = await getDistributionMe();
    inviteCode.value = info.inviteCode;
    inviteUrl.value = info.inviteUrl;
    await drawPoster();
  } catch {
    uni.showToast({ title: "获取邀请信息失败", icon: "none" });
  } finally {
    loading.value = false;
  }
});

function drawPoster(): Promise<void> {
  return new Promise((resolve) => {
    const ctx = uni.createCanvasContext("posterCanvas");

    // Background gradient
    const bgGradient = ctx.createLinearGradient(0, 0, 0, canvasHeight);
    bgGradient.addColorStop(0, "#0a4bfe");
    bgGradient.addColorStop(1, "#6366f1");
    ctx.setFillStyle(bgGradient);
    ctx.fillRect(0, 0, canvasWidth, canvasHeight);

    // White card
    ctx.setFillStyle("#ffffff");
    const cardX = 20;
    const cardY = 60;
    const cardW = canvasWidth - 40;
    const cardH = canvasHeight - 120;
    ctx.fillRect(cardX, cardY, cardW, cardH);

    // App name
    ctx.setFillStyle("#0a4bfe");
    ctx.setFontSize(22);
    ctx.setTextAlign("center");
    ctx.fillText("星球出机", canvasWidth / 2, cardY + 40);

    // Subtitle
    ctx.setFillStyle("#6b7280");
    ctx.setFontSize(14);
    ctx.fillText("设备托管租赁平台", canvasWidth / 2, cardY + 70);

    // Divider
    ctx.setStrokeStyle("#e5e7eb");
    ctx.setLineWidth(1);
    ctx.moveTo(cardX + 20, cardY + 90);
    ctx.lineTo(cardX + cardW - 20, cardY + 90);
    ctx.stroke();

    // Invite code label
    ctx.setFillStyle("#374151");
    ctx.setFontSize(14);
    ctx.fillText("我的邀请码", canvasWidth / 2, cardY + 130);

    // Invite code value
    ctx.setFillStyle("#0a4bfe");
    ctx.setFontSize(28);
    ctx.fillText(inviteCode.value, canvasWidth / 2, cardY + 170);

    // CTA text
    ctx.setFillStyle("#6b7280");
    ctx.setFontSize(14);
    ctx.fillText("复制邀请码注册，享专属优惠", canvasWidth / 2, cardY + 210);

    const qrX = (canvasWidth - 140) / 2;
    const qrY = cardY + 240;

    ctx.setFillStyle("#f9fafb");
    ctx.fillRect(qrX - 5, qrY - 5, 150, 150);

    ctx.setFillStyle("#0a4bfe");
    ctx.setFontSize(16);
    ctx.fillText("邀请码", canvasWidth / 2, qrY + 36);
    ctx.setFontSize(24);
    ctx.fillText(inviteCode.value, canvasWidth / 2, qrY + 76);
    ctx.setFillStyle("#6b7280");
    ctx.setFontSize(10);
    wrapCanvasText(ctx, inviteUrl.value, canvasWidth / 2, qrY + 112, 120, 16);

    // Footer text
    ctx.setFillStyle("#9ca3af");
    ctx.setFontSize(11);
    ctx.fillText("长按保存海报，分享给好友", canvasWidth / 2, canvasHeight - 30);

    ctx.draw(false, () => {
      resolve();
    });
  });
}

function wrapCanvasText(
  ctx: ReturnType<typeof uni.createCanvasContext>,
  text: string,
  x: number,
  y: number,
  maxWidth: number,
  lineHeight: number
) {
  let line = "";
  let currentY = y;
  for (const char of text) {
    const testLine = line + char;
    if (ctx.measureText(testLine).width > maxWidth && line) {
      ctx.fillText(line, x, currentY);
      line = char;
      currentY += lineHeight;
      if (currentY > y + lineHeight * 2) {
        break;
      }
    } else {
      line = testLine;
    }
  }
  if (line) ctx.fillText(line, x, currentY);
}

function savePoster() {
  uni.canvasToTempFilePath({
    canvasId: "posterCanvas",
    success(res) {
      uni.saveImageToPhotosAlbum({
        filePath: res.tempFilePath,
        success() {
          uni.showToast({ title: "已保存到相册", icon: "success" });
        },
        fail() {
          uni.showToast({ title: "保存失败，请检查相册权限", icon: "none" });
        },
      });
    },
    fail() {
      uni.showToast({ title: "生成图片失败", icon: "none" });
    },
  });
}

function sharePoster() {
  uni.canvasToTempFilePath({
    canvasId: "posterCanvas",
    success(res) {
      uni.share({
        provider: "weixin",
        type: 0,
        title: "加入星球出机，享专属优惠",
        imageUrl: res.tempFilePath,
        path: `/pages/loading/index?inviteCode=${inviteCode.value}`,
        success() {
          uni.showToast({ title: "已分享", icon: "success" });
        },
        fail() {
          uni.showToast({ title: "分享失败", icon: "none" });
        },
      });
    },
    fail() {
      uni.showToast({ title: "生成图片失败", icon: "none" });
    },
  });
}
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: #f5f6f8;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32rpx 0;
}

.poster-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.poster-label {
  font-size: 28rpx;
  color: #6b7280;
  margin-bottom: 24rpx;
}

.poster-canvas {
  border-radius: 16rpx;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.12);
  width: 300px;
  height: 500px;
}

.poster-actions {
  display: flex;
  gap: 24rpx;
  margin-top: 40rpx;
  width: 100%;
  padding: 0 40rpx;
  box-sizing: border-box;
}

.btn {
  flex: 1;
  height: 80rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 600;
}

.btn.primary {
  background: #0a4bfe;
  color: #fff;
}

.btn.outline {
  border: 2rpx solid #0a4bfe;
  color: #0a4bfe;
  background: #fff;
}

.loading-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  color: #fff;
  font-size: 28rpx;
  margin-top: 20rpx;
}
</style>
