import { request } from "../utils/request";

const BATCH_SIZE = 10;
const FLUSH_INTERVAL = 5000;

interface TrackEvent {
  event: string;
  page?: string;
  params?: Record<string, unknown>;
  timestamp: number;
}

let buffer: TrackEvent[] = [];
let timer: ReturnType<typeof setTimeout> | null = null;

function flush() {
  if (buffer.length === 0) return;
  const batch = buffer.splice(0, BATCH_SIZE);
  request("/analytics/events", {
    method: "POST",
    data: { events: batch },
  }).catch(() => {
    // re-queue on failure (max once)
    buffer.unshift(...batch.slice(0, BATCH_SIZE));
  });
}

function scheduleFlush() {
  if (timer) return;
  timer = setTimeout(() => {
    timer = null;
    flush();
  }, FLUSH_INTERVAL);
}

export function track(event: string, params?: Record<string, unknown>) {
  buffer.push({
    event,
    page: getCurrentPageRoute(),
    params,
    timestamp: Date.now(),
  });
  if (buffer.length >= BATCH_SIZE) {
    flush();
  } else {
    scheduleFlush();
  }
}

export function trackPageView(page: string) {
  track("page_view", { page });
}

function getCurrentPageRoute(): string {
  const pages = getCurrentPages();
  const current = pages[pages.length - 1];
  return current?.route || "";
}
