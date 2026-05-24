export interface SkuItem {
  id: number;
  title: string;
  type: SkuType;
  image: string;
  media?: string[];
  priceAmount: number;
  stock: number;
  tags: string[];
  brand?: string;
  model?: string;
  specs?: Record<string, string>;
  availability?: { available: boolean; reason?: string };
  priceInfo?: {
    rentTiers?: { days: number; pricePerDay: number }[];
    buyPrice?: number;
    subscriptionPrice?: number;
  };
}

export type SkuType = "RENT" | "BUY" | "SOFTWARE";

export interface PageResult<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
}
