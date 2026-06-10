<template>
  <view class="product-card" :class="{ row }" @tap="openDetail">
    <view class="product-card__image-wrap" :style="{ height: row ? '200rpx' : '220rpx' }">
      <image v-if="displayImage" class="product-card__image" :src="displayImage" mode="aspectFill" />
      <view class="product-card__type-tags">
        <text v-if="product.type === 'RENT' || hasRentPrice" class="type-tag rent">可租</text>
        <text v-if="product.type === 'BUY' || hasBuyPrice" class="type-tag buy">可买</text>
        <text v-if="product.type === 'SOFTWARE' || hasSoftwarePrice" class="type-tag software">软件适配</text>
      </view>
    </view>
    <view class="product-card__body">
      <view class="product-card__header">
        <view class="product-card__info">
          <text class="product-card__title" :class="row ? 'line-clamp-2' : 'line-clamp-1'">{{ product.title }}</text>

        </view>
        <view class="favorite-btn" @tap.stop="toggleFavorite">
          <image class="favorite-icon" :src="isFavorited ? '/static/icons/star-active.svg' : '/static/icons/star.svg'" mode="aspectFit" />
        </view>
      </view>

      <view class="product-card__tags">
        <text v-for="tag in displayTags" :key="tag" class="feature-tag">{{ tag }}</text>
      </view>

      <view class="product-card__footer">
        <view class="product-card__price-row">
          <text class="product-card__price">
            <text class="price">{{ formatPriceCompact(product.priceAmount) }}</text>
            <text class="unit">/{{ priceUnit }}起</text>
          </text>
          <text class="price-type-tag">{{ priceTypeLabel }}</text>
        </view>
        <view class="product-card__actions">
          <view class="rent-button" @tap.stop="openDetail">{{ actionText }}</view>
          <view class="cart-button" @tap.stop="addCart">
            <image class="cart-shape" src="/static/icons/cart.svg" mode="aspectFit" />
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { formatPriceCompact } from "../utils/format";
import { useCartStore } from "../stores/cart";
import { navigateToPage } from "../utils/navigation";

const props = defineProps<{
  row?: boolean;
  product: {
    id: number;
    title: string;
    subtitle?: string;
    stock: number;
    tags: string[];
    priceAmount: number;
    image: string;
    media?: string[];
    mediaItems?: { id?: number; url: string; type: "IMAGE" | "VIDEO"; sortOrder?: number }[];
    type?: "RENT" | "BUY" | "SOFTWARE";
    brand?: string;
    model?: string;
    adaptedScenesText?: string;
    prices?: { priceType: string; priceMinor: number }[];
  };
}>();
const cartStore = useCartStore();
const isFavorited = ref(false);

const displayImage = computed(() => {
  if (props.product.image) return props.product.image;
  const mediaImage = [...(props.product.mediaItems || [])]
    .filter((item) => item.type === "IMAGE" && item.url)
    .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0))[0]?.url;
  if (mediaImage) return mediaImage;
  return props.product.media?.[0] || "";
});

const displayTags = computed(() => props.product.tags?.slice(0, 4) || []);

const hasRentPrice = computed(() => props.product.prices?.some(p => p.priceType === "DAILY_RENT" || p.priceType === "LEASE_BUY"));
const hasBuyPrice = computed(() => props.product.prices?.some(p => p.priceType === "BUY"));
const hasSoftwarePrice = computed(() => props.product.prices?.some(p => p.priceType === "SUBSCRIPTION"));

const priceUnit = computed(() => {
  const type = props.product.type;
  if (type === "BUY") return "";
  if (type === "SOFTWARE") return "月";
  return "天";
});

const priceTypeLabel = computed(() => {
  const type = props.product.type;
  if (type === "RENT") return "租赁价";
  if (type === "BUY") return "购买价";
  if (type === "SOFTWARE") return "订阅价";
  return "租赁价";
});

const actionText = computed(() => {
  const type = props.product.type;
  if (type === "RENT") return "立即租赁";
  if (type === "BUY") return "立即购买";
  if (type === "SOFTWARE") return "立即订阅";
  return "立即租赁";
});

function openDetail() {
  navigateToPage(`/pages/product-detail/index?id=${props.product.id}`);
}

function addCart() {
  cartStore.addItem({
    skuId: props.product.id,
    title: props.product.title,
    image: displayImage.value,
    priceAmount: props.product.priceAmount,
  });
}

function toggleFavorite() {
  isFavorited.value = !isFavorited.value;
}
</script>

<style scoped lang="scss">
.product-card {
  min-width: 0;
  display: flex;
  flex-direction: column;
  border-radius: 20rpx;
  overflow: hidden;
  background: #fff;
}

.product-card.row {
  display: flex;
  flex-direction: row;
  gap: 22rpx;
  padding: 28rpx 0;
  border-radius: 0;
  border-bottom: 1rpx solid #f3f4f6;
  align-items: stretch;
  background: transparent;
  overflow: visible;
}

.product-card__image-wrap {
  position: relative;
  width: 100%;
  height: 220rpx;
  border-radius: 16rpx;
  background: #fff;
  border: 1rpx solid #f0f0f0;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.product-card.row .product-card__image-wrap {
  width: 220rpx;
}

.product-card__image {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
}

.product-card__type-tags {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  gap: 8rpx;
  padding: 6rpx 0;
  background: rgba(255, 255, 255, 0.95);
}

.type-tag {
  font-size: 18rpx;
  color: #0a4bfe;
  padding: 2rpx 8rpx;
  border-radius: 4rpx;
  background: rgba(10, 75, 254, 0.08);
}

.product-card__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 16rpx 0 4rpx;
}

.product-card.row .product-card__body {
  padding: 4rpx 0;
}

.product-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12rpx;
}

.product-card__info {
  flex: 1;
  min-width: 0;
}

.product-card__title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #111827;
  line-height: 1.35;
}

.product-card__subtitle {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #9ca3af;
  line-height: 1.3;
}

.favorite-btn {
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.favorite-icon {
  width: 36rpx;
  height: 36rpx;
}

.product-card__tags {
  margin-top: 12rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.feature-tag {
  font-size: 20rpx;
  color: #0a4bfe;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  background: rgba(10, 75, 254, 0.08);
  line-height: 1.3;
}

.product-card__scene {
  margin-top: 10rpx;
  font-size: 20rpx;
  color: #9ca3af;
  line-height: 1.3;
}

.product-card__footer {
  margin-top: 14rpx;
}

.product-card__price-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.product-card__price {
  font-size: 22rpx;
  color: #ef4444;
}

.product-card__price .price {
  font-size: 32rpx;
  font-weight: 700;
}

.product-card__price .unit {
  font-size: 20rpx;
  color: #9ca3af;
}

.price-type-tag {
  font-size: 18rpx;
  color: #9ca3af;
  padding: 2rpx 8rpx;
  border-radius: 4rpx;
  background: #f3f4f6;
}

.product-card__actions {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 10rpx;
}

.rent-button {
  height: 56rpx;
  min-width: 140rpx;
  padding: 0 24rpx;
  border-radius: 999rpx;
  background: #0a4bfe;
  color: #fff;
  font-size: 24rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cart-button {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  border: 2rpx solid #0a4bfe;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cart-shape {
  width: 30rpx;
  height: 30rpx;
  display: block;
}
</style>
