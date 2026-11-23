import { createI18n } from 'vue-i18n'
import { watch } from 'vue'
import en from './locales/en'
import fr from './locales/fr'
import ar from './locales/ar'
import zh from './locales/zh'
import ru from './locales/ru'

export const SUPPORTED_LOCALES = ['en', 'fr', 'ar', 'zh', 'ru'] as const
export type SupportedLocale = (typeof SUPPORTED_LOCALES)[number]

const RTL_LOCALES: ReadonlySet<string> = new Set(['ar'])

const STORAGE_KEY = 'healthhub-locale'

function detectInitialLocale(): SupportedLocale {
  try {
    const stored = window.localStorage.getItem(STORAGE_KEY)
    if (stored && (SUPPORTED_LOCALES as readonly string[]).includes(stored)) {
      return stored as SupportedLocale
    }
    const nav = (window.navigator.language ?? 'en').slice(0, 2).toLowerCase()
    if ((SUPPORTED_LOCALES as readonly string[]).includes(nav)) {
      return nav as SupportedLocale
    }
  } catch {
    // storage unavailable (private mode etc.) — fall through to 'en'
  }
  return 'en'
}

export const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: detectInitialLocale(),
  fallbackLocale: 'en',
  messages: { en, fr, ar, zh, ru }
})

function applyDocumentDirection(locale: string): void {
  document.documentElement.lang = locale
  document.documentElement.dir = RTL_LOCALES.has(locale) ? 'rtl' : 'ltr'
}

applyDocumentDirection(i18n.global.locale.value)

watch(i18n.global.locale, (locale) => {
  applyDocumentDirection(locale)
  try {
    window.localStorage.setItem(STORAGE_KEY, locale)
  } catch {
    // storage unavailable — locale just won't persist
  }
})
