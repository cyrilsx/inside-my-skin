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
    </div>
    <p v-if="store.lastSync">Last sync: {{ store.lastSync.ingested }} ingested, {{ store.lastSync.duplicates }} duplicates.</p>
  </section>
</template>

<style scoped>
.actions { display: flex; gap: 8px; margin-top: 12px; }
</style>
