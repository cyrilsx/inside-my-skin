import { describe, expect, it } from 'vitest'
import { apiClient, sdk } from './client'
import type { Daily, MetricType } from './generated/types.gen'

describe('generated API client', () => {
  it('is generated from docs/openapi.yaml and exposes typed operations', () => {
    expect(apiClient).toBeDefined()
    expect(typeof sdk.getDailies).toBe('function')
    expect(typeof sdk.ingestBatch).toBe('function')
    const metric: MetricType = 'STEPS'
    expect(metric).toBe('STEPS')
    const daily: Partial<Daily> = { date: '2026-03-29', sources: ['GARMIN'] }
    expect(daily.sources).toContain('GARMIN')
  })
})
