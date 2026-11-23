import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import App from './App.vue'
import { router } from './router'
import { i18n } from './i18n'

describe('App', () => {
  it('renders the HealthHub heading', async () => {
    router.push('/')
    await router.isReady()
    const wrapper = mount(App, {
      global: {
        plugins: [createPinia(), router, i18n]
      }
    })
    expect(wrapper.find('h1').text()).toBe('HealthHub')
  })

  it('renders the language switcher with all five locales in their own language', async () => {
    router.push('/')
    await router.isReady()
    const wrapper = mount(App, {
      global: {
        plugins: [createPinia(), router, i18n]
      }
    })
    const options = wrapper.findAll('select option')
    expect(options.map(o => o.text())).toEqual(['English', 'Français', 'العربية', '中文', 'Русский'])
    expect(options.map(o => o.attributes('lang'))).toEqual(['en', 'fr', 'ar', 'zh', 'ru'])
  })
})
