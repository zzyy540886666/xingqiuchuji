export interface DistributionInfo {
  inviteCode: string;
  inviteUrl: string;
  teamStats: {
    level1Count: number;
    level2Count: number;
  };
}

export interface Commission {
  id: string;
  orderId: string;
  amountMinor: number;
  status: CommissionStatus;
  createdAt: string;
}

export type CommissionStatus = "PENDING_PROTECT" | "SETTLEABLE" | "SETTLED";
