<script setup lang="ts">
import { onMounted } from 'vue'
import { useActivitiesStore } from '../stores/domain'

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
      <thead><tr><th>Started</th><th>Type</th><th>Title</th><th>Duration</th><th>Distance</th><th>Source</th></tr></thead>
      <tbody>
        <tr v-for="a in store.items" :key="a.id">
          <td>{{ a.startedAt }}</td>
          <td>{{ a.type }}</td>
          <td>{{ a.title ?? '—' }}</td>
          <td>{{ Math.round(a.durationS / 60) }} min</td>
          <td>{{ a.distanceM != null ? (a.distanceM / 1000).toFixed(2) + ' km' : '—' }}</td>
          <td>{{ a.source }}</td>
        </tr>
      </tbody>
    </table>
    <p v-if="!store.error && store.items.length === 0">No activities in the last 30 days.</p>
  </section>
</template>
