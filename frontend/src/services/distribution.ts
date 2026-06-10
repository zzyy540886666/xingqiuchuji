import { request } from "../utils/request";
import { buildQuery } from "../utils/query";

export interface DistributionInfo {
  overview: {
    pendingCommission: number;
    withdrawableCommission: number;
    withdrawnCommission: number;
  };
  inviteCode: string;
  inviteUrl: string;
  invitePath: string;
  teamStats: {
    level1Count: number;
    level2Count: number;
    todayNew: number;
    totalInvite: number;
  };
}

const EMPTY_DISTRIBUTION_INFO: DistributionInfo = {
  overview: {
    pendingCommission: 0,
    withdrawableCommission: 0,
    withdrawnCommission: 0,
  },
  inviteCode: "",
  inviteUrl: "",
  invitePath: "",
  teamStats: {
    level1Count: 0,
    level2Count: 0,
    todayNew: 0,
    totalInvite: 0,
  },
};

function normalizeDistributionInfo(value: Partial<DistributionInfo> | null | undefined): DistributionInfo {
  return {
    overview: {
      ...EMPTY_DISTRIBUTION_INFO.overview,
      ...(value?.overview || {}),
    },
    inviteCode: value?.inviteCode || "",
    inviteUrl: value?.inviteUrl || "",
    invitePath: value?.invitePath || "",
    teamStats: {
      ...EMPTY_DISTRIBUTION_INFO.teamStats,
      ...(value?.teamStats || {}),
    },
  };
}

export interface Commission {
  id: string;
  orderId: string;
  orderNo: string;
  productImage: string;
  productName: string;
  sourceType: 'RENT' | 'BUY' | 'ACCESSORY';
  amountMinor: number;
  status: "PENDING_PROTECT" | "SETTLEABLE" | "SETTLED" | "FROZEN" | "DISPUTE";
  createdAt: string;
  settleTime: string;
}

export async function getDistributionMe(): Promise<DistributionInfo> {
  const result = await request<Partial<DistributionInfo> | null>("/distribution/me");
  return normalizeDistributionInfo(result);
}

export function getTeam(): Promise<{ level1Count: number; level2Count: number; members: { userId: string; nickname: string; avatarUrl: string; level: number; joinedAt: string }[] }> {
  return request("/distribution/team");
}

export function getCommissions(params: { status?: string; page?: number }): Promise<{ items: Commission[]; total: number }> {
  const query = buildQuery(params);
  return request(`/distribution/commissions${query ? `?${query}` : ""}`);
}

export function bindInvite(inviteCode: string): Promise<void> {
  return request("/distribution/bind", { method: "POST", data: { inviteCode } });
}
