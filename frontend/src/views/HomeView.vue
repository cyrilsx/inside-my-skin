<script setup lang="ts">
import { useAppStore } from '../stores/app'

const appStore = useAppStore()
</script>

<template>
  <section>
    <h2>{{ appStore.title }}</h2>
    <p>Connected data sources and dashboards will appear here.</p>
  </section>
</template>
