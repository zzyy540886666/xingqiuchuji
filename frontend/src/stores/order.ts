import { defineStore } from "pinia";
import { ref } from "vue";

export interface OrderDraft {
  skuId?: number;
  orderType?: "RENT" | "BUY" | "SOFTWARE";
  rentDays?: number;
  addressId?: string;
  couponId?: string;
  coinDeduct?: number;
  idempotencyKey?: string;
}

export const useOrderStore = defineStore("order", () => {
  const draft = ref<OrderDraft>({});
  const previewResult = ref<Record<string, unknown> | null>(null);

  function updateDraft(partial: Partial<OrderDraft>) {
    Object.assign(draft.value, partial);
  }

  function resetDraft() {
    draft.value = {};
    previewResult.value = null;
  }

  function generateIdempotencyKey() {
    draft.value.idempotencyKey = `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
  }

  return { draft, previewResult, updateDraft, resetDraft, generateIdempotencyKey };
});
