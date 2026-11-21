import { describe, expect, it } from 'vitest'
import { router } from './index'

describe('router', () => {
  it('redirects / to /today', async () => {
    await router.push('/')
    await router.isReady()
    expect(router.currentRoute.value.path).toBe('/today')
  })

  it('resolves unknown paths to the 404 view instead of a blank page', async () => {
    await router.push('/definitely-not-a-page')
    await router.isReady()
    expect(router.currentRoute.value.name).toBe('not-found')
  })

  it('lazy-loads every main view', async () => {
    const names: Record<string, string> = {
      '/today': 'today',
      '/trends': 'trends',
      '/activities': 'activities',
      '/mental': 'mental',
      '/sources': 'sources'
    }
    for (const [path, name] of Object.entries(names)) {
      await router.push(path)
      await router.isReady()
      expect(router.currentRoute.value.name).toBe(name)
      expect(router.currentRoute.value.matched.length).toBeGreaterThan(0)
    }
  })
})
