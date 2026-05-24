import { request } from "../utils/request";

export interface OrderPreviewParams {
  skuId: number;
  orderType: "RENT" | "BUY" | "SOFTWARE";
  rentStartDate?: string;
  rentEndDate?: string;
  address?: string;
}

export interface PriceBreakdownItem {
  label: string;
  amountMinor: number;
}

export interface OrderPreviewResult {
  priceBreakdown: PriceBreakdownItem[];
  payableAmount: number;
  depositAmount: number;
}

export interface Order {
  id: string;
  orderNo: string;
  status: string;
  orderType: "RENT" | "BUY" | "SOFTWARE";
  skuId: number;
  amountMinor: number;
  payableMinor: number;
  depositMinor?: number;
  shippingMinor?: number;
  discountMinor?: number;
  rentStartDate?: string;
  rentEndDate?: string;
  addressJson?: string;
  createdAt: string;
}

export function previewOrder(params: OrderPreviewParams): Promise<OrderPreviewResult> {
  return request<OrderPreviewResult>("/orders/preview", {
    method: "POST",
    data: params,
  });
}

export function createOrder(params: OrderPreviewParams, idempotencyKey: string): Promise<Order> {
  return request<Order>("/orders", {
    method: "POST",
    data: { ...params, idempotencyKey },
    idempotencyKey,
  });
}

export function getWechatPayParams(orderId: string) {
  return request<{
    timeStamp: string;
    nonceStr: string;
    package: string;
    signType: string;
    paySign: string;
  }>(`/orders/${orderId}/payments/wechat-jsapi`, { method: "POST" });
}

export function getOrderList(params: { type?: string; status?: string; page?: number; pageSize?: number }): Promise<{ items: Order[]; total: number }> {
  const query = new URLSearchParams();
  if (params.type) query.set("type", params.type);
  if (params.status) query.set("status", params.status);
  if (params.page) query.set("page", String(params.page));
  if (params.pageSize) query.set("pageSize", String(params.pageSize || 20));
  return request<{ items: Order[]; total: number }>(`/orders?${query.toString()}`);
}

export function getOrderDetail(orderId: string): Promise<Order> {
  return request<Order>(`/orders/${orderId}`);
}

export function cancelOrder(orderId: string): Promise<void> {
  return request<void>(`/orders/${orderId}/cancel`, { method: "POST" });
}

export function getContractUrl(orderId: string): Promise<{ url: string }> {
  return request<{ url: string }>(`/orders/${orderId}/contract/download-url`);
}
