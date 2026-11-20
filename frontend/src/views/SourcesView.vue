<script setup lang="ts">
import { onMounted } from 'vue'
import { useSourcesStore } from '../stores/sources'

const store = useSourcesStore()

onMounted(() => store.fetchConnections())

async function connect(provider: 'withings' | 'garmin') {
  if (provider === 'withings') {
    const url = await store.startAuthorization('withings')
    if (url) window.location.href = url
  } else {
    await store.connectProvider('garmin')
  }
}

async function exportData(format: 'json' | 'csv') {
  const content = await store.exportData(format)
  if (!content) return
  const blob = new Blob([content], { type: format === 'csv' ? 'text/csv' : 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `healthhub-export.${format}`
  link.click()
  URL.revokeObjectURL(url)
}

async function deleteAllData() {
  if (!window.confirm('Delete all stored data and provider tokens? This runs asynchronously and cannot be undone.')) return
  await store.deleteAllData()
}
</script>

<template>
  <section>
    <h2>Sources</h2>
    <p v-if="store.error" role="alert">{{ store.error }}</p>
    <table v-else>
      <thead><tr><th>Provider</th><th>Label</th><th>Connected</th><th>Last sync</th><th>Status</th><th></th></tr></thead>
      <tbody>
        <tr v-for="c in store.connections" :key="c.id">
          <td>{{ c.provider }}</td>
          <td>{{ c.label ?? '—' }}</td>
          <td>{{ c.connectedAt }}</td>
          <td>{{ c.lastSyncAt ?? '—' }}</td>
          <td>{{ c.lastSyncStatus ?? '—' }}</td>
          <td><button @click="store.syncNow(c.id)">Sync now</button></td>
        </tr>
      </tbody>
    </table>
    <p v-if="!store.error && store.connections.length === 0">No connections yet.</p>
    <div class="actions">
      <button @click="connect('withings')">Connect Withings</button>
      <button @click="connect('garmin')">Connect Garmin</button>
      <button @click="exportData('json')">Export JSON</button>
      <button @click="exportData('csv')">Export CSV</button>
      <button class="danger" @click="deleteAllData">Delete all data</button>
    </div>
    <p v-if="store.lastSync">Last sync: {{ store.lastSync.ingested }} ingested, {{ store.lastSync.duplicates }} duplicates.</p>
    <p v-if="store.lastDeletionJob">Deletion job {{ store.lastDeletionJob.id }} accepted (status: {{ store.lastDeletionJob.status }}); completion arrives on the event stream.</p>
  </section>
</template>

<style scoped>
.actions { display: flex; gap: 8px; margin-top: 12px; flex-wrap: wrap; }
.danger { color: #b00020; }
</style>
