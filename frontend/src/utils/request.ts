export const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
const API_PREFIX = "/api/v1";
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

export function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
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
      method: options.method ?? "GET",
      data: options.data as string | Record<string, unknown> | ArrayBuffer | undefined,
      header,
      timeout: 10000,
      success(response) {
        const body = response.data as ApiResult<T> | T;
        if (body && typeof body === "object" && "success" in body) {
          const wrapped = body as ApiResult<T>;
          if (wrapped.success) {
            resolve(wrapped.data as T);
            return;
          }
          reject(new Error(wrapped.error?.message || wrapped.error?.code || "请求失败"));
          return;
        }
        resolve(body as T);
      },
      fail(error) {
        reject(new Error(error.errMsg || "网络连接失败"));
      },
    });
  });
}

export const apiPrefix = API_PREFIX;

function generateTraceId(): string {
  const ts = Date.now().toString(36);
  const rand = Math.random().toString(36).slice(2, 8);
  traceCounter = (traceCounter + 1) % 10000;
  return `mp-${ts}-${rand}-${traceCounter}`;
}
