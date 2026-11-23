<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { SUPPORTED_LOCALES, type SupportedLocale } from '../i18n'

const { locale, t } = useI18n()

const names: Record<SupportedLocale, string> = {
  en: 'English',
  fr: 'Français',
  ar: 'العربية',
  zh: '中文',
  ru: 'Русский'
}
</script>

<template>
  <label class="switcher">
    <span>{{ t('app.language') }}</span>
    <select v-model="locale" :aria-label="t('app.language')">
      <option v-for="l in SUPPORTED_LOCALES" :key="l" :value="l" :lang="l">{{ names[l] }}</option>
    </select>
  </label>
</template>

<style scoped>
.switcher { display: inline-flex; align-items: center; gap: 6px; font-size: 0.9rem; }
select { font: inherit; background: var(--surface); color: var(--fg); border: 1px solid var(--border); border-radius: 6px; padding: 4px 6px; min-height: 32px; }
</style>
