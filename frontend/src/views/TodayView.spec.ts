import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'

vi.mock('../api/client', () => ({
  apiClient: {},
  sdk: {
    getDailies: vi.fn(async () => ({
      data: {
        items: [{
          date: new Date().toISOString().slice(0, 10),
          sources: ['HEALTH_CONNECT'],
          steps: 1234,
          sleepSeconds: 27000,
          screenTimeS: 5400
        }]
      },
      error: undefined
    }))
  }
}))

import TodayView from './TodayView.vue'

describe('TodayView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders today\'s cards from the store', async () => {
    const wrapper = mount(TodayView)
    await new Promise(resolve => setTimeout(resolve, 0))
    await wrapper.vm.$nextTick()
    expect(wrapper.text()).toContain('1234')
    expect(wrapper.text()).toContain('7.5 h')
    expect(wrapper.text()).toContain('90 min')
    expect(wrapper.text()).toContain('HEALTH_CONNECT')
  })
})
