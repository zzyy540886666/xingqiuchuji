let counter = 0

export function generateTraceId(): string {
  const ts = Date.now().toString(36)
  const rand = Math.random().toString(36).slice(2, 8)
  counter = (counter + 1) % 10000
  return `adm-${ts}-${rand}-${counter}`
}
