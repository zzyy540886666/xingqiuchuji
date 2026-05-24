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
  revenueRecords: RevenueRecord[];
}

export interface RevenueRecord {
  id: string;
  amountMinor: number;
  status: string;
  createdAt: string;
}
