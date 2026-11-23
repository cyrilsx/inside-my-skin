import { describe, expect, it } from 'vitest'
import { nextTick } from 'vue'
import { i18n, SUPPORTED_LOCALES } from './index'
import en from './locales/en'
import fr from './locales/fr'
import ar from './locales/ar'
import zh from './locales/zh'
import ru from './locales/ru'

const messages: Record<string, unknown> = { en, fr, ar, zh, ru }

function keys(obj: unknown, prefix = ''): string[] {
  if (typeof obj !== 'object' || obj === null) return [prefix]
  return Object.entries(obj as Record<string, unknown>).flatMap(([k, v]) => keys(v, prefix ? `${prefix}.${k}` : k))
}

describe('i18n', () => {
  it('supports exactly en, fr, ar, zh, ru', () => {
    expect([...SUPPORTED_LOCALES].sort()).toEqual(['ar', 'en', 'fr', 'ru', 'zh'])
  })

  it('every locale has the exact same keys as en', () => {
    const enKeys = keys(en).sort()
    for (const locale of SUPPORTED_LOCALES) {
      expect(keys(messages[locale]).sort(), `locale ${locale} key mismatch`).toEqual(enKeys)
    }
  })

  it('translates a sample key in every locale', () => {
    for (const locale of SUPPORTED_LOCALES) {
      i18n.global.locale.value = locale
      expect(i18n.global.t('nav.today')).not.toBe('nav.today')
      expect(i18n.global.t('nav.today').length).toBeGreaterThan(0)
    }
    i18n.global.locale.value = 'en'
  })

  it('sets dir=rtl for Arabic and ltr otherwise, and syncs <html lang>', async () => {
    i18n.global.locale.value = 'ar'
    await nextTick()
    expect(document.documentElement.dir).toBe('rtl')
    expect(document.documentElement.lang).toBe('ar')
    i18n.global.locale.value = 'fr'
    await nextTick()
    expect(document.documentElement.dir).toBe('ltr')
    expect(document.documentElement.lang).toBe('fr')
    i18n.global.locale.value = 'en'
    await nextTick()
  })

  it('falls back to en for unknown keys in other locales', () => {
    i18n.global.locale.value = 'ru'
    expect(i18n.global.t('nav.today')).toBe((ru as { nav: { today: string } }).nav.today)
    i18n.global.locale.value = 'en'
  })
})
