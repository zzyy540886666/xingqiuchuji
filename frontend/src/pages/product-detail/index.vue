<template>
  <view class="page detail-page">
    <view class="detail-nav">
      <view class="nav-left">
        <view class="back-button translucent" @tap="back"><image class="back-icon" src="/static/icons/back-light.svg" mode="aspectFit" /></view>
        <view class="nav-title">
          <text>{{ product?.title || "" }}</text>
          <text>{{ product?.brand || "" }}</text>
        </view>
      </view>
      <view class="nav-tools">
        <view class="round-tool" @tap="shareProduct"><image class="tool-icon" src="/static/icons/share.svg" mode="aspectFit" /></view>
        <view class="round-tool" @tap="showProductActions"><image class="tool-icon" src="/static/icons/more-light.svg" mode="aspectFit" /></view>
      </view>
    </view>

    <swiper class="product-swiper" circular @change="onSwiperChange">
      <swiper-item v-for="image in productImages" :key="image">
        <image class="product-hero" :src="image" mode="aspectFill" />
      </swiper-item>
    </swiper>
    <view v-if="videoUrl" class="play-button" @tap="previewVideo"><image class="play-icon" src="/static/icons/play.svg" mode="aspectFit" /></view>
    <text class="image-counter">{{ currentImageIndex + 1 }}/{{ productImages.length || 1 }}</text>

    <view v-if="product" class="info-card">
      <view class="product-title-row">
        <text class="product-title">{{ product.title }}</text>
        <text class="badge">{{ typeLabel(product.type) }}</text>
      </view>
      <text class="product-sub">{{ product.subtitle || product.brand || "" }}</text>
      <view class="tag-row">
        <text v-for="tag in product.tags" :key="tag" class="tag">{{ tag }}</text>
      </view>
      <view class="meta-lines">
        <text v-if="product.adaptedScenesText"><text class="label">适配场景：</text>{{ product.adaptedScenesText }}</text>
        <text>
          <text class="label">库存状态：</text>
          <text class="stock">{{ product.stockStatusText || `库存 ${product.stock}` }}</text>
          <text v-if="product.deliveryText"> · {{ product.deliveryText }}</text>
        </text>
      </view>

      <view v-if="pricePlans.length" class="plan-tabs">
        <text v-for="plan in pricePlans" :key="plan.key" :class="{ active: selectedPlan === plan.key }" @tap="selectedPlan = plan.key">
          {{ plan.label }}
        </text>
      </view>
      <view v-if="pricePlans.length" class="price-panel">
        <view v-for="plan in pricePlans" :key="plan.key" class="price-cell">
          <text class="price-label">{{ plan.label }}</text>
          <view>
            <text class="price-main" :class="{ muted: selectedPlan !== plan.key }">{{ formatPriceCompact(plan.priceMinor) }}</text>
            <text class="price-unit">{{ plan.unit }}</text>
          </view>
        </view>
      </view>
      <text v-if="product.originalPriceMinor" class="original-price">原价 {{ formatPriceCompact(product.originalPriceMinor) }}</text>

      <view v-if="durations.length" class="block border">
        <text class="block-title">时长</text>
        <view class="duration-grid">
          <text v-for="duration in durations" :key="duration" :class="{ active: selectedDuration === duration }" @tap="selectedDuration = duration">
            {{ duration }}
          </text>
        </view>
      </view>

      <view v-if="product.services?.length" class="block border">
        <text class="block-title">服务</text>
        <view v-for="service in product.services" :key="service.name" class="service-row">
          <view class="check-line"><image class="checked" src="/static/icons/check-blue.svg" mode="aspectFit" /><text>{{ service.name }}</text></view>
          <text class="free">{{ service.priceLabel || service.price }}</text>
        </view>
      </view>

      <view v-if="params.length" class="block border">
        <view class="block-header">
          <text class="block-title">产品参数</text>
          <text class="more" @tap="showAllParams">更多参数</text>
        </view>
        <view class="param-grid">
          <view v-for="param in params" :key="param.label" class="param">
            <text class="param-label">{{ param.label }}</text>
            <text class="param-value">{{ param.value }}</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="detailSections.length" class="details-block">
      <text class="details-title">产品说明</text>
      <view v-for="section in detailSections" :key="section.title" class="fold-row">
        <view class="fold-title" @tap="toggleSection(section.title)">
          <text>{{ section.title }}</text>
          <image class="fold-arrow" :class="{ open: expandedSection === section.title }" src="/static/icons/chevron-down.svg" mode="aspectFit" />
        </view>
        <text v-if="expandedSection === section.title" class="fold-content">{{ section.content }}</text>
      </view>
    </view>

    <view class="bottom-action">
      <view class="detail-actions">
        <view class="mini-action" :class="{ active: isFavorite }" @tap="toggleFavorite"><text>收藏</text></view>
        <view class="mini-action" @tap="contactService"><text>客服</text></view>
        <view class="mini-action" @tap="shareProduct"><text>分享</text></view>
      </view>
      <view class="buy-actions">
        <view class="primary-pill" @tap="goOrder">{{ currentPlan?.key === "BUY" ? "立即购买" : "立即租赁" }}</view>
        <view class="outline-pill" @tap="addCart">加入购物车</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { formatPriceCompact } from "../../utils/format";
import { getSkuDetail } from "../../services/catalog";
import type { SkuItem } from "../../services/catalog";
import { useCartStore } from "../../stores/cart";

const cartStore = useCartStore();
const product = ref<SkuItem | null>(null);
const productImages = ref<string[]>([]);
const currentImageIndex = ref(0);
const selectedPlan = ref("");
const selectedDuration = ref("");
const isFavorite = ref(false);
const expandedSection = ref("");
const params = ref<{ label: string; value: string }[]>([]);
const detailSections = ref<{ title: string; content: string }[]>([]);

const videoUrl = computed(() => product.value?.mediaItems?.find((item) => item.type === "VIDEO")?.url || "");
const pricePlans = computed(() => (product.value?.prices || []).map((price) => ({
  key: price.priceType,
  label: price.priceType === "DAILY_RENT" ? "短期租赁" : price.priceType === "LEASE_BUY" ? "租赁购买" : price.priceType === "BUY" ? "购买买断" : "订阅服务",
  priceMinor: price.priceMinor,
  unit: price.priceType === "DAILY_RENT" ? "/天起" : price.priceType === "LEASE_BUY" ? "/年起" : "/台起",
  minDuration: price.minDuration,
  maxDuration: price.maxDuration,
})));
const currentPlan = computed(() => pricePlans.value.find((item) => item.key === selectedPlan.value) || pricePlans.value[0]);
const durations = computed(() => {
  const plan = currentPlan.value;
  if (!plan || plan.key !== "DAILY_RENT") return [];
  const values = [plan.minDuration, 7, 30, plan.maxDuration].filter((value): value is number => Boolean(value && value > 0));
  return [...new Set(values)].sort((a, b) => b - a).map((days) => `${days}天`);
});

onLoad((query) => loadDetail(Number(query?.id || 0)));

async function loadDetail(skuId: number) {
  if (!skuId) return;
  try {
    const detail = await getSkuDetail(skuId);
    product.value = detail;
    const images = detail.mediaItems?.filter((item) => item.type === "IMAGE").map((item) => item.url) || [];
    productImages.value = images.length ? images : [detail.image].filter(Boolean);
    params.value = Object.entries(detail.specs || {}).map(([label, value]) => ({ label, value }));
    detailSections.value = detail.detailSections || [];
    selectedPlan.value = detail.prices?.[0]?.priceType || "";
    selectedDuration.value = durations.value[0] || "";
    isFavorite.value = Boolean(uni.getStorageSync(`xq_favorite_sku_${detail.id}`));
  } catch {
    uni.showToast({ title: "加载失败", icon: "none" });
  }
}

function typeLabel(type: SkuItem["type"]) {
  return type === "RENT" ? "租" : type === "BUY" ? "购" : "软件";
}
function onSwiperChange(event: { detail: { current: number } }) { currentImageIndex.value = event.detail.current; }
function toggleSection(title: string) { expandedSection.value = expandedSection.value === title ? "" : title; }
function back() { uni.navigateBack({ delta: 1 }); }
function goOrder() {
  if (!product.value) return;
  const orderType = currentPlan.value?.key === "BUY" ? "BUY" : "RENT";
  const rentDays = Number.parseInt(selectedDuration.value, 10) || undefined;
  uni.navigateTo({ url: `/pages/order/confirm?skuId=${product.value.id}&orderType=${orderType}&rentDays=${rentDays || ""}` });
}
function addCart() {
  if (!product.value) return;
  cartStore.addItem({ skuId: product.value.id, title: product.value.title, image: product.value.image, priceAmount: product.value.priceAmount });
}
function toggleFavorite() {
  if (!product.value) return;
  isFavorite.value = !isFavorite.value;
  const key = `xq_favorite_sku_${product.value.id}`;
  if (isFavorite.value) uni.setStorageSync(key, "1"); else uni.removeStorageSync(key);
  uni.showToast({ title: isFavorite.value ? "已收藏" : "已取消收藏", icon: "none" });
}
function contactService() { uni.navigateTo({ url: "/pages/im/chat?id=customer-service" }); }
function previewVideo() {
  if (videoUrl.value) uni.previewMedia({ sources: [{ url: videoUrl.value, type: "video" }] });
}
function shareProduct() {
  if (!product.value) return;
  uni.setClipboardData({ data: `/pages/product-detail/index?id=${product.value.id}`, success: () => uni.showToast({ title: "分享链接已复制", icon: "none" }) });
}
function showProductActions() {
  uni.showActionSheet({ itemList: ["联系客服", "查看同类商品"], success: (result) => {
    if (result.tapIndex === 0) contactService();
    if (result.tapIndex === 1) uni.navigateTo({ url: `/pages/category/index?keyword=${encodeURIComponent(product.value?.brand || "")}` });
  } });
}
function showAllParams() {
  uni.showModal({ title: "产品参数", content: params.value.map((item) => `${item.label}：${item.value}`).join("\n"), showCancel: false });
}
</script>

<style scoped lang="scss">
.detail-page { padding-bottom: 150rpx; background: #f5f6f8; }
.detail-nav { position: absolute; left: 0; right: 0; top: 0; z-index: 20; padding: 92rpx 28rpx 28rpx; display: flex; align-items: center; justify-content: space-between; background: linear-gradient(180deg, rgba(0, 0, 0, .55), transparent); color: #fff; }
.nav-left, .nav-tools, .product-title-row, .tag-row, .plan-tabs, .price-panel, .duration-grid, .service-row, .block-header, .bottom-action, .detail-actions, .buy-actions { display: flex; align-items: center; }
.nav-left { gap: 18rpx; }
.back-button { width: 58rpx; height: 58rpx; display: flex; align-items: center; justify-content: center; }
.nav-title text { display: block; font-size: 25rpx; }
.nav-title text:last-child { font-size: 21rpx; opacity: .8; }
.nav-tools { gap: 18rpx; }
.round-tool { width: 58rpx; height: 58rpx; border-radius: 50%; background: rgba(0,0,0,.28); display: flex; align-items: center; justify-content: center; }
.tool-icon { width: 30rpx; height: 30rpx; display: block; }
.product-swiper { height: 580rpx; background: #e5e7eb; }
.product-hero { width: 100%; height: 100%; }
.play-button { position: absolute; top: 470rpx; left: 34rpx; width: 64rpx; height: 64rpx; border-radius: 50%; background: rgba(0,0,0,.5); display: flex; justify-content: center; align-items: center; }
.play-icon { width: 34rpx; height: 34rpx; display: block; margin-left: 4rpx; }
.image-counter { position: absolute; top: 486rpx; right: 30rpx; color: #fff; font-size: 23rpx; }
.info-card, .details-block { margin-top: 18rpx; padding: 28rpx; background: #fff; }
.product-title-row { gap: 16rpx; }
.product-title { font-size: 34rpx; font-weight: 700; color: #111827; flex: 1; }
.badge { padding: 5rpx 12rpx; color: #0a4bfe; background: #dbeafe; border-radius: 7rpx; font-size: 22rpx; }
.product-sub { display: block; margin-top: 12rpx; color: #6b7280; font-size: 25rpx; }
.tag-row { flex-wrap: wrap; gap: 10rpx; margin: 18rpx 0; }
.tag { background: #eff6ff; color: #2563eb; border-radius: 6rpx; padding: 5rpx 12rpx; font-size: 22rpx; }
.meta-lines text { display: block; margin: 8rpx 0; color: #374151; font-size: 25rpx; }
.label { color: #6b7280; }
.stock { color: #16a34a; }
.plan-tabs { gap: 34rpx; margin-top: 28rpx; border-bottom: 1rpx solid #e5e7eb; }
.plan-tabs text { padding: 18rpx 0; color: #6b7280; font-size: 26rpx; }
.plan-tabs .active { color: #0a4bfe; border-bottom: 4rpx solid #0a4bfe; }
.price-panel { gap: 10rpx; padding: 22rpx 0; }
.price-cell { flex: 1; }
.price-label { display: block; color: #6b7280; font-size: 22rpx; }
.price-main { color: #ef4444; font-size: 36rpx; font-weight: 700; }
.price-main.muted { color: #9ca3af; font-size: 29rpx; }
.price-unit { color: #6b7280; font-size: 21rpx; }
.original-price { color: #9ca3af; text-decoration: line-through; font-size: 23rpx; }
.block { margin-top: 26rpx; padding-top: 25rpx; }
.border { border-top: 1rpx solid #eef2f7; }
.block-title, .details-title { font-size: 28rpx; font-weight: 700; color: #111827; }
.duration-grid { gap: 16rpx; margin-top: 18rpx; }
.duration-grid text { padding: 12rpx 28rpx; background: #f3f4f6; border-radius: 8rpx; }
.duration-grid .active { background: #e0edff; color: #0a4bfe; }
.service-row { justify-content: space-between; margin-top: 18rpx; font-size: 25rpx; }
.checked { width: 24rpx; height: 24rpx; margin-right: 12rpx; display: block; }
.free { color: #16a34a; }
.block-header { justify-content: space-between; }
.more { color: #0a4bfe; font-size: 23rpx; }
.param-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 18rpx; margin-top: 18rpx; }
.param { background: #f8fafc; border-radius: 12rpx; padding: 16rpx; }
.param-label, .param-value { display: block; font-size: 23rpx; }
.param-label { color: #6b7280; }
.param-value { margin-top: 8rpx; color: #111827; font-weight: 600; }
.fold-row { border-bottom: 1rpx solid #eef2f7; }
.fold-title { display: flex; justify-content: space-between; padding: 25rpx 0; font-size: 25rpx; }
.fold-arrow { width: 24rpx; height: 24rpx; transition: transform .16s ease; }
.fold-arrow.open { transform: rotate(180deg); }
.fold-content { display: block; padding: 0 0 24rpx; line-height: 1.7; color: #6b7280; font-size: 24rpx; }
.bottom-action { position: fixed; left: 0; right: 0; bottom: 0; z-index: 30; justify-content: space-between; gap: 16rpx; padding: 18rpx 22rpx calc(18rpx + env(safe-area-inset-bottom)); background: #fff; box-shadow: 0 -3rpx 14rpx rgba(0,0,0,.06); }
.detail-actions { gap: 22rpx; }
.mini-action { color: #6b7280; font-size: 22rpx; }
.mini-action.active { color: #ef4444; }
.buy-actions { flex: 1; gap: 12rpx; }
.primary-pill, .outline-pill { flex: 1; text-align: center; border-radius: 40rpx; padding: 21rpx 8rpx; font-size: 25rpx; }
.primary-pill { color: #fff; background: #0a4bfe; }
.outline-pill { color: #0a4bfe; border: 1rpx solid #0a4bfe; }
</style>
