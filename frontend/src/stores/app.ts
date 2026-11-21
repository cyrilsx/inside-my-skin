import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const error = ref<string | null>(null)

  function setError(message: string | null): void {
    error.value = message
  }

  return { error, setError }
})
