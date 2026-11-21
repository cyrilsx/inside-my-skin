<script setup lang="ts">
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { useAppStore } from './stores/app'

const route = useRoute()
const appStore = useAppStore()

const links = [
  { to: '/today', label: 'Today' },
  { to: '/trends', label: 'Trends' },
  { to: '/activities', label: 'Activities' },
  { to: '/mental', label: 'Mental' },
  { to: '/sources', label: 'Sources' }
]
</script>

<template>
  <a class="skip-link" href="#main">Skip to main content</a>
  <header>
    <h1>HealthHub</h1>
    <p>Your health and wellbeing data, through one canonical API.</p>
    <nav aria-label="Main">
      <RouterLink
        v-for="l in links"
        :key="l.to"
        :to="l.to"
        :aria-current="route.path === l.to ? 'page' : undefined"
      >{{ l.label }}</RouterLink>
    </nav>
  </header>
  <main id="main" tabindex="-1">
    <p v-if="appStore.error" role="alert">{{ appStore.error }}</p>
    <RouterView />
  </main>
  <footer>
    <p>Private dashboard — data stays on your own server. Screen time values are estimates.</p>
  </footer>
</template>

<style scoped>
header { padding: 16px 24px 0; }
nav { display: flex; gap: 16px; margin: 8px 0 16px; flex-wrap: wrap; }
nav a { padding: 8px 4px; min-height: 24px; }
nav a.router-link-active { font-weight: 700; text-decoration-thickness: 3px; }
main { padding: 0 24px 24px; max-width: 72rem; }
main:focus { outline: none; }
footer { padding: 16px 24px; color: var(--muted); font-size: 0.85rem; border-top: 1px solid var(--border); }
</style>
