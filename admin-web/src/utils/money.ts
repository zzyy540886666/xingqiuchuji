export function formatMoney(fen: number): string {
  const yuan = (fen / 100).toFixed(2)
  return `¥${yuan}`
}

export function fenToYuan(fen: number): string {
  return (fen / 100).toFixed(2)
}

export function yuanToFen(yuan: number): number {
  return Math.round(yuan * 100)
}
