import { defineStore } from 'pinia'
import { ref } from 'vue'
import { apiClient, sdk } from '../api/client'
import type { MetricType, Series } from '../api/generated/types.gen'

export const useSeriesStore = defineStore('series', () => {
  const series = ref<Series | null>(null)
  const error = ref<string | null>(null)
  const loading = ref(false)

  async function fetchSeries(metricType: MetricType, from: string, to: string): Promise<void> {
    loading.value = true
    error.value = null
    try {
      const result = await sdk.getSeries({ client: apiClient, path: { metricType }, query: { from, to } })
      if (result.error) {
        error.value = 'Request failed'
        series.value = null
      } else {
        series.value = result.data ?? null
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Request failed'
      series.value = null
    } finally {
      loading.value = false
    }
  }

  return { series, error, loading, fetchSeries }
})
