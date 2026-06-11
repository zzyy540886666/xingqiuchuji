import { request } from "../utils/request";
import { buildQuery } from "../utils/query";

export interface OrderPreviewParams {
  skuId: number;
  orderType: "RENT" | "BUY" | "SOFTWARE";
  rentStartDate?: string;
  rentEndDate?: string;
  address?: string;
}

export type OrderStatus =
  | "PENDING_PAY"
  | "PAID"
  | "CANCELLED"
  | "FULFILLING"
  | "COMPLETED"
  | "REFUNDING"
  | "REFUNDED";

export type OrderType = "RENT" | "BUY" | "SOFTWARE";

export interface PriceBreakdownItem {
  label: string;
  amountMinor: number;
}

export interface OrderPreviewResult {
  priceBreakdown: PriceBreakdownItem[];
  payableAmount: number;
  depositAmount: number;
  discountAmount?: number;
}

export interface Order {
  id: string;
  orderNo: string;
  userId?: number;
  status: OrderStatus;
  orderType: OrderType;
  skuId: number;
  amountMinor: number;
  payableMinor: number;
  depositMinor?: number;
  shippingMinor?: number;
  discountMinor?: number;
  rentStartDate?: string;
  rentEndDate?: string;
  addressJson?: string;
  idempotencyKey?: string;
  createdAt: string;
  updatedAt?: string;
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

export function getOrderList(params: { type?: OrderType | string; status?: OrderStatus | string; page?: number; pageSize?: number }): Promise<{ items: Order[]; total: number }> {
  const query = buildQuery({ ...params, pageSize: params.pageSize || 20 });
  return request<{ items: Order[]; total: number }>(`/orders${query ? `?${query}` : ""}`);
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
