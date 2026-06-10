import { reLaunchToPage } from "./navigation";
import { useRequestStore } from "../stores/request";

export const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
const API_PREFIX = "/api/v1";
const UPLOADS_PREFIX = "/uploads/";
const LOCAL_UPLOAD_ORIGIN = /^http:\/\/(?:localhost|127\.0\.0\.1)(?::\d+)?(?=\/uploads\/)/;
const AVAILABLE_LOCAL_IMAGES = new Set([
  "/static/images/default-avatar.svg",
  "/static/images/product-placeholder.svg",
  "/static/images/opening-screen-full.png",
  "/static/images/opening-screen.webp",
]);
let traceCounter = 0;

export type ApiResult<T> = {
  success: boolean;
  data?: T;
  error?: {
    code: string;
    message: string;
    details?: Record<string, unknown>;
  };
  traceId?: string;
};

export type RequestOptions = {
  method?: "GET" | "POST" | "PUT" | "DELETE";
  data?: unknown;
  idempotencyKey?: string;
};

export function resolveUploadUrl(url: string): string {
  if (url.startsWith("/static/images/") && !AVAILABLE_LOCAL_IMAGES.has(url)) {
    return "";
  }
  const uploadBaseUrl = apiBaseUrl.replace(/\/$/, "");
  if (url.startsWith(UPLOADS_PREFIX)) {
    return `${uploadBaseUrl}${url}`;
  }
  return url.replace(LOCAL_UPLOAD_ORIGIN, uploadBaseUrl);
}

function normalizeUploadUrls<T>(value: T): T {
  if (typeof value === "string") {
    return resolveUploadUrl(value) as T;
  }
  if (Array.isArray(value)) {
    return value.map((item) => normalizeUploadUrls(item)) as T;
  }
  if (value && typeof value === "object") {
    const normalized: Record<string, unknown> = {};
    for (const [key, item] of Object.entries(value as Record<string, unknown>)) {
      normalized[key] = normalizeUploadUrls(item);
    }
    return normalized as T;
  }
  return value;
}

let _redirecting = false;

function redirectToLogin() {
  if (_redirecting) return;
  _redirecting = true;
  uni.removeStorageSync("xq_access_token");
  uni.showToast({ title: "请先登录", icon: "none", duration: 2000 });
  setTimeout(() => {
    reLaunchToPage("/pages/profile/index", "auth_expired");
    _redirecting = false;
  }, 500);
}

export function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const method = options.method ?? "GET";
  const requestStore = useRequestStore();
  const processId = requestStore.start(path, method);
  const clientTraceId = generateTraceId();
  const token = uni.getStorageSync("xq_access_token");
  const header: Record<string, string> = {
    "Content-Type": "application/json; charset=utf-8",
    "X-Trace-Id": clientTraceId,
  };

  if (token) {
    header.Authorization = `Bearer ${token}`;
  }

  if (options.idempotencyKey) {
    header["Idempotency-Key"] = options.idempotencyKey;
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${apiBaseUrl}${API_PREFIX}${path}`,
      method,
      data: options.data as string | Record<string, unknown> | ArrayBuffer | undefined,
      header,
      timeout: 10000,
      success(response) {
        const body = response.data as ApiResult<T> | T;
        const wrapped = isApiResult<T>(body) ? body : undefined;
        const traceId = wrapped?.traceId;

        if (response.statusCode === 401 || response.statusCode === 403) {
          requestStore.fail(processId, "auth_expired", response.statusCode, traceId);
          redirectToLogin();
          reject(new Error("登录已过期，请重新登录"));
          return;
        }

        if (response.statusCode < 200 || response.statusCode >= 300) {
          requestStore.fail(processId, wrapped?.error?.message || "http_error", response.statusCode, traceId);
          reject(new Error(wrapped?.error?.message || `请求失败 (${response.statusCode})`));
          return;
        }
        if (wrapped) {
          if (wrapped.success) {
            requestStore.succeed(processId, response.statusCode, traceId);
            resolve(normalizeUploadUrls(wrapped.data as T));
            return;
          }
          requestStore.fail(processId, wrapped.error?.message || wrapped.error?.code || "api_error", response.statusCode, traceId);
          reject(new Error(wrapped.error?.message || wrapped.error?.code || "请求失败"));
          return;
        }
        requestStore.succeed(processId, response.statusCode);
        resolve(normalizeUploadUrls(body as T));
      },
      fail(error) {
        requestStore.fail(processId, error.errMsg || "network_error");
        reject(new Error(error.errMsg || "网络连接失败"));
      },
    });
  });
}

export const apiPrefix = API_PREFIX;

function isApiResult<T>(body: ApiResult<T> | T): body is ApiResult<T> {
  return Boolean(body && typeof body === "object" && "success" in body);
}

function generateTraceId(): string {
  const ts = Date.now().toString(36);
  const rand = Math.random().toString(36).slice(2, 8);
  traceCounter = (traceCounter + 1) % 10000;
  return `mp-${ts}-${rand}-${traceCounter}`;
}
