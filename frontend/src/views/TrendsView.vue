<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import VChart from 'vue-echarts'
import { lineSeriesOption } from '../charts/echarts'
import { useSeriesStore } from '../stores/series'
import type { MetricType } from '../api/generated/types.gen'

const store = useSeriesStore()
const metric = ref<MetricType>('STEPS')
const metrics: MetricType[] = ['STEPS', 'SLEEP_SECONDS', 'RESTING_HEART_RATE_BPM', 'HRV_RMSSD_S', 'WEIGHT_KG', 'SCREEN_TIME_S']

const to = new Date().toISOString()
const from = new Date(Date.now() - 30 * 86400000).toISOString()

onMounted(() => store.fetchSeries(metric.value, from, to))

async function changeMetric() {
  await store.fetchSeries(metric.value, from, to)
}

const option = computed(() => {
  const s = store.series
  if (!s) return {}
  const bySource = new Map<string, [string, number][]>()
  for (const p of s.points) {
    const list = bySource.get(p.source) ?? []
    list.push([p.occurredAt, p.value])
    bySource.set(p.source, list)
  }
  return lineSeriesOption(`${s.metricType} (${s.unit})`, [...bySource.entries()].map(([name, data]) => ({ name, data })))
})
</script>

<template>
  <section>
    <h2>Trends</h2>
    <label>Metric
      <select v-model="metric" @change="changeMetric">
        <option v-for="m in metrics" :key="m" :value="m">{{ m }}</option>
      </select>
    </label>
    <p v-if="store.error" role="alert">{{ store.error }}</p>
    <VChart v-else :option="option" style="height: 360px" autoresize />
  </section>
</template>
