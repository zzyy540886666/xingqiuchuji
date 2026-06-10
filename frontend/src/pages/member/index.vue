<template>
  <view class="page member-page">
    <!-- Top Space Background -->
    <view class="space-bg">
      <view class="bg-glow"></view>
    </view>

    <view class="member-head">
      <!-- Navigation -->
      <view class="member-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
        <view class="back-button" @tap="back">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M15 18L9 12L15 6" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </view>
        <text class="nav-title">会员中心</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <template v-if="loading">
      <view class="member-head">
        <Skeleton variant="member" />
      </view>
    </template>
    <template v-else>
      <view class="member-head">
        <!-- User Info -->
      <view class="user-info">
        <image class="avatar" :src="user?.avatarUrl || '/static/images/default-avatar.svg'" mode="aspectFill" />
        <view class="info-content">
          <view class="name-row">
            <text class="name">{{ user?.nickname || '星球玩家小白' }}</text>
            <view class="badge">
              <svg class="badge-icon" width="10" height="10" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="url(#paint0_linear)"/>
                <defs>
                  <linearGradient id="paint0_linear" x1="12" y1="2" x2="12" y2="21" gradientUnits="userSpaceOnUse">
                    <stop stop-color="#FFF5E1"/>
                    <stop offset="1" stop-color="#FFD17F"/>
                  </linearGradient>
                </defs>
              </svg>
              <text>Lv.{{ membership?.level || 1 }} {{ membership?.isNative ? '原住民' : '原住民' }}</text>
            </view>
          </view>
          <view class="id-row">
            <text>ID: {{ user?.id || 'XQWJXB20250001' }}</text>
            <view class="copy-wrap" @tap="copyId(user?.id || 'XQWJXB20250001')">
              <svg class="copy-icon" width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="9" y="9" width="13" height="13" rx="2" stroke="rgba(255,255,255,0.7)" stroke-width="2" stroke-linejoin="round"/>
                <path d="M5 15H4C2.89543 15 2 14.1046 2 13V4C2 2.89543 2.89543 2 4 2H13C14.1046 2 15 2.89543 15 4V5" stroke="rgba(255,255,255,0.7)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </view>
          </view>
        </view>
        
        <!-- Planet Card Graphic -->
        <view class="planet-card-graphic">
          <view class="card-inner">
            <view class="card-ring"></view>
            <view class="card-planet"></view>
            <text class="card-text">PLANET CARD</text>
          </view>
        </view>
      </view>

      <!-- Level Card -->
      <view class="level-card">
        <view class="level-header">
          <text class="level-title">Lv.{{ membership?.level || 1 }} 原住民</text>
          <text class="level-progress-text">320/1000</text>
        </view>
        <view class="progress-bar">
          <view class="progress-inner" style="width: 32%"></view>
        </view>
        <view class="level-sub-row">
          <text class="level-sub-text">再获得680经验升级 Lv.2 探索者</text>
          <view class="growth-btn" @tap="showGrowthRules">成长中心 {{ '>' }}</view>
        </view>

        <view class="timeline">
          <view class="timeline-line"></view>
          <view class="node">
            <view class="dot"></view>
            <text class="node-lv">Lv.0</text>
            <text class="node-name">观察者</text>
          </view>
          <view class="node active">
            <view class="dot-active"><view class="dot-inner"></view></view>
            <text class="node-lv">Lv.1</text>
            <text class="node-name">原住民</text>
          </view>
          <view class="node">
            <view class="dot"></view>
            <text class="node-lv">Lv.2</text>
            <text class="node-name">探索者</text>
          </view>
          <view class="node">
            <view class="dot"></view>
            <text class="node-lv">Lv.3</text>
            <text class="node-name">开拓者</text>
          </view>
          <view class="node last-node">
            <svg class="star-node-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L14.12 9.88L22 12L14.12 14.12L12 22L9.88 14.12L2 12L9.88 9.88L12 2Z" fill="url(#paint_star)"/>
              <defs>
                <linearGradient id="paint_star" x1="12" y1="2" x2="12" y2="22" gradientUnits="userSpaceOnUse">
                  <stop stop-color="#FFF2D1"/>
                  <stop offset="1" stop-color="#E2B76D"/>
                </linearGradient>
              </defs>
            </svg>
            <text class="node-name star-name">星球合伙人</text>
          </view>
        </view>

        <view class="level-footer">
          <view class="condition">
            <svg class="c-icon" width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="rgba(255,255,255,0.6)" stroke-width="2"/>
              <path d="M12 16V12M12 8H12.01" stroke="rgba(255,255,255,0.6)" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <text>升级条件：累计消费满 ¥10,000 或开通年费星球卡</text>
          </view>
          <text class="rules-link" @tap="showGrowthRules">查看等级规则 {{ '>' }}</text>
        </view>
      </view>
    </view>

    <!-- 会员权益 -->
    <text class="section-title">会员权益</text>
    <view class="benefit-grid">
        <view class="benefit-item">
          <svg class="b-icon" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 5L32 10V20C32 27.5 20 35 20 35C20 35 8 27.5 8 20V10L20 5Z" stroke="#3B82F6" stroke-width="2" stroke-linejoin="round"/>
            <path d="M15 19L19 23L26 15" stroke="#3B82F6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <view class="b-value"><text class="val blue">2</text><text class="unit blue">次</text></view>
          <text class="b-desc">剩余免押次数</text>
        </view>
        <view class="benefit-item">
          <svg class="b-icon" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M25.8 11.2L11.2 25.8C10.2 26.8 10.2 28.5 11.2 29.5C12.2 30.5 13.8 30.5 14.8 29.5L29.5 14.8M25.8 11.2C24.1 9.5 21.4 9.5 19.7 11.2L16.4 14.5M25.8 11.2C27.5 12.9 27.5 15.6 25.8 17.3L22.5 20.6" stroke="#3B82F6" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="24" cy="15" r="1.5" fill="#3B82F6"/>
          </svg>
          <view class="b-value"><text class="val blue">1</text><text class="unit blue">次</text></view>
          <text class="b-desc">剩余维保次数</text>
        </view>
        <view class="benefit-item">
          <svg class="b-icon" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 12H20L28 20C29 21 29 22.5 28 23.5L23.5 28C22.5 29 21 29 20 28L12 20V12Z" stroke="#3B82F6" stroke-width="2" stroke-linejoin="round"/>
            <circle cx="16" cy="16" r="1.5" fill="#3B82F6"/>
          </svg>
          <view class="b-value"><text class="val blue">9.5</text><text class="unit blue">折</text></view>
          <text class="b-desc">配件专属折扣</text>
        </view>
        <view class="benefit-item">
          <svg class="b-icon" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="20" cy="10" r="3.5" stroke="#3B82F6" stroke-width="2"/><circle cx="12" cy="27" r="3.5" stroke="#3B82F6" stroke-width="2"/><circle cx="28" cy="27" r="3.5" stroke="#3B82F6" stroke-width="2"/><path d="M18 13.5L14 23.5M22 13.5L26 23.5M15.5 27H24.5" stroke="#3B82F6" stroke-width="2" stroke-linecap="round"/></svg>
          <view class="b-value"><text class="val blue">2</text><text class="unit blue">级</text></view>
          <text class="b-desc">分润层级</text>
        </view>
        <view class="benefit-item">
          <svg class="b-icon" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 12C26.63 12 32 14 32 16.5C32 19 26.63 21 20 21C13.37 21 8 19 8 16.5C8 14 13.37 12 20 12Z" stroke="#3B82F6" stroke-width="2"/>
            <path d="M8 16.5V21.5C8 24 13.37 26 20 26C26.63 26 32 24 32 21.5V16.5" stroke="#3B82F6" stroke-width="2"/>
            <path d="M8 21.5V26.5C8 29 13.37 31 20 31C26.63 31 32 29 32 26.5V21.5" stroke="#3B82F6" stroke-width="2"/>
          </svg>
          <view class="b-value"><text class="val blue">5,680</text></view>
          <text class="b-desc">光年币余额 {{ '>' }}</text>
        </view>
        <view class="benefit-item">
          <svg class="b-icon" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="9" y="15" width="22" height="17" rx="1" stroke="#3B82F6" stroke-width="2"/><path d="M15 15V11C15 9.067 16.567 7.5 18.5 7.5H21.5C23.433 7.5 25 9.067 25 11V15M20 15V32M9 20H31" stroke="#3B82F6" stroke-width="2"/></svg>
          <view class="b-value"><text class="val black">新物种体验</text></view>
          <text class="b-desc">优先体验资格</text>
        </view>
        <view class="benefit-item">
          <svg class="b-icon" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M20 8C14.4772 8 10 12.4772 10 18V24C10 26.2091 11.7909 28 14 28H15V18H10M20 8C25.5228 8 30 12.4772 30 18V24C30 26.2091 28.2091 28 26 28H25V18H30M20 8V12M15 32C15 34.2091 17.2386 36 20 36C22.7614 36 25 34.2091 25 32V28H15V32Z" stroke="#3B82F6" stroke-width="2"/></svg>
          <view class="b-value"><text class="val black">专属客服</text></view>
          <text class="b-desc">7x12小时服务</text>
        </view>
        <view class="benefit-item">
          <svg class="b-icon" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M20 5L30 10V18.5C30 25.5 20 33.5 20 33.5C20 33.5 10 25.5 10 18.5V10L20 5Z" stroke="#3B82F6" stroke-width="2" stroke-linejoin="round"/><path d="M20 11L21.8 16H27L22.8 19.2L24.2 24.2L20 21.5L15.8 24.2L17.2 19.2L13 16H18.2L20 11Z" fill="#3B82F6"/></svg>
          <view class="b-value"><text class="val black">活动特权</text></view>
          <text class="b-desc">专属活动参与权</text>
        </view>
      </view>
    <!-- 星球卡 -->
    <view class="planet-section card">
      <view class="section-title-row">
        <view class="title-group"><text class="section-title">星球卡</text><text class="planet-hint">· 选择适合你的会员卡</text></view>
        <text class="section-more" @tap="showRights">对比权益 {{ '>' }}</text>
      </view>
      <scroll-view scroll-x class="card-scroll" :show-scrollbar="false">
        <view class="card-flex">
          <!-- Card 1 -->
          <view class="p-card blue-card">
            <view class="p-badge blue-badge">推荐</view>
            <view class="p-top">
              <text class="p-name">月度星球卡</text>
              <text class="p-dur">30天有效期</text>
              <view class="p-planet-bg blue-bg">
                <view class="p-globe blue-globe"></view>
              </view>
            </view>
            <view class="p-bottom">
              <view class="p-list">
                <text>· 2次 免押次数</text>
                <text>· 1次 维保次数</text>
                <text>· 9.5折 配件折扣</text>
                <text>· 分润层级 2级</text>
              </view>
              <view class="p-price"><text class="sym">¥</text><text class="num">99</text></view>
              <view class="p-btn blue-btn">立即开通</view>
            </view>
          </view>
          <!-- Card 2 -->
          <view class="p-card gold-card">
            <view class="p-badge gold-badge">超值</view>
            <view class="p-top">
              <text class="p-name">年度星球卡</text>
              <text class="p-dur">365天有效期</text>
              <view class="p-planet-bg gold-bg">
                <view class="p-globe gold-globe"></view>
              </view>
            </view>
            <view class="p-bottom">
              <view class="p-list">
                <text>· 12次 免押次数 <text class="sub-g">(每月1次)</text></text>
                <text>· 6次 维保次数 <text class="sub-g">(每2月1次)</text></text>
                <text>· 9折 配件折扣</text>
                <text>· 分润层级 2级</text>
              </view>
              <view class="p-price"><text class="sym">¥</text><text class="num">899</text><text class="old-price">¥1299</text></view>
              <view class="p-btn gold-btn">立即开通</view>
            </view>
          </view>
          <!-- Card 3 -->
          <view class="p-card silver-card">
            <view class="p-badge silver-badge">尊享</view>
            <view class="p-top">
              <text class="p-name">终身星球卡</text>
              <text class="p-dur">永久有效</text>
              <view class="p-planet-bg silver-bg">
                <view class="p-globe silver-globe"></view>
              </view>
            </view>
            <view class="p-bottom">
              <view class="p-list">
                <text>· 不限 免押次数</text>
                <text>· 不限 维保次数</text>
                <text>· 8.5折 配件折扣</text>
                <text>· 分润层级 3级</text>
              </view>
              <view class="p-price"><text class="sym">¥</text><text class="num">2999</text></view>
              <view class="p-btn blue-btn">立即开通</view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 权益说明 -->
    <view class="rights card">
      <view class="section-title-row"><text class="section-title">权益说明</text><text class="section-more" @tap="showRights">更多说明 {{ '>' }}</text></view>
      <view class="rights-row">
        <view class="right-item">
          <svg class="r-icon" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 5L32 10V20C32 27.5 20 35 20 35C20 35 8 27.5 8 20V10L20 5Z" stroke="#3B82F6" stroke-width="2.5" stroke-linejoin="round"/>
            <path d="M15 19L19 23L26 15" stroke="#3B82F6" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <text class="r-title">免押特权</text>
          <text class="r-desc">租赁免押，轻松体验</text>
        </view>
        <view class="right-item">
          <svg class="r-icon" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M28 12.5C28 15.5 25.5 18 22.5 18C21.5 18 20.5 17.7 19.8 17.2L10.5 26.5C9.5 27.5 9.5 29 10.5 30C11.5 31 13 31 14 30L23.3 20.7C23.8 21.4 24.6 21.7 25.5 21.7C28.5 21.7 31 19.2 31 16.2C31 13.5 28.5 11.2 25.7 11.2C24.3 11.2 23 11.7 22 12.6L18 8L16.5 9.5L20 14C21 13 22.3 12.5 23.5 12.5C25 12.5 26.5 13.5 26.5 15" stroke="#3B82F6" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <text class="r-title">维保特权</text>
          <text class="r-desc">免费维保，省心省力</text>
        </view>
        <view class="right-item">
          <svg class="r-icon" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M15 8H8V15L23 30L30 23L15 8Z" stroke="#3B82F6" stroke-width="2.5" stroke-linejoin="round"/>
            <circle cx="12.5" cy="12.5" r="1.5" fill="#3B82F6"/>
          </svg>
          <text class="r-title">折扣特权</text>
          <text class="r-desc">配件优惠，持续省钱</text>
        </view>
      </view>
    </view>

    <!-- 常见问题 -->
    <view class="faq card">
      <view class="section-title-row">
        <view class="title-group">
          <text class="section-title">常见问题</text>
          <svg class="faq-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10" stroke="#9CA3AF" stroke-width="2"/>
            <path d="M12 16V16.01M12 12V11C12 9.5 14 9.5 14 8C14 6.5 12 6.5 12 6.5C10 6.5 10 8 10 8" stroke="#9CA3AF" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </view>
        <text class="section-more" @tap="showRights">查看全部 {{ '>' }}</text>
      </view>
      <text class="faq-text">星球卡如何使用？ 免押次数如何计算？ 升级规则是什么？</text>
    </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import Skeleton from "../../components/PageSkeleton.vue";
import { getMembership, getPlanetCards, purchasePlanetCard, type MembershipSummary, type PlanetCardSku } from "../../services/member";
import { useSessionStore } from "../../stores/session";
import { formatAmount } from "../../utils/format";

const statusBarHeight = ref(0);
const loading = ref(false); // set to false for UI testing
{
  const systemInfo = uni.getWindowInfo();
  statusBarHeight.value = systemInfo.statusBarHeight || 44;
}

const membership = ref<MembershipSummary | null>(null);
const planetCards = ref<PlanetCardSku[]>([]);
const session = useSessionStore();
const user = computed(() => session.user);

onShow(async () => {
  const token = uni.getStorageSync("xq_access_token");
  if (!token) return;
  try {
    [membership.value, planetCards.value] = await Promise.all([getMembership(), getPlanetCards()]);
    if (!session.user) await session.fetchUser();
  } catch {}
});

function back() { uni.navigateBack({ delta: 1 }); }
function showGrowthRules() { uni.showModal({ title: "成长规则", content: "规则说明...", showCancel: false }); }
function showRights() { uni.showModal({ title: "权益说明", content: "权益说明...", showCancel: false }); }
function copyId(id: string) {
  uni.setClipboardData({ data: id, success: () => uni.showToast({ title: '已复制', icon: 'none' }) });
}
</script>

<style scoped lang="scss">
.member-page {
  background: #F5F6F8;
  padding-bottom: 40rpx;
  position: relative;
  min-height: 100vh;
}

.space-bg {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  height: 600rpx;
  background: #081230; /* Dark space blue */
  overflow: hidden;
  z-index: 0;
}

.bg-glow {
  position: absolute;
  left: -20%;
  top: -20%;
  width: 140%;
  height: 140%;
  background: radial-gradient(circle at 70% 30%, rgba(22, 119, 255, 0.4) 0%, transparent 60%);
}

.member-head {
  position: relative;
  z-index: 1;
}

.member-nav {
  height: 88rpx;
  padding: 0 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-title {
  color: #fff;
  font-size: 34rpx;
  font-weight: 500;
}

.back-button {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  justify-content: center;
}

.nav-placeholder {
  width: 64rpx;
}

.user-info {
  display: flex;
  padding: 30rpx 40rpx;
  align-items: center;
  position: relative;
}

.avatar {
  width: 110rpx;
  height: 110rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.8);
  margin-right: 24rpx;
}

.info-content {
  flex: 1;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.name {
  color: #fff;
  font-size: 36rpx;
  font-weight: 600;
}

.badge {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.15);
  border: 1rpx solid rgba(255, 255, 255, 0.3);
  padding: 4rpx 12rpx;
  border-radius: 100rpx;
  gap: 8rpx;
}

.badge text {
  color: #fff;
  font-size: 20rpx;
}

.id-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 12rpx;
}

.id-row text {
  color: rgba(255, 255, 255, 0.7);
  font-size: 24rpx;
}

.copy-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32rpx;
  height: 32rpx;
}

.planet-card-graphic {
  position: absolute;
  right: -20rpx;
  top: 10rpx;
  width: 240rpx;
  height: 140rpx;
  transform: rotate(-5deg);
}

.card-inner {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(20, 40, 90, 0.9), rgba(10, 20, 60, 0.9));
  border: 2rpx solid rgba(255, 215, 0, 0.5);
  border-radius: 20rpx;
  box-shadow: 0 10rpx 20rpx rgba(0,0,0,0.3);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.card-ring {
  position: absolute;
  width: 120rpx;
  height: 40rpx;
  border: 2rpx solid rgba(255, 215, 0, 0.8);
  border-radius: 50%;
  transform: rotate(-15deg);
  top: 40rpx;
}

.card-planet {
  width: 50rpx;
  height: 50rpx;
  background: radial-gradient(circle at 30% 30%, #ffd700, #b8860b);
  border-radius: 50%;
  z-index: 1;
  margin-bottom: 8rpx;
}

.card-text {
  color: #ffd700;
  font-size: 16rpx;
  font-weight: 700;
  letter-spacing: 2rpx;
}

.level-card {
  margin: 20rpx 30rpx 0;
  background: linear-gradient(180deg, rgba(26, 44, 88, 0.95), rgba(13, 26, 61, 0.95));
  border: 1rpx solid rgba(255,255,255,0.1);
  border-radius: 24rpx;
  padding: 30rpx;
}

.level-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.level-title {
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
}

.level-progress-text {
  color: rgba(255,255,255,0.7);
  font-size: 24rpx;
}

.progress-bar {
  height: 8rpx;
  background: rgba(255,255,255,0.1);
  border-radius: 4rpx;
  margin-top: 16rpx;
  overflow: hidden;
}

.progress-inner {
  height: 100%;
  background: linear-gradient(90deg, #3B82F6, #60A5FA);
  border-radius: 4rpx;
}

.level-sub-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
}

.level-sub-text {
  color: rgba(255,255,255,0.6);
  font-size: 22rpx;
}

.growth-btn {
  border: 1rpx solid rgba(255,255,255,0.3);
  border-radius: 100rpx;
  padding: 4rpx 16rpx;
  color: #fff;
  font-size: 20rpx;
}

.timeline {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  margin-top: 40rpx;
  padding-bottom: 30rpx;
  border-bottom: 1rpx dashed rgba(255,255,255,0.15);
}

.timeline-line {
  position: absolute;
  top: 10rpx;
  left: 20rpx;
  right: 20rpx;
  height: 2rpx;
  background: rgba(255,255,255,0.2);
  z-index: 0;
}

.node {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.dot {
  width: 12rpx;
  height: 12rpx;
  background: #A1A1AA;
  border-radius: 50%;
  margin-bottom: 12rpx;
  margin-top: 5rpx;
}

.dot-active {
  width: 22rpx;
  height: 22rpx;
  border-radius: 50%;
  border: 2rpx solid #FDE047;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 7rpx;
  background: #0d1a3d;
}

.dot-inner {
  width: 10rpx;
  height: 10rpx;
  background: #FDE047;
  border-radius: 50%;
}

.node-lv {
  color: #fff;
  font-size: 22rpx;
  font-weight: 600;
  margin-bottom: 2rpx;
}

.node-name {
  color: rgba(255,255,255,0.6);
  font-size: 20rpx;
}

.active .node-name {
  color: #fff;
}

.star-name {
  margin-top: -2rpx;
}

.level-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24rpx;
}

.condition {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.condition text {
  color: rgba(255,255,255,0.6);
  font-size: 22rpx;
}

.rules-link {
  color: rgba(255,255,255,0.6);
  font-size: 22rpx;
}

.card {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin: 24rpx 30rpx 0;
}

.section-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.title-group {
  display: flex;
  align-items: center;
}

.section-title {
  color: #111827;
  font-size: 32rpx;
  font-weight: 600;
}

.planet-hint {
  color: #9CA3AF;
  font-size: 24rpx;
  margin-left: 12rpx;
}

.section-more {
  color: #9CA3AF;
  font-size: 24rpx;
}

.benefit-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 30rpx 10rpx;
}

.benefit-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.b-icon {
  width: 50rpx;
  height: 50rpx;
  margin-bottom: 12rpx;
}

.b-value {
  display: flex;
  align-items: baseline;
  margin-bottom: 4rpx;
}

.val {
  font-size: 30rpx;
  font-weight: 600;
}

.val.blue { color: #3B82F6; }
.val.black { color: #111827; font-size: 26rpx; }

.unit {
  font-size: 22rpx;
  margin-left: 4rpx;
}
.unit.blue { color: #3B82F6; }

.b-desc {
  color: #6B7280;
  font-size: 20rpx;
  white-space: nowrap;
}

.card-scroll {
  width: 100%;
}

.card-flex {
  display: flex;
  gap: 20rpx;
  padding-bottom: 10rpx;
}

.p-card {
  width: 280rpx;
  flex-shrink: 0;
  border-radius: 20rpx;
  overflow: hidden;
  position: relative;
  background: #fff;
  border: 1rpx solid #E5E7EB;
}

.p-badge {
  position: absolute;
  top: 0;
  right: 0;
  padding: 4rpx 16rpx;
  font-size: 20rpx;
  color: #fff;
  border-bottom-left-radius: 16rpx;
  z-index: 2;
}

.blue-badge { background: #3B82F6; }
.gold-badge { background: #D97706; }
.silver-badge { background: #64748B; }

.p-top {
  padding: 24rpx;
  position: relative;
  height: 120rpx;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.p-name {
  color: #fff;
  font-size: 28rpx;
  font-weight: 600;
  z-index: 1;
}

.p-dur {
  color: rgba(255,255,255,0.8);
  font-size: 20rpx;
  z-index: 1;
  margin-top: 4rpx;
}

.p-planet-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.blue-bg { background: linear-gradient(135deg, #1E3A8A, #3B82F6); }
.gold-bg { background: linear-gradient(135deg, #92400E, #F59E0B); }
.silver-bg { background: linear-gradient(135deg, #334155, #94A3B8); }

.p-globe {
  position: absolute;
  right: -20rpx;
  bottom: -40rpx;
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
}

.blue-globe { background: radial-gradient(circle at 30% 30%, #60A5FA, #1D4ED8); box-shadow: 0 0 20rpx rgba(59,130,246,0.5); }
.gold-globe { background: radial-gradient(circle at 30% 30%, #FBBF24, #B45309); box-shadow: 0 0 20rpx rgba(245,158,11,0.5); }
.silver-globe { background: radial-gradient(circle at 30% 30%, #CBD5E1, #475569); box-shadow: 0 0 20rpx rgba(148,163,184,0.5); }

.p-bottom {
  padding: 20rpx;
}

.p-list {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-bottom: 24rpx;
}

.p-list text {
  color: #4B5563;
  font-size: 20rpx;
}

.sub-g {
  color: #9CA3AF;
}

.p-price {
  display: flex;
  align-items: baseline;
  margin-bottom: 20rpx;
}

.sym {
  font-size: 24rpx;
  color: #111827;
  font-weight: 600;
}

.num {
  font-size: 36rpx;
  color: #111827;
  font-weight: 700;
  margin-left: 4rpx;
}

.old-price {
  font-size: 20rpx;
  color: #9CA3AF;
  text-decoration: line-through;
  margin-left: 12rpx;
}

.p-btn {
  width: 100%;
  height: 56rpx;
  border-radius: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 500;
  color: #fff;
}

.blue-btn { background: #3B82F6; }
.gold-btn { background: linear-gradient(90deg, #F59E0B, #D97706); }

.rights-row {
  display: flex;
  justify-content: space-between;
}

.right-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.r-icon {
  width: 60rpx;
  height: 60rpx;
  margin-bottom: 16rpx;
}

.r-title {
  color: #111827;
  font-size: 26rpx;
  font-weight: 600;
  margin-bottom: 8rpx;
}

.r-desc {
  color: #6B7280;
  font-size: 20rpx;
}

.faq-icon {
  margin-left: 12rpx;
}

.faq-text {
  color: #6B7280;
  font-size: 24rpx;
  line-height: 1.6;
}
</style>
