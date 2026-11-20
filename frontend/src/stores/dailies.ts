import { defineStore } from 'pinia'
import { ref } from 'vue'
import { apiClient, sdk } from '../api/client'
import type { Daily, GetDailiesError } from '../api/generated/types.gen'

export const useDailiesStore = defineStore('dailies', () => {
  const items = ref<Daily[]>([])
  const error = ref<string | null>(null)
  const loading = ref(false)

  async function fetchDailies(from: string, to: string): Promise<void> {
    loading.value = true
    error.value = null
    try {
      const result = await sdk.getDailies({ client: apiClient, query: { from, to } })
      if (result.error) {
        error.value = (result.error as GetDailiesError & { title?: string }).title ?? 'Request failed'
        items.value = []
      } else {
        items.value = result.data?.items ?? []
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Request failed'
      items.value = []
    } finally {
      loading.value = false
    }
  }

  return { items, error, loading, fetchDailies }
})
