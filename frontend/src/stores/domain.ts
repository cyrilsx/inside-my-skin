import { defineStore } from 'pinia'
import { ref } from 'vue'
import { apiClient, sdk } from '../api/client'
import type { Activity, MentalSession, MoodCheckin, UsageDaily } from '../api/generated/types.gen'

export const useActivitiesStore = defineStore('activities', () => {
  const items = ref<Activity[]>([])
  const error = ref<string | null>(null)

  async function fetchActivities(from: string, to: string): Promise<void> {
    const result = await sdk.listActivities({ client: apiClient, query: { from, to } })
    if (result.error) {
      error.value = 'Request failed'
      items.value = []
    } else {
      error.value = null
      items.value = result.data?.items ?? []
    }
  }

  return { items, error, fetchActivities }
})

export const useMentalStore = defineStore('mental', () => {
  const sessions = ref<MentalSession[]>([])
  const usage = ref<UsageDaily[]>([])
  const error = ref<string | null>(null)

  async function fetchMental(from: string, to: string): Promise<void> {
    const [sessionsResult, usageResult] = await Promise.all([
      sdk.listMentalSessions({ client: apiClient, query: { from, to } }),
      sdk.listUsageDaily({ client: apiClient, query: { from, to } })
    ])
    if (sessionsResult.error || usageResult.error) {
      error.value = 'Request failed'
    } else {
      error.value = null
      sessions.value = sessionsResult.data?.items ?? []
      usage.value = usageResult.data?.items ?? []
    }
  }

  return { sessions, usage, error, fetchMental }
})

export const useMoodStore = defineStore('mood', () => {
  const checkins = ref<MoodCheckin[]>([])
  const error = ref<string | null>(null)

  async function fetchMood(from: string, to: string): Promise<void> {
    const result = await sdk.listMoodCheckins({ client: apiClient, query: { from, to } })
    if (result.error) {
      error.value = 'Request failed'
      checkins.value = []
    } else {
      error.value = null
      checkins.value = result.data?.items ?? []
    }
  }

  return { checkins, error, fetchMood }
})
