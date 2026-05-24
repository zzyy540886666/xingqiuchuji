<template>
  <view class="product-card" :class="{ row }" @tap="openDetail">
    <view class="product-card__image-wrap">
      <image class="product-card__image" :src="product.image" mode="aspectFill" />
      <text class="product-card__stock">可租 {{ product.stock }}</text>
    </view>
    <view class="product-card__body">
      <text class="product-card__title" :class="row ? 'line-clamp-2' : 'line-clamp-1'">{{ product.title }}</text>
      <view class="product-card__tags">
        <text v-for="tag in product.tags" :key="tag" class="muted-tag">{{ tag }}</text>
      </view>
      <view class="product-card__footer">
        <text class="product-card__price"><text class="price">{{ formatPriceCompact(product.priceAmount) }}</text> /天起</text>
        <view class="product-card__actions">
          <view class="rent-button" @tap.stop="openDetail">立即租赁</view>
          <view class="cart-button" @tap.stop="addCart">
            <image class="cart-shape" src="/static/icons/cart.svg" mode="aspectFit" />
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { formatPriceCompact } from "../utils/format";
import { useCartStore } from "../stores/cart";

const props = defineProps<{
  row?: boolean;
  product: {
    id: number;
    title: string;
    stock: number;
    tags: string[];
    priceAmount: number;
    image: string;
  };
}>();
const cartStore = useCartStore();

function openDetail() {
  uni.navigateTo({ url: `/pages/product-detail/index?id=${props.product.id}` });
}

function addCart() {
  cartStore.addItem({
    skuId: props.product.id,
    title: props.product.title,
    image: props.product.image,
    priceAmount: props.product.priceAmount,
  });
}
</script>

<style scoped lang="scss">
.product-card {
  min-width: 0;
}

.product-card.row {
  display: flex;
  gap: 22rpx;
  padding: 22rpx 0;
  border-bottom: 1rpx solid #f3f4f6;
}

.product-card__image-wrap {
  position: relative;
  height: 260rpx;
  border-radius: 20rpx;
  background: #f6f8fa;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.row .product-card__image-wrap {
  width: 200rpx;
  height: 200rpx;
  flex-shrink: 0;
}

.product-card__image {
  width: 92%;
  height: 92%;
  border-radius: 16rpx;
}

.product-card__stock {
  position: absolute;
  left: 12rpx;
  bottom: 12rpx;
  height: 32rpx;
  line-height: 32rpx;
  padding: 0 12rpx;
  border-radius: 6rpx;
  background: rgba(219, 234, 254, 0.92);
  color: #0a4bfe;
  font-size: 18rpx;
  font-weight: 800;
}

.product-card__body {
  padding: 16rpx 2rpx 0;
  flex: 1;
  min-width: 0;
}

.row .product-card__body {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding-top: 0;
}

.product-card__title {
  display: block;
  font-size: 24rpx;
  font-weight: 800;
  color: #111827;
  line-height: 1.35;
}

.row .product-card__title {
  font-size: 28rpx;
}

.product-card__tags {
  margin-top: 12rpx;
  min-height: 64rpx;
  overflow: hidden;
}

.product-card__footer {
  margin-top: 8rpx;
}

.row .product-card__footer {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}

.product-card__price {
  font-size: 20rpx;
  color: #9ca3af;
}

.row .product-card__price .price {
  font-size: 32rpx;
}

.product-card__actions {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-top: 12rpx;
}

.rent-button {
  height: 52rpx;
  min-width: 118rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  background: #0a4bfe;
  color: #fff;
  font-size: 22rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cart-button {
  width: 52rpx;
  height: 52rpx;
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
