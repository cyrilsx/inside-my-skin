<script setup lang="ts">
import { onMounted } from 'vue'
import { useActivitiesStore } from '../stores/domain'
import { formatDateTime, formatDistanceM, formatDurationS } from '../utils/format'

const store = useActivitiesStore()
const to = new Date().toISOString()
const from = new Date(Date.now() - 30 * 86400000).toISOString()

onMounted(() => store.fetchActivities(from, to))
</script>

<template>
  <section>
    <h2>Activities</h2>
    <p v-if="store.error" role="alert">{{ store.error }}</p>
    <table v-else>
      <caption>Activities in the last 30 days</caption>
      <thead><tr><th scope="col">Started</th><th scope="col">Type</th><th scope="col">Title</th><th scope="col">Duration</th><th scope="col">Distance</th><th scope="col">Source</th></tr></thead>
      <tbody>
        <tr v-for="a in store.items" :key="a.id">
          <td>{{ formatDateTime(a.startedAt) }}</td>
          <td>{{ a.type }}</td>
          <td>{{ a.title ?? '—' }}</td>
          <td>{{ formatDurationS(a.durationS) }}</td>
          <td>{{ a.distanceM != null ? formatDistanceM(a.distanceM) : '—' }}</td>
          <td>{{ a.source }}</td>
        </tr>
      </tbody>
    </table>
    <p v-if="!store.error && store.items.length === 0">No activities in the last 30 days.</p>
  </section>
</template>
