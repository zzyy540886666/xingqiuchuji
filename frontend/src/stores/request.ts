import { defineStore } from "pinia";
import { computed, ref } from "vue";

export type RequestProcessStatus = "pending" | "success" | "failed";

export interface RequestProcess {
  id: number;
  path: string;
  method: string;
  status: RequestProcessStatus;
  startedAt: number;
  endedAt?: number;
  durationMs?: number;
  httpStatus?: number;
  traceId?: string;
  error?: string;
}

const MAX_HISTORY = 30;

export const useRequestStore = defineStore("request", () => {
  const activeById = ref<Record<number, RequestProcess>>({});
  const history = ref<RequestProcess[]>([]);
  let nextId = 1;

  const pendingCount = computed(() => Object.keys(activeById.value).length);
  const isRequesting = computed(() => pendingCount.value > 0);

  function start(path: string, method: string) {
    const process: RequestProcess = {
      id: nextId++,
      path,
      method,
      status: "pending",
      startedAt: Date.now(),
    };
    activeById.value = { ...activeById.value, [process.id]: process };
    return process.id;
  }

  function succeed(id: number, httpStatus?: number, traceId?: string) {
    finish(id, "success", { httpStatus, traceId });
  }

  function fail(id: number, error: string, httpStatus?: number, traceId?: string) {
    finish(id, "failed", { error, httpStatus, traceId });
  }

  function finish(
    id: number,
    status: RequestProcessStatus,
    updates: Pick<RequestProcess, "error" | "httpStatus" | "traceId"> = {}
  ) {
    const process = activeById.value[id];
    if (!process) return;

    const endedAt = Date.now();
    const finished: RequestProcess = {
      ...process,
      ...updates,
      status,
      endedAt,
      durationMs: endedAt - process.startedAt,
    };

    const { [id]: _done, ...rest } = activeById.value;
    activeById.value = rest;
    history.value = [finished, ...history.value].slice(0, MAX_HISTORY);
  }

  function clearHistory() {
    history.value = [];
  }

  return {
    activeById,
    history,
    pendingCount,
    isRequesting,
    start,
    succeed,
    fail,
    clearHistory,
  };
});
