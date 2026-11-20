import { client } from './generated/client.gen'
import * as sdk from './generated/sdk.gen'

client.setConfig({ baseUrl: import.meta.env.VITE_API_BASE_URL ?? '/api/v1' })

export const apiClient = client
export { sdk }

export function setAuthToken(token: string | null): void {
  if (token) {
    client.setConfig({ headers: { Authorization: `Bearer ${token}` } })
  } else {
    client.setConfig({ headers: { Authorization: undefined } })
  }
}
