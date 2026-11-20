import { client } from './generated/client.gen'
import * as sdk from './generated/sdk.gen'

client.setConfig({ baseUrl: import.meta.env.VITE_API_BASE_URL ?? '/api/v1' })

export const apiClient = client
export { sdk }
