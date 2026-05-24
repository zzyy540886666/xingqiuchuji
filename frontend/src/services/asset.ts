import { request } from "../utils/request";

export interface Asset {
  id: string;
  name: string;
  modelInfo: string;
  imageUrl?: string;
  status: string;
}

export interface AssetDashboard {
  deviceStatus: string;
  totalRevenueMinor: number;
  monthRevenueMinor: number;
  activeSlotCount: number;
  revenueRecords: { id: string; amountMinor: number; status: string; createdAt: string }[];
}

export function getAssets(): Promise<Asset[]> {
  return request("/assets/robots");
}

export function getAssetDetail(assetId: string): Promise<Asset> {
  return request(`/assets/robots/${assetId}`);
}

export function getAssetDashboard(assetId: string): Promise<AssetDashboard> {
  return request(`/assets/robots/${assetId}/dashboard`);
}

export function submitTrusteeship(assetId: string, data: { startTime: string; endTime: string; dailyRateMinor: number }): Promise<{ status: string }> {
  return request(`/assets/robots/${assetId}/trusteeship`, {
    method: "POST",
    data,
  });
}
