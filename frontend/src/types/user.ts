export interface UserProfile {
  id: string;
  nickname: string;
  avatar: string;
  phone?: string;
  memberLevel: number;
  memberLevelName: string;
  nativeResident: boolean;
}

export interface MembershipSummary {
  level: number;
  levelName: string;
  benefits: MemberBenefit[];
  nativeResident: boolean;
  planetCard?: { name: string; expireAt: string };
}

export interface MemberBenefit {
  type: string;
  label: string;
  value: string;
}

export interface PlanetCardSku {
  id: string;
  name: string;
  priceAmount: number;
  durationDays: number;
  benefits: string[];
}
