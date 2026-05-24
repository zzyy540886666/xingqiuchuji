export function formatAmount(amount: number): string {
  if (!Number.isInteger(amount)) {
    throw new Error("amount must be integer minor unit");
  }
  const sign = amount < 0 ? "-" : "";
  const absolute = Math.abs(amount);
  const yuan = Math.floor(absolute / 100);
  const cents = String(absolute % 100).padStart(2, "0");
  return `${sign}¥${yuan.toLocaleString("zh-CN")}.${cents}`;
}

export function formatPriceCompact(amount: number): string {
  if (!Number.isInteger(amount)) {
    throw new Error("amount must be integer minor unit");
  }
  return `¥${Math.round(amount / 100).toLocaleString("zh-CN")}`;
}
