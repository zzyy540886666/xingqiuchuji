import { request } from "../utils/request";
import { buildQuery } from "../utils/query";

export interface SkuListParams {
  type?: "RENT" | "BUY" | "SOFTWARE";
  brandId?: string;
  modelId?: string;
  minPrice?: number;
  maxPrice?: number;
  keyword?: string;
  page?: number;
  pageSize?: number;
  recommended?: boolean;
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
  tags?: string[];
}

export function getSkuList(params: SkuListParams): Promise<PageResult<SkuItem>> {
  const query = buildQuery({
    type: params.type,
    brandId: params.brandId,
    modelId: params.modelId,
    minPrice: params.minPrice,
    maxPrice: params.maxPrice,
    q: params.keyword,
    page: params.page,
    pageSize: params.pageSize,
    recommended: params.recommended,
  });
  return request<PageResult<SkuItem>>(`/catalog/skus${query ? `?${query}` : ""}`);
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
