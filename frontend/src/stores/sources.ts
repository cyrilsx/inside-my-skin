import { defineStore } from 'pinia'
import { ref } from 'vue'
import { apiClient, sdk } from '../api/client'
import type { Connection, SyncResult } from '../api/generated/types.gen'

export const useSourcesStore = defineStore('sources', () => {
  const connections = ref<Connection[]>([])
  const error = ref<string | null>(null)
  const lastSync = ref<SyncResult | null>(null)

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

  return { connections, error, lastSync, fetchConnections, syncNow, connectProvider, startAuthorization }
})
