export function readJsonStorage<T>(key: string, fallback: T): T {
  const raw = uni.getStorageSync(key);
  if (!raw) return fallback;

  try {
    return JSON.parse(String(raw)) as T;
  } catch {
    uni.removeStorageSync(key);
    return fallback;
  }
}

export function writeJsonStorage<T>(key: string, value: T) {
  uni.setStorageSync(key, JSON.stringify(value));
}
