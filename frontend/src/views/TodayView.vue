<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useDailiesStore } from '../stores/dailies'
import { formatDate, formatHoursFromSeconds, formatNumber, formatWeightKg } from '../utils/format'

const { t } = useI18n()
const store = useDailiesStore()

const today = new Date().toISOString().slice(0, 10)
const weekAgo = new Date(Date.now() - 7 * 86400000).toISOString().slice(0, 10)

onMounted(() => store.fetchDailies(weekAgo, today))

const latest = computed(() => store.items[store.items.length - 1])
</script>

<template>
  <section>
    <h2>{{ t('today.title') }}</h2>
    <p v-if="store.error" role="alert">{{ t('errors.loadFailed') }}</p>
    <p v-else-if="store.loading">{{ t('common.loading') }}</p>
    <div v-else-if="latest" class="cards">
      <div class="card"><span class="value">{{ latest.steps != null ? formatNumber(latest.steps) : t('common.none') }}</span><span class="label">{{ t('today.steps') }}</span></div>
      <div class="card"><span class="value">{{ latest.sleepSeconds != null ? formatHoursFromSeconds(latest.sleepSeconds) : t('common.none') }}</span><span class="label">{{ t('today.sleep') }}</span></div>
      <div class="card"><span class="value">{{ latest.restingHeartRateBpm ?? t('common.none') }}</span><span class="label">{{ t('today.restingHr') }}</span></div>
      <div class="card"><span class="value">{{ latest.weightKg != null ? formatWeightKg(latest.weightKg) : t('common.none') }}</span><span class="label">{{ t('today.weight') }}</span></div>
      <div class="card"><span class="value">{{ latest.screenTimeS != null ? formatHoursFromSeconds(latest.screenTimeS) : t('common.none') }}</span><span class="label">{{ t('today.screenTime') }}</span></div>
      <div class="card"><span class="value">{{ latest.sources.join(', ') }}</span><span class="label">{{ t('today.sources') }}</span></div>
    </div>
    <p v-else>{{ t('today.noData', { date: formatDate(today) }) }}</p>
  </section>
</template>

<style scoped>
.cards { display: flex; flex-wrap: wrap; gap: 12px; }
.card { border: 1px solid var(--border); border-radius: 8px; padding: 12px 16px; min-width: 120px; display: flex; flex-direction: column; background: var(--surface); }
.value { font-size: 1.4rem; font-weight: 600; }
.label { color: var(--muted); font-size: 0.8rem; }
</style>
