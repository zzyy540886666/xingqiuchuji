import dayjs from 'dayjs'

export function formatTime(iso: string, format = 'YYYY-MM-DD HH:mm:ss'): string {
  if (!iso) return '-'
  return dayjs(iso).format(format)
}

export function formatDate(iso: string): string {
  return formatTime(iso, 'YYYY-MM-DD')
}
