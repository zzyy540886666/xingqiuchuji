export function formatMoney(valueInCent: number): string {
  return `¥${(valueInCent / 100).toFixed(valueInCent % 100 === 0 ? 0 : 2)}`
}

export function maskMobile(mobile: string): string {
  return mobile.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

export function compactText(text: string, max = 12): string {
  return text.length > max ? `${text.slice(0, max)}...` : text
}
