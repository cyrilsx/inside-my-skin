import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

vi.mock('../api/client', () => ({
  apiClient: {},
  sdk: {
    getDailies: vi.fn(async () => ({
      data: { items: [{ date: '2026-03-29', sources: ['GARMIN'], steps: 8421 }] },
      error: undefined
    }))
  }
}))

import { useDailiesStore } from './dailies'

describe('dailies store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('fetchDailies populates items from the generated client', async () => {
    const store = useDailiesStore()
    await store.fetchDailies('2026-03-22T00:00:00Z', '2026-03-29T23:59:59Z')
    expect(store.items).toHaveLength(1)
    expect(store.items[0].steps).toBe(8421)
    expect(store.error).toBeNull()
  })
})
