<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import VChart from 'vue-echarts'
import { barSeriesOption, lineSeriesOption } from '../charts/echarts'
import { useMentalStore, useMoodStore } from '../stores/domain'
import { formatDate, formatDateTime, formatDurationS } from '../utils/format'

const { t } = useI18n()
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
    t('mental.screenTimeChart'),
    mental.usage.map(u => formatDate(u.date)),
    mental.usage.map(u => Math.round(u.totalScreenTimeS / 60))
  )
)

const moodOption = computed(() =>
  lineSeriesOption(t('mental.moodChart'), [{
    name: t('mental.valence'),
    data: mood.checkins.map(c => [c.occurredAt, c.valence])
  }])
)
</script>

<template>
  <section>
    <h2>{{ t('mental.title') }}</h2>
    <p v-if="mental.error || mood.error" role="alert">{{ t('errors.loadFailed') }}</p>
    <h3>{{ t('mental.screenTime') }}</h3>
    <VChart :option="usageOption" style="height: 280px" autoresize />
    <h3>{{ t('mental.sessions') }}</h3>
    <ul>
      <li v-for="s in mental.sessions" :key="s.id">{{ formatDateTime(s.startedAt) }} — {{ s.kind }} — {{ formatDurationS(s.durationS) }} ({{ s.source }})</li>
    </ul>
    <p v-if="mental.sessions.length === 0">{{ t('mental.sessionsEmpty') }}</p>
    <h3>{{ t('mental.mood') }}</h3>
    <VChart :option="moodOption" style="height: 240px" autoresize />
    <ul>
      <li v-for="c in mood.checkins" :key="c.id">{{ formatDateTime(c.occurredAt) }} — {{ t('mental.valence') }} {{ c.valence }} — {{ c.labels.join(', ') }}</li>
    </ul>
  </section>
</template>
