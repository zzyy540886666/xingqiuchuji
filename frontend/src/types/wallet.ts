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
