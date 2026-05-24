import { defineStore } from "pinia";
import { ref } from "vue";
import { request } from "../utils/request";

export interface CartItem {
  skuId: number;
  title: string;
  image: string;
  priceAmount: number;
  quantity: number;
}

export const useCartStore = defineStore("cart", () => {
  const items = ref<CartItem[]>([]);

  function addItem(item: Omit<CartItem, "quantity">) {
    const existing = items.value.find((i) => i.skuId === item.skuId);
    if (existing) {
      existing.quantity++;
    } else {
      items.value.push({ ...item, quantity: 1 });
    }
    uni.showToast({ title: "已加入购物车", icon: "none" });
  }

  function removeItem(skuId: number) {
    items.value = items.value.filter((i) => i.skuId !== skuId);
  }

  function clear() {
    items.value = [];
  }

  return { items, addItem, removeItem, clear };
});
