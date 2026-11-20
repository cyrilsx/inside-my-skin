import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import App from './App.vue'
import { router } from './router'

describe('App', () => {
  it('renders the HealthHub heading', async () => {
    router.push('/')
    await router.isReady()
    const wrapper = mount(App, {
      global: {
        plugins: [createPinia(), router]
      }
    })
    expect(wrapper.find('h1').text()).toBe('HealthHub')
  })
})
