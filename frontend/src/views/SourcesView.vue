<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useSourcesStore } from '../stores/sources'
import ConfirmDialog from '../components/ConfirmDialog.vue'
import { formatDateTime } from '../utils/format'

const store = useSourcesStore()
const confirmDialog = ref<InstanceType<typeof ConfirmDialog> | null>(null)

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

function askDeleteAllData() {
  confirmDialog.value?.open()
}

async function confirmDeleteAllData() {
  await store.deleteAllData()
}
</script>

<template>
  <section>
    <h2>Sources</h2>
    <p v-if="store.error" role="alert">{{ store.error }}</p>
    <table v-else>
      <caption>Connected data sources</caption>
      <thead><tr><th scope="col">Provider</th><th scope="col">Label</th><th scope="col">Connected</th><th scope="col">Last sync</th><th scope="col">Status</th><th scope="col"><span class="visually-hidden">Actions</span></th></tr></thead>
      <tbody>
        <tr v-for="c in store.connections" :key="c.id">
          <td>{{ c.provider }}</td>
          <td>{{ c.label ?? '—' }}</td>
          <td>{{ c.connectedAt ? formatDateTime(c.connectedAt) : '—' }}</td>
          <td>{{ c.lastSyncAt ? formatDateTime(c.lastSyncAt) : '—' }}</td>
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
      <button class="danger" @click="askDeleteAllData">Delete all data</button>
    </div>
    <p v-if="store.lastSync" role="status">Last sync: {{ store.lastSync.ingested }} ingested, {{ store.lastSync.duplicates }} duplicates.</p>
    <p v-if="store.lastDeletionJob" role="status">Deletion job {{ store.lastDeletionJob.id }} accepted (status: {{ store.lastDeletionJob.status }}); completion arrives on the event stream.</p>
    <ConfirmDialog
      ref="confirmDialog"
      title="Delete all data"
      message="Delete all stored data and provider tokens? This runs asynchronously and cannot be undone."
      confirm-label="Delete all data"
      @confirm="confirmDeleteAllData"
    />
  </section>
</template>

<style scoped>
.actions { display: flex; gap: 8px; margin-top: 12px; flex-wrap: wrap; }
.visually-hidden {
  position: absolute;
  width: 1px; height: 1px;
  margin: -1px; padding: 0; border: 0;
  clip: rect(0 0 0 0);
  overflow: hidden;
}
</style>
