import { request } from "../utils/request";

export interface MembershipSummary {
  level: number;
  isNative: boolean;
  lifetimeSpendMinor: number;
  planetCardExpiresAt?: string;
  benefits: { benefitType: string; totalCount: number; usedCount: number; expiresAt?: string }[];
}

export interface PlanetCardSku {
  id: string;
  name: string;
  priceMinor: number;
  durationDays: number;
  benefitLevel: number;
  stock: number;
  status: string;
}

export function getMembership(): Promise<MembershipSummary> {
  return request<MembershipSummary>("/users/me/membership");
}

export function getPlanetCards(): Promise<PlanetCardSku[]> {
  return request<PlanetCardSku[]>("/membership/planet-cards");
}

export function purchasePlanetCard(skuId: string): Promise<void> {
  return request<void>(`/membership/planet-cards/${skuId}/purchase`, {
    method: "POST",
  });
}
