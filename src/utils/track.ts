export function trackEvent(name: string, payload: Record<string, unknown> = {}) {
  const event = {
    name,
    payload,
    occurredAt: Date.now()
  }
  console.info('[track]', event)
}
