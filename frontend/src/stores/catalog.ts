import { defineStore } from "pinia";
import { ref } from "vue";
import { readJsonStorage, writeJsonStorage } from "../utils/storage";

export interface CatalogFilter {
  type: "RENT" | "BUY" | "SOFTWARE";
  brandId?: string;
  modelId?: string;
  minPrice?: number;
  maxPrice?: number;
  keyword?: string;
  page: number;
  pageSize: number;
}

export const useCatalogStore = defineStore("catalog", () => {
  const filter = ref<CatalogFilter>({
    type: "RENT",
    page: 1,
    pageSize: 20,
  });

  const searchHistory = ref<string[]>(readJsonStorage("xq_search_history", []));

  function setFilter(partial: Partial<CatalogFilter>) {
    Object.assign(filter.value, partial, { page: 1 });
  }

  function addSearchHistory(keyword: string) {
    if (!keyword.trim()) return;
    searchHistory.value = [
      keyword,
      ...searchHistory.value.filter((k) => k !== keyword),
    ].slice(0, 20);
    writeJsonStorage("xq_search_history", searchHistory.value);
  }

  function clearSearchHistory() {
    searchHistory.value = [];
    uni.removeStorageSync("xq_search_history");
  }

  return { filter, searchHistory, setFilter, addSearchHistory, clearSearchHistory };
});
