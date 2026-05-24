<template>
  <view class="page member-page">
    <view class="space-bg">
      <view v-for="i in 30" :key="i" class="star-dot" :style="starStyle(i)"></view>
      <view class="planet-card"><view class="planet"></view></view>
    </view>

    <view class="member-head">
      <StatusBar light time="9:43" />
      <view class="member-nav">
        <view class="back-button translucent" @tap="back"><image class="back-icon" src="/static/icons/back-light.svg" mode="aspectFit" /></view>
        <text>会员中心</text>
        <view class="nav-right">
          <image class="nav-tool" src="/static/icons/grid.svg" mode="aspectFit" />
          <image class="nav-tool" src="/static/icons/circle-light.svg" mode="aspectFit" />
        </view>
      </view>
      <view class="member-user">
        <image class="member-avatar" :src="user?.avatarUrl || '/static/images/avatar.png'" mode="aspectFill" />
        <view>
          <view class="member-name-row"><text>{{ user?.nickname || '微信用户' }}</text><text>Lv.{{ membership?.level || 0 }} {{ membership?.isNative ? '原住民' : '会员' }}</text></view>
          <text class="member-id">用户 ID: {{ user?.id || '-' }}</text>
        </view>
      </view>
      <view class="level-card">
        <view class="level-top"><view><text>Lv.{{ membership?.level || 0 }}</text><text>{{ membership?.isNative ? '原住民' : '会员' }}</text></view><text>{{ formatAmount(membership?.lifetimeSpendMinor || 0) }}</text></view>
        <view class="level-mid"><text>{{ levelTip }}</text><view @tap="showGrowthRules">成长中心 &gt;</view></view>
        <view class="level-line">
          <view v-for="node in levelNodes" :key="node.name" class="level-node" :class="{ active: node.levelValue === (membership?.level || 0) }">
            <text>{{ node.level }}</text>
            <text>{{ node.name }}</text>
          </view>
        </view>
        <view class="level-rule"><text>星球卡到期：{{ cardExpiry }}</text><text @tap="showGrowthRules">查看等级规则 &gt;</text></view>
      </view>
    </view>

    <view class="benefits card">
      <text class="section-title">会员权益</text>
      <view class="benefit-grid">
        <view v-for="benefit in benefits" :key="benefit.label" class="benefit-item">
          <image class="benefit-icon" src="/static/icons/member.svg" mode="aspectFit" />
          <text>{{ benefit.value }}</text>
          <text>{{ benefit.label }}</text>
        </view>
      </view>
    </view>

    <view class="planet-section card">
      <view class="section-title-row">
        <view><text class="section-title">星球卡</text><text class="planet-hint">选择适合你的会员卡</text></view>
        <text class="section-more" @tap="showRights">对比权益</text>
      </view>
      <view class="card-grid">
        <view v-for="card in planetCards" :key="card.id" class="planet-ticket" :class="card.benefitLevel > 1 ? 'gold' : 'blue'">
          <view class="ticket-top">
            <text>{{ card.name }}</text>
            <text>{{ card.durationDays }}天有效期</text>
            <text class="ticket-badge">Lv.{{ card.benefitLevel }}</text>
          </view>
          <view class="ticket-bottom">
            <view class="ticket-line"><text>库存</text><text>{{ card.stock }}</text></view>
            <view class="ticket-line"><text>权益等级</text><text>Lv.{{ card.benefitLevel }}</text></view>
            <view class="ticket-price"><text></text><text>{{ formatAmount(card.priceMinor) }}</text></view>
            <view class="open-button" @tap="requestCard(card)">立即开通</view>
          </view>
        </view>
      </view>
    </view>

    <view class="rights card">
      <view class="section-title-row"><text class="section-title">权益说明</text><text class="section-more" @tap="showRights">更多说明</text></view>
      <view class="rights-row">
        <view v-for="right in rights" :key="right.title" class="right-item">
          <image class="benefit-icon" src="/static/icons/service.svg" mode="aspectFit" />
          <text>{{ right.title }}</text>
          <text>{{ right.desc }}</text>
        </view>
      </view>
    </view>

    <view class="faq card">
      <view class="section-title-row"><text class="section-title">常见问题</text><text class="section-more" @tap="showRights">查看全部</text></view>
      <view v-for="question in questions" :key="question.title" class="faq-row" @tap="showQuestion(question)"><text>{{ question.title }}</text><image class="chevron" src="/static/icons/chevron-right.svg" mode="aspectFit" /></view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import StatusBar from "../../components/StatusBar.vue";
import { getMembership, getPlanetCards, purchasePlanetCard, type MembershipSummary, type PlanetCardSku } from "../../services/member";
import { useSessionStore } from "../../stores/session";
import { formatAmount } from "../../utils/format";

const levelNodes = [
  { level: "Lv.0", levelValue: 0, name: "观察者" },
  { level: "Lv.1", levelValue: 1, name: "原住民" },
  { level: "Lv.2", levelValue: 2, name: "探索者" },
  { level: "Lv.3", levelValue: 3, name: "开拓者" },
];
const rights = [
  { title: "免押特权", desc: "租赁免押，轻松体验" },
  { title: "维保特权", desc: "免费维保，省心省力" },
  { title: "折扣特权", desc: "配件优惠，持续省钱" },
];
const questions = [
  { title: "星球卡如何使用？", answer: "开通成功后权益自动到账，可在租赁下单时使用对应权益。" },
  { title: "免押次数如何计算？", answer: "符合条件的租赁订单使用一次免押权益，具体以订单确认页展示为准。" },
  { title: "升级规则是什么？", answer: "会员等级依据累计完成消费金额计算，退款或取消订单不计入成长值。" },
];
const membership = ref<MembershipSummary | null>(null);
const planetCards = ref<PlanetCardSku[]>([]);
const session = useSessionStore();
const user = computed(() => session.user);
const benefits = computed(() => (membership.value?.benefits || []).map((benefit) => ({
  label: benefitLabel(benefit.benefitType),
  value: `${Math.max(benefit.totalCount - benefit.usedCount, 0)} 次`,
})));

const levelTip = computed(() => {
  const spend = membership.value?.lifetimeSpendMinor || 0;
  if (spend < 10000) return `再消费 ¥${((10000 - spend) / 100).toFixed(0)} 升级 Lv.1 原住民`;
  if (spend < 50000) return `再消费 ¥${((50000 - spend) / 100).toFixed(0)} 升级 Lv.2 探索者`;
  if (spend < 200000) return `再消费 ¥${((200000 - spend) / 100).toFixed(0)} 升级 Lv.3 开拓者`;
  return "已达到当前最高成长等级";
});
const cardExpiry = computed(() => membership.value?.planetCardExpiresAt?.slice(0, 10) || "尚未开通");

onShow(async () => {
  try {
    [membership.value, planetCards.value] = await Promise.all([getMembership(), getPlanetCards()]);
    if (!session.user) await session.fetchUser();
  } catch {
    // Guest presentation remains available before login.
  }
});

function starStyle(index: number) {
  return `left:${(index * 29) % 100}%;top:${(index * 37) % 100}%;opacity:${0.3 + (index % 6) / 10}`;
}

function back() {
  uni.navigateBack({ delta: 1 });
}

function showGrowthRules() {
  uni.showModal({ title: "成长规则", content: "累计完成消费达到 ¥100、¥500、¥2,000 可依次升级，具体权益以账户到账结果为准。", showCancel: false });
}

function showRights() {
  uni.showModal({ title: "星球卡权益", content: "星球卡开通后自动发放对应免押、维保和折扣权益，权益余量可在本页查看。", showCancel: false });
}

function showQuestion(question: { title: string; answer: string }) {
  uni.showModal({ title: question.title, content: question.answer, showCancel: false });
}

function benefitLabel(type: string) {
  const names: Record<string, string> = { FREE_DEPOSIT: "剩余免押次数", MAINTENANCE: "剩余维保次数", DISCOUNT: "可用折扣权益" };
  return names[type] || type;
}

function requestCard(card: PlanetCardSku) {
  uni.showModal({
    title: `开通${card.name}`,
    content: `确认以 ${formatAmount(card.priceMinor)} 开通该星球卡？`,
    confirmText: "确认开通",
    success: async (result) => {
      if (!result.confirm) return;
      try {
        await purchasePlanetCard(card.id);
        membership.value = await getMembership();
        uni.showToast({ title: "开通成功", icon: "none" });
      } catch (error: any) {
        uni.showToast({ title: error.message || "开通失败", icon: "none" });
      }
    },
  });
}
</script>

<style scoped lang="scss">
.member-page {
  background: #f4f8ff;
  padding-bottom: 40rpx;
  position: relative;
}

.space-bg {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  height: 920rpx;
  background: linear-gradient(180deg, #061b3f, #08285a, #0a2f6c);
  overflow: hidden;
}

.star-dot {
  position: absolute;
  width: 4rpx;
  height: 4rpx;
  border-radius: 50%;
  background: #fff;
}

.planet-card {
  position: absolute;
  right: -40rpx;
  top: 160rpx;
  width: 400rpx;
  height: 240rpx;
  border-radius: 32rpx;
  border: 1rpx solid rgba(47, 140, 255, 0.45);
  background: rgba(47, 140, 255, 0.18);
  transform: rotate(-12deg);
}

.planet {
  position: absolute;
  right: -30rpx;
  top: -30rpx;
  width: 150rpx;
  height: 150rpx;
  border-radius: 50%;
  background: radial-gradient(circle, #5a9bff, #1677ff);
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
  color: #fff;
}

.member-nav > text {
  font-size: 34rpx;
  font-weight: 900;
}

.translucent {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.nav-right {
  display: flex;
  gap: 14rpx;
}

.nav-tool {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  padding: 16rpx;
}

.member-user {
  display: flex;
  align-items: center;
  gap: 22rpx;
  padding: 30rpx 40rpx 0;
}

.member-avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  border: 6rpx solid #fff;
}

.member-name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.member-name-row text:first-child {
  color: #fff;
  font-size: 32rpx;
  font-weight: 900;
}

.member-name-row text:last-child {
  color: #5a9bff;
  border: 1rpx solid rgba(47, 140, 255, 0.45);
  background: rgba(8, 40, 90, 0.6);
  border-radius: 999rpx;
  padding: 6rpx 16rpx;
  font-size: 22rpx;
  font-weight: 800;
}

.member-id {
  display: block;
  color: rgba(255, 255, 255, 0.8);
  font-size: 24rpx;
  margin-top: 10rpx;
}

.level-card {
  margin: 40rpx 30rpx 0;
  padding: 28rpx;
  border: 1rpx solid #1e4a7a;
  border-radius: 28rpx;
  background: #0b2855;
  color: #fff;
  box-shadow: 0 18rpx 40rpx rgba(0, 0, 0, 0.18);
}

.level-top,
.level-mid,
.level-rule {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.level-top view {
  display: flex;
  gap: 12rpx;
  align-items: center;
}

.level-top view text:first-child {
  font-size: 32rpx;
  font-weight: 900;
}

.level-mid {
  margin-top: 14rpx;
  color: rgba(255, 255, 255, 0.76);
  font-size: 24rpx;
}

.level-mid view {
  border: 1rpx solid rgba(255, 255, 255, 0.5);
  border-radius: 999rpx;
  padding: 8rpx 18rpx;
  color: #fff;
}

.level-line {
  display: flex;
  justify-content: space-between;
  margin: 28rpx 0 20rpx;
  position: relative;
}

.level-line::before {
  content: "";
  position: absolute;
  left: 20rpx;
  right: 20rpx;
  top: 18rpx;
  height: 3rpx;
  background: #1e4a7a;
}

.level-node {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  color: rgba(255, 255, 255, 0.42);
  font-size: 18rpx;
}

.level-node text:first-child {
  min-width: 36rpx;
  height: 36rpx;
  border-radius: 999rpx;
  border: 4rpx solid #2f5a7a;
  background: #0b2855;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16rpx;
}

.level-node.active {
  color: #fff;
}

.level-node.active text:first-child {
  min-width: 72rpx;
  border-color: #7db3ff;
  background: linear-gradient(135deg, #5a9bff, #1677ff);
}

.level-rule {
  padding-top: 20rpx;
  border-top: 1rpx solid #1e4a7a;
  color: rgba(255, 255, 255, 0.72);
  font-size: 22rpx;
  gap: 16rpx;
}

.level-rule text:last-child {
  color: #5a9bff;
  white-space: nowrap;
}

.benefits,
.planet-section,
.rights,
.faq {
  position: relative;
  z-index: 2;
  margin: 32rpx 30rpx 0;
  padding: 32rpx;
  border-radius: 32rpx;
}

.benefits {
  margin-top: 34rpx;
}

.benefit-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  row-gap: 36rpx;
  margin-top: 30rpx;
}

.benefit-item,
.right-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  text-align: center;
}

.benefit-icon {
  width: 48rpx;
  height: 48rpx;
  display: block;
}

.benefit-item text:nth-child(2) {
  color: #111827;
  font-size: 24rpx;
  font-weight: 900;
}

.benefit-item text:nth-child(3),
.right-item text:nth-child(3) {
  color: #9ca3af;
  font-size: 20rpx;
  line-height: 1.35;
}

.planet-hint {
  color: #9ca3af;
  font-size: 22rpx;
  margin-left: 16rpx;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14rpx;
}

.planet-ticket {
  border-radius: 24rpx;
  overflow: hidden;
}

.planet-ticket.blue { background: linear-gradient(180deg, #08285a, #0a2f6c); }
.planet-ticket.gold { background: linear-gradient(180deg, #8b6914, #a67c1a); }

.ticket-top {
  height: 140rpx;
  padding: 20rpx;
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.ticket-top text:first-child {
  color: #fff;
  font-size: 24rpx;
  font-weight: 900;
}

.ticket-top text:nth-child(2) {
  color: rgba(255, 255, 255, 0.76);
  font-size: 20rpx;
}

.ticket-badge {
  position: absolute;
  right: 16rpx;
  top: 18rpx;
  background: #1677ff;
  color: #fff;
  border-radius: 999rpx;
  padding: 4rpx 12rpx;
  font-size: 18rpx !important;
  font-weight: 900;
}

.gold .ticket-badge {
  background: #ffb84d;
  color: #8b6914;
}

.ticket-bottom {
  background: #fff;
  padding: 20rpx;
}

.ticket-line {
  display: flex;
  justify-content: space-between;
  color: #666;
  font-size: 18rpx;
  margin-bottom: 8rpx;
}

.ticket-line text:last-child {
  color: #111827;
  font-weight: 900;
}

.ticket-price {
  margin: 14rpx 0;
  color: #ff6b00;
}

.ticket-price text:last-child {
  font-size: 44rpx;
  font-weight: 900;
}

.open-button {
  height: 58rpx;
  border-radius: 999rpx;
  background: linear-gradient(90deg, #1677ff, #2f8cff);
  color: #fff;
  font-size: 24rpx;
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
}

.gold .open-button {
  background: linear-gradient(90deg, #dfa83a, #f5c76a);
}


.rights-row {
  display: flex;
  gap: 20rpx;
}

.right-item {
  flex: 1;
  padding-top: 10rpx;
}

.right-item text:nth-child(2) {
  color: #111827;
  font-size: 26rpx;
  font-weight: 900;
}

.faq-row {
  min-height: 72rpx;
  border-bottom: 1rpx solid #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #4b5563;
  font-size: 26rpx;
}

.faq-row:last-child {
  border-bottom: 0;
}
</style>
