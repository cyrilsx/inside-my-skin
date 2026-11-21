const dateFormat = new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' })
const dateTimeFormat = new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' })
const numberFormat = new Intl.NumberFormat(undefined)

export function formatDate(isoDate: string): string {
  return dateFormat.format(new Date(isoDate + (isoDate.length === 10 ? 'T00:00:00Z' : '')))
}

export function formatDateTime(isoDateTime: string): string {
  return dateTimeFormat.format(new Date(isoDateTime))
}

export function formatNumber(value: number): string {
  return numberFormat.format(value)
}

export function formatDurationS(seconds: number): string {
  const minutes = Math.round(seconds / 60)
  return `${formatNumber(minutes)} min`
}

export function formatDistanceM(meters: number): string {
  return `${(meters / 1000).toFixed(2)} km`
}

export function formatWeightKg(kg: number): string {
  return `${kg} kg`
}

export function formatHoursFromSeconds(seconds: number): string {
  return `${(Math.round(seconds / 3600 * 10) / 10).toLocaleString(undefined)} h`
}
