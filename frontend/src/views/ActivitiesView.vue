<script setup lang="ts">
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useActivitiesStore } from '../stores/domain'
import { formatDateTime, formatDistanceM, formatDurationS } from '../utils/format'

const { t } = useI18n()
const store = useActivitiesStore()
const to = new Date().toISOString()
const from = new Date(Date.now() - 30 * 86400000).toISOString()

onMounted(() => store.fetchActivities(from, to))
</script>

<template>
  <section>
    <h2>{{ t('activities.title') }}</h2>
    <p v-if="store.error" role="alert">{{ t('errors.loadFailed') }}</p>
    <table v-else>
      <caption>{{ t('activities.caption') }}</caption>
      <thead><tr>
        <th scope="col">{{ t('activities.started') }}</th>
        <th scope="col">{{ t('activities.type') }}</th>
        <th scope="col">{{ t('activities.activityTitle') }}</th>
        <th scope="col">{{ t('activities.duration') }}</th>
        <th scope="col">{{ t('activities.distance') }}</th>
        <th scope="col">{{ t('activities.source') }}</th>
      </tr></thead>
      <tbody>
        <tr v-for="a in store.items" :key="a.id">
          <td>{{ formatDateTime(a.startedAt) }}</td>
          <td translate="no">{{ a.type }}</td>
          <td>{{ a.title ?? t('common.none') }}</td>
          <td>{{ formatDurationS(a.durationS) }}</td>
          <td>{{ a.distanceM != null ? formatDistanceM(a.distanceM) : t('common.none') }}</td>
          <td translate="no">{{ a.source }}</td>
        </tr>
      </tbody>
    </table>
    <p v-if="!store.error && store.items.length === 0">{{ t('activities.empty') }}</p>
  </section>
</template>
