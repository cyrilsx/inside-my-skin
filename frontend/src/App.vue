<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { useAppStore } from './stores/app'
import LanguageSwitcher from './components/LanguageSwitcher.vue'

const { t } = useI18n()
const route = useRoute()
const appStore = useAppStore()

const links = [
  { to: '/today', key: 'nav.today' },
  { to: '/trends', key: 'nav.trends' },
  { to: '/activities', key: 'nav.activities' },
  { to: '/mental', key: 'nav.mental' },
  { to: '/sources', key: 'nav.sources' }
] as const
</script>

<template>
  <a class="skip-link" href="#main">{{ t('common.skipToMain') }}</a>
  <header>
    <h1>HealthHub</h1>
    <p>{{ t('app.tagline') }}</p>
    <nav aria-label="Main">
      <RouterLink
        v-for="l in links"
        :key="l.to"
        :to="l.to"
        :aria-current="route.path === l.to ? 'page' : undefined"
      >{{ t(l.key) }}</RouterLink>
      <LanguageSwitcher />
    </nav>
  </header>
  <main id="main" tabindex="-1">
    <p v-if="appStore.error" role="alert">{{ appStore.error }}</p>
    <RouterView />
  </main>
  <footer>
    <p>{{ t('app.footer') }}</p>
  </footer>
</template>

<style scoped>
header { padding: 16px 24px 0; }
nav { display: flex; gap: 16px; margin: 8px 0 16px; flex-wrap: wrap; align-items: center; }
nav a { padding: 8px 4px; min-height: 24px; }
nav a.router-link-active { font-weight: 700; text-decoration-thickness: 3px; }
main { padding: 0 24px 24px; max-width: 72rem; }
main:focus { outline: none; }
footer { padding: 16px 24px; color: var(--muted); font-size: 0.85rem; border-top: 1px solid var(--border); }
</style>
