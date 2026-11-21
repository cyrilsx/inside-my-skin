<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useDailiesStore } from '../stores/dailies'
import { formatDate, formatDurationS, formatHoursFromSeconds, formatNumber, formatWeightKg } from '../utils/format'

const store = useDailiesStore()

const today = new Date().toISOString().slice(0, 10)
const weekAgo = new Date(Date.now() - 7 * 86400000).toISOString().slice(0, 10)

onMounted(() => store.fetchDailies(weekAgo, today))

const latest = computed(() => store.items[store.items.length - 1])
</script>

<template>
  <section>
    <h2>Today</h2>
    <p v-if="store.error" role="alert">{{ store.error }}</p>
    <p v-else-if="store.loading">Loading…</p>
    <div v-else-if="latest" class="cards">
      <div class="card"><span class="value">{{ latest.steps != null ? formatNumber(latest.steps) : '—' }}</span><span class="label">steps</span></div>
      <div class="card"><span class="value">{{ latest.sleepSeconds != null ? formatHoursFromSeconds(latest.sleepSeconds) : '—' }}</span><span class="label">sleep</span></div>
      <div class="card"><span class="value">{{ latest.restingHeartRateBpm ?? '—' }}</span><span class="label">resting HR (bpm)</span></div>
      <div class="card"><span class="value">{{ latest.weightKg != null ? formatWeightKg(latest.weightKg) : '—' }}</span><span class="label">weight</span></div>
      <div class="card"><span class="value">{{ latest.screenTimeS != null ? formatDurationS(latest.screenTimeS) : '—' }}</span><span class="label">screen time (estimate)</span></div>
      <div class="card"><span class="value">{{ latest.sources.join(', ') }}</span><span class="label">sources</span></div>
    </div>
    <p v-else>No data yet for {{ formatDate(today) }}.</p>
  </section>
</template>

<style scoped>
.cards { display: flex; flex-wrap: wrap; gap: 12px; }
.card { border: 1px solid var(--border); border-radius: 8px; padding: 12px 16px; min-width: 120px; display: flex; flex-direction: column; background: var(--surface); }
.value { font-size: 1.4rem; font-weight: 600; }
.label { color: var(--muted); font-size: 0.8rem; }
</style>
