import { describe, expect, it } from 'vitest'
import { formatDate, formatDateTime, formatDistanceM, formatDurationS, formatNumber } from './format'

describe('format utils (Intl-based, locale-aware)', () => {
  it('formats an ISO date without timezone surprises', () => {
    expect(formatDate('2026-03-29')).toBe(new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date('2026-03-29T00:00:00Z')))
  })

  it('formats an ISO date-time', () => {
    const out = formatDateTime('2026-03-29T08:30:00+02:00')
    expect(out).toContain('2026')
  })

  it('formats numbers with grouping', () => {
    expect(formatNumber(8421)).toBe(new Intl.NumberFormat(undefined).format(8421))
  })

  it('formats durations as minutes and distances as km', () => {
    expect(formatDurationS(2520)).toBe('42 min')
    expect(formatDistanceM(5000)).toBe('5.00 km')
  })
})
