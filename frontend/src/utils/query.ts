export function buildQuery(params: Record<string, unknown>): string {
  const pairs: string[] = [];
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === "") return;
    pairs.push(`${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`);
  });
  return pairs.join("&");
}

export function buildPageUrl(path: string, params: Record<string, unknown> = {}): string {
  const query = buildQuery(params);
  return query ? `${path}?${query}` : path;
}
