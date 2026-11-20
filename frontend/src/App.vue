<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'

const links = [
  { to: '/today', label: 'Today' },
  { to: '/trends', label: 'Trends' },
  { to: '/activities', label: 'Activities' },
  { to: '/mental', label: 'Mental' },
  { to: '/sources', label: 'Sources' }
]
</script>

<template>
  <main>
    <header>
      <h1>HealthHub</h1>
      <p>Your health and wellbeing data, through one canonical API.</p>
      <nav>
        <RouterLink v-for="l in links" :key="l.to" :to="l.to">{{ l.label }}</RouterLink>
      </nav>
    </header>
    <RouterView />
  </main>
</template>

<style scoped>
nav { display: flex; gap: 16px; margin: 8px 0 16px; }
nav a.router-link-active { font-weight: 700; }
</style>
