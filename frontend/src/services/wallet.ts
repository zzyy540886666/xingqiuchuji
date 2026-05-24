import { request } from "../utils/request";

export interface WalletInfo {
  balanceMinor: number;
  frozenMinor: number;
}

export interface LedgerEntry {
  id: string;
  type: string;
  amountMinor: number;
  balanceAfterMinor: number;
  description: string;
  createdAt: string;
}

export function getWallet(): Promise<WalletInfo> {
  return request<WalletInfo>("/wallet");
}

export function getLedger(params: { cursor?: number; limit?: number }): Promise<{ items: LedgerEntry[]; page: number; pageSize: number; total: number }> {
  const query = new URLSearchParams();
  if (params.cursor) query.set("cursor", String(params.cursor));
  if (params.limit) query.set("limit", String(params.limit));
  return request(`/wallet/ledger?${query.toString()}`);
}

export function withdraw(amountMinor: number, idempotencyKey: string): Promise<{ id: string; status: string; failReason?: string }> {
  return request("/wallet/withdraw", {
    method: "POST",
    data: { amountMinor },
    idempotencyKey,
  });
}
