import { request } from "../utils/request";
import { buildQuery } from "../utils/query";

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
  refType?: string;
  refId?: string;
  createdAt: string;
}

export function getWallet(): Promise<WalletInfo> {
  return request<WalletInfo>("/wallet");
}

export function getLedger(params: { cursor?: number; limit?: number }): Promise<{ items: LedgerEntry[]; page: number; pageSize: number; total: number }> {
  const query = buildQuery(params);
  return request(`/wallet/ledger${query ? `?${query}` : ""}`);
}

export function withdraw(amountMinor: number, idempotencyKey: string): Promise<{ id: string; status: string; failReason?: string }> {
  return request("/wallet/withdraw", {
    method: "POST",
    data: { amountMinor },
    idempotencyKey,
  });
}
