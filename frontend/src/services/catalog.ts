import { request } from "../utils/request";

export interface SkuListParams {
  type?: "RENT" | "BUY" | "SOFTWARE";
  brandId?: string;
  modelId?: string;
  minPrice?: number;
  maxPrice?: number;
  keyword?: string;
  page?: number;
  pageSize?: number;
}

export interface SkuPrice {
  id?: number;
  priceType: "DAILY_RENT" | "LEASE_BUY" | "BUY" | "SUBSCRIPTION" | string;
  priceMinor: number;
  minDuration?: number;
  maxDuration?: number;
  dailyRateMinor?: number;
}

export interface SkuItem {
  id: number;
  name?: string;
  title: string;
  type: "RENT" | "BUY" | "SOFTWARE";
  image: string;
  media?: string[];
  mediaItems?: { id?: number; url: string; type: "IMAGE" | "VIDEO"; sortOrder?: number }[];
  priceAmount: number;
  prices?: SkuPrice[];
  originalPriceMinor?: number;
  stock: number;
  stockStatusText?: string;
  deliveryText?: string;
  adaptedScenesText?: string;
  subtitle?: string;
  tags: string[];
  brand?: string;
  model?: string;
  specs?: Record<string, string>;
  services?: { name: string; price?: string; priceLabel?: string }[];
  detailSections?: { title: string; content: string; sortOrder?: number }[];
}

export interface PageResult<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
}

export interface CatalogFilterOption {
  id: number;
  label: string;
  value: string;
  minPriceMinor?: number;
  maxPriceMinor?: number;
  sortOrder: number;
}

export interface CatalogFilterGroup {
  id: number;
  code: string;
  title: string;
  filterField: "BRAND_ID" | "PRICE_RANGE" | "MODEL_ID" | "KEYWORD";
  options: CatalogFilterOption[];
}

export interface SceneItem {
  id: number;
  name: string;
  description?: string;
  imageUrl: string;
  sortOrder: number;
}

export function getSkuList(params: SkuListParams): Promise<PageResult<SkuItem>> {
  const query = new URLSearchParams();
  if (params.type) query.set("type", params.type);
  if (params.brandId) query.set("brandId", params.brandId);
  if (params.modelId) query.set("modelId", params.modelId);
  if (params.minPrice != null) query.set("minPrice", String(params.minPrice));
  if (params.maxPrice != null) query.set("maxPrice", String(params.maxPrice));
  if (params.keyword) query.set("q", params.keyword);
  if (params.page) query.set("page", String(params.page));
  if (params.pageSize) query.set("pageSize", String(params.pageSize));
  return request<PageResult<SkuItem>>(`/catalog/skus?${query.toString()}`);
}

export function getSkuDetail(skuId: number): Promise<SkuItem> {
  return request<SkuItem>(`/catalog/skus/${skuId}`);
}

export function getCatalogFilters(): Promise<CatalogFilterGroup[]> {
  return request<CatalogFilterGroup[]>("/catalog/filters");
}

export function getScenes(): Promise<SceneItem[]> {
  return request<SceneItem[]>("/catalog/scenes");
}

export function getSceneBundles(sceneId: string) {
  return request<{ bundles: unknown[] }>(`/catalog/scenes/${sceneId}/bundles`);
}
