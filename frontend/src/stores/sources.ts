import { defineStore } from 'pinia'
import { ref } from 'vue'
import { apiClient, sdk } from '../api/client'
import type { Connection, DeletionJob, SyncResult } from '../api/generated/types.gen'

export const useSourcesStore = defineStore('sources', () => {
  const connections = ref<Connection[]>([])
  const error = ref<string | null>(null)
  const lastSync = ref<SyncResult | null>(null)
  const lastDeletionJob = ref<DeletionJob | null>(null)

  async function fetchConnections(): Promise<void> {
    const result = await sdk.listConnections({ client: apiClient })
    if (result.error) {
      error.value = 'Request failed'
      connections.value = []
    } else {
      error.value = null
      connections.value = result.data?.items ?? []
    }
  }

  async function syncNow(id: string): Promise<void> {
    const result = await sdk.syncConnection({ client: apiClient, path: { id } })
    if (!result.error) {
      lastSync.value = result.data ?? null
      await fetchConnections()
    }
  }

  async function connectProvider(provider: 'withings' | 'garmin'): Promise<void> {
    await sdk.connectProvider({ client: apiClient, path: { provider } })
    await fetchConnections()
  }

  async function startAuthorization(provider: 'withings' | 'garmin'): Promise<string | null> {
    const result = await sdk.startAuthorization({ client: apiClient, path: { provider } })
    return result.data?.authorizationUrl ?? null
  }

  async function exportData(format: 'json' | 'csv'): Promise<string | null> {
    const result = await sdk.exportData({ client: apiClient, query: { format } })
    if (result.error) {
      error.value = 'Export failed'
      return null
    }
    const data = result.data
    return typeof data === 'string' ? data : JSON.stringify(data, null, 2)
  }

  async function deleteAllData(): Promise<void> {
    const result = await sdk.deleteAllData({ client: apiClient })
    if (result.error) {
      error.value = 'Deletion request failed'
    } else {
      lastDeletionJob.value = result.data ?? null
      await fetchConnections()
    }
  }

  return { connections, error, lastSync, lastDeletionJob, fetchConnections, syncNow, connectProvider, startAuthorization, exportData, deleteAllData }
})
