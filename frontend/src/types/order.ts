export interface Order {
  id: string;
  orderNo: string;
  status: OrderStatus;
  orderType: OrderType;
  skuTitle: string;
  skuImage: string;
  totalAmount: number;
  payableAmount: number;
  depositAmount?: number;
  createdAt: string;
  paidAt?: string;
  allowedActions: string[];
}

export type OrderStatus =
  | "PENDING_PAY"
  | "PAID"
  | "IN_SERVICE"
  | "COMPLETED"
  | "CANCELLED"
  | "REFUNDING";

export type OrderType = "RENT" | "BUY" | "SOFTWARE";

export interface PriceBreakdown {
  baseAmount: number;
  discountAmount: number;
  depositAmount: number;
  payableAmount: number;
  coinDeductAmount?: number;
}

export interface WechatPayParams {
  timeStamp: string;
  nonceStr: string;
  package: string;
  signType: string;
  paySign: string;
}
