import { defineStore } from "pinia";
import { ref } from "vue";
import { request } from "../utils/request";

export interface Banner {
  id: number;
  imageUrl: string;
  linkUrl: string;
  title: string;
}

export interface Activity {
  id: number;
  title: string;
  tag: string;
  startAt: string;
  endAt: string;
  description: string;
}

export interface AppConfig {
  version?: number;
  upToDate?: boolean;
  membershipRules?: unknown[];
  planetCards?: unknown[];
  topics?: unknown[];
  qaBlocks?: unknown[];
  hotKeywords?: string[];
  banners?: Banner[];
  activities?: Activity[];
  sceneTags?: Record<string, string[]>;
  commissionEnabled?: boolean;
}

export const useConfigStore = defineStore("config", () => {
  const config = ref<AppConfig>({});
  const loaded = ref(false);

  async function fetchConfig() {
    try {
      const res = await request<AppConfig>("/config/app");
      if (!res.upToDate) {
        config.value = res;
      }
      loaded.value = true;
    } catch {
      // allow app to continue in degraded mode
    }
  }

  return { config, loaded, fetchConfig };
});
