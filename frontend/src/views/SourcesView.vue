<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSourcesStore } from '../stores/sources'
import ConfirmDialog from '../components/ConfirmDialog.vue'
import { formatDateTime } from '../utils/format'

const { t } = useI18n()
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
    <h2>{{ t('sources.title') }}</h2>
    <p v-if="store.error" role="alert">{{ t('errors.loadFailed') }}</p>
    <table v-else>
      <caption>{{ t('sources.caption') }}</caption>
      <thead><tr>
        <th scope="col">{{ t('sources.provider') }}</th>
        <th scope="col">{{ t('sources.label') }}</th>
        <th scope="col">{{ t('sources.connected') }}</th>
        <th scope="col">{{ t('sources.lastSync') }}</th>
        <th scope="col">{{ t('sources.status') }}</th>
        <th scope="col"><span class="visually-hidden">{{ t('sources.actions') }}</span></th>
      </tr></thead>
      <tbody>
        <tr v-for="c in store.connections" :key="c.id">
          <td translate="no">{{ c.provider }}</td>
          <td>{{ c.label ?? t('common.none') }}</td>
          <td>{{ c.connectedAt ? formatDateTime(c.connectedAt) : t('common.none') }}</td>
          <td>{{ c.lastSyncAt ? formatDateTime(c.lastSyncAt) : t('common.none') }}</td>
          <td>{{ c.lastSyncStatus ?? t('common.none') }}</td>
          <td><button @click="store.syncNow(c.id)">{{ t('sources.syncNow') }}</button></td>
        </tr>
      </tbody>
    </table>
    <p v-if="!store.error && store.connections.length === 0">{{ t('sources.empty') }}</p>
    <div class="actions">
      <button @click="connect('withings')">{{ t('sources.connectWithings') }}</button>
      <button @click="connect('garmin')">{{ t('sources.connectGarmin') }}</button>
      <button @click="exportData('json')">{{ t('sources.exportJson') }}</button>
      <button @click="exportData('csv')">{{ t('sources.exportCsv') }}</button>
      <button class="danger" @click="askDeleteAllData">{{ t('sources.deleteAll') }}</button>
    </div>
    <p v-if="store.lastSync" role="status">{{ t('sources.lastSyncResult', { ingested: store.lastSync.ingested, duplicates: store.lastSync.duplicates }) }}</p>
    <p v-if="store.lastDeletionJob" role="status">{{ t('sources.deletionAccepted', { id: store.lastDeletionJob.id, status: store.lastDeletionJob.status }) }}</p>
    <ConfirmDialog
      ref="confirmDialog"
      :title="t('dialog.deleteTitle')"
      :message="t('dialog.deleteMessage')"
      :confirm-label="t('dialog.deleteConfirm')"
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
