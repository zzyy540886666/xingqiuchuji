import { request } from "../utils/request";

export interface DistributionInfo {
  inviteCode: string;
  inviteUrl: string;
  teamStats: { level1Count: number; level2Count: number };
}

export interface Commission {
  id: string;
  orderId: string;
  amountMinor: number;
  status: "PENDING_PROTECT" | "SETTLEABLE" | "SETTLED";
  createdAt: string;
}

export function getDistributionMe(): Promise<DistributionInfo> {
  return request("/distribution/me");
}

export function getTeam(): Promise<{ level1Count: number; level2Count: number; members: { userId: string; nickname: string; avatarUrl: string; level: number; joinedAt: string }[] }> {
  return request("/distribution/team");
}

export function getCommissions(params: { status?: string; page?: number }): Promise<{ items: Commission[]; total: number }> {
  const query = new URLSearchParams();
  if (params.status) query.set("status", params.status);
  if (params.page) query.set("page", String(params.page));
  return request(`/distribution/commissions?${query.toString()}`);
}

export function bindInvite(inviteCode: string): Promise<void> {
  return request("/distribution/bind", { method: "POST", data: { inviteCode } });
}
