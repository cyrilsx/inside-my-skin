import { i18n } from '../i18n'

function locale(): string {
  return i18n.global.locale.value
}

export function formatDate(isoDate: string): string {
  return new Intl.DateTimeFormat(locale(), { dateStyle: 'medium' })
    .format(new Date(isoDate + (isoDate.length === 10 ? 'T00:00:00Z' : '')))
}

export function formatDateTime(isoDateTime: string): string {
  return new Intl.DateTimeFormat(locale(), { dateStyle: 'medium', timeStyle: 'short' })
    .format(new Date(isoDateTime))
}

export function formatNumber(value: number): string {
  return new Intl.NumberFormat(locale()).format(value)
}

export function formatDurationS(seconds: number): string {
  return i18n.global.t('common.minutes', { count: formatNumber(Math.round(seconds / 60)) })
}

export function formatDistanceM(meters: number): string {
  return i18n.global.t('common.kilometers', { count: (meters / 1000).toFixed(2) })
}

export function formatWeightKg(kg: number): string {
  return i18n.global.t('common.kilograms', { count: kg })
}

export function formatHoursFromSeconds(seconds: number): string {
  return i18n.global.t('common.hours', { count: (Math.round(seconds / 3600 * 10) / 10).toLocaleString(locale()) })
}
