import { defineStore } from "pinia";
import { computed, ref } from "vue";

export type NavigationMethod =
  | "navigateTo"
  | "switchTab"
  | "redirectTo"
  | "reLaunch"
  | "navigateBack"
  | "blocked";

export type NavigationStatus = "idle" | "pending" | "settled" | "failed";

export interface NavigationProcess {
  id: number;
  from: string;
  to: string;
  method: NavigationMethod;
  reason?: string;
  status: NavigationStatus;
  startedAt: number;
  endedAt?: number;
  error?: string;
}

const MAX_HISTORY = 12;

export const useNavigationStore = defineStore("navigation", () => {
  const activeProcess = ref<NavigationProcess | null>(null);
  const history = ref<NavigationProcess[]>([]);
  let nextId = 1;

  const isNavigating = computed(() => activeProcess.value?.status === "pending");

  function start(to: string, method: NavigationMethod, reason?: string) {
    const process: NavigationProcess = {
      id: nextId++,
      from: getCurrentRoute(),
      to,
      method,
      reason,
      status: "pending",
      startedAt: Date.now(),
    };
    activeProcess.value = process;
    history.value = [process, ...history.value].slice(0, MAX_HISTORY);
    return process.id;
  }

  function settle(id: number) {
    finish(id, "settled");
  }

  function fail(id: number, error?: string) {
    finish(id, "failed", error);
  }

  function block(to: string, reason?: string) {
    const process: NavigationProcess = {
      id: nextId++,
      from: getCurrentRoute(),
      to,
      method: "blocked",
      reason,
      status: "settled",
      startedAt: Date.now(),
      endedAt: Date.now(),
    };
    history.value = [process, ...history.value].slice(0, MAX_HISTORY);
  }

  function finish(id: number, status: "settled" | "failed", error?: string) {
    const endedAt = Date.now();
    if (activeProcess.value?.id === id) {
      activeProcess.value = {
        ...activeProcess.value,
        status,
        endedAt,
        error,
      };
    }
    history.value = history.value.map((item) =>
      item.id === id ? { ...item, status, endedAt, error } : item
    );
  }

  function resetActive() {
    activeProcess.value = null;
  }

  return {
    activeProcess,
    history,
    isNavigating,
    start,
    settle,
    fail,
    block,
    resetActive,
  };
});

function getCurrentRoute() {
  const pages = getCurrentPages();
  const current = pages[pages.length - 1];
  return current?.route ? `/${current.route}` : "";
}
