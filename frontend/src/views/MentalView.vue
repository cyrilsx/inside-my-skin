<script setup lang="ts">
import { computed, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { barSeriesOption, lineSeriesOption } from '../charts/echarts'
import { useMentalStore, useMoodStore } from '../stores/domain'

const mental = useMentalStore()
const mood = useMoodStore()
const to = new Date().toISOString().slice(0, 10)
const from = new Date(Date.now() - 30 * 86400000).toISOString().slice(0, 10)

onMounted(() => {
  mental.fetchMental(from + 'T00:00:00Z', to + 'T23:59:59Z')
  mood.fetchMood(from + 'T00:00:00Z', to + 'T23:59:59Z')
})

const usageOption = computed(() =>
  barSeriesOption(
    'Screen time per day (minutes, estimate)',
    mental.usage.map(u => u.date),
    mental.usage.map(u => Math.round(u.totalScreenTimeS / 60))
  )
)

const moodOption = computed(() =>
  lineSeriesOption('Mood valence', [{
    name: 'valence',
    data: mood.checkins.map(c => [c.occurredAt, c.valence])
  }])
)
</script>

<template>
  <section>
    <h2>Mental</h2>
    <p v-if="mental.error || mood.error" role="alert">{{ mental.error ?? mood.error }}</p>
    <h3>Screen time</h3>
    <VChart :option="usageOption" style="height: 280px" autoresize />
    <h3>Sessions</h3>
    <ul>
      <li v-for="s in mental.sessions" :key="s.id">{{ s.startedAt }} — {{ s.kind }} — {{ Math.round(s.durationS / 60) }} min ({{ s.source }})</li>
    </ul>
    <p v-if="mental.sessions.length === 0">No sessions in the last 30 days.</p>
    <h3>Mood check-ins</h3>
    <VChart :option="moodOption" style="height: 240px" autoresize />
    <ul>
      <li v-for="c in mood.checkins" :key="c.id">{{ c.occurredAt }} — valence {{ c.valence }} — {{ c.labels.join(', ') }}</li>
    </ul>
  </section>
</template>
