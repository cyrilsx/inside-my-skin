import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

const { exportDataMock, deleteAllDataMock, listConnectionsMock, listProvidersMock, getCredMock, putCredMock, delCredMock } = vi.hoisted(() => ({
  exportDataMock: vi.fn(async () => ({ data: { measurements: [] }, error: undefined })),
  deleteAllDataMock: vi.fn(async () => ({
    data: { id: 'job-1', status: 'PENDING', createdAt: '2026-11-20T12:00Z' },
    error: undefined
  })),
  listConnectionsMock: vi.fn(async () => ({ data: { items: [] }, error: undefined })),
  listProvidersMock: vi.fn(async () => ({ data: { items: [{ provider: 'withings', oauth: true, credentialsConfigured: false }] }, error: undefined })),
  getCredMock: vi.fn(async () => ({ data: { configured: true, keys: ['clientId', 'clientSecret'] }, error: undefined })),
  putCredMock: vi.fn(async () => ({ data: undefined, error: undefined })),
  delCredMock: vi.fn(async () => ({ data: undefined, error: undefined }))
}))

vi.mock('../api/client', () => ({
  apiClient: {},
  sdk: {
    listConnections: listConnectionsMock,
    exportData: exportDataMock,
    deleteAllData: deleteAllDataMock,
    listProviders: listProvidersMock,
    getProviderCredentials: getCredMock,
    putProviderCredentials: putCredMock,
    deleteProviderCredentials: delCredMock
  }
}))

import { useSourcesStore } from './sources'

describe('sources store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    exportDataMock.mockClear()
    deleteAllDataMock.mockClear()
    listConnectionsMock.mockClear()
  })

  it('exportData passes the format to the generated client and returns serialized content', async () => {
    const store = useSourcesStore()
    const content = await store.exportData('csv')
    expect(exportDataMock).toHaveBeenCalledWith(expect.objectContaining({ query: { format: 'csv' } }))
    expect(content).toBe(JSON.stringify({ measurements: [] }, null, 2))
  })

  it('deleteAllData stores the accepted job and refreshes connections', async () => {
    const store = useSourcesStore()
    await store.deleteAllData()
    expect(deleteAllDataMock).toHaveBeenCalledOnce()
    expect(store.lastDeletionJob?.status).toBe('PENDING')
    expect(listConnectionsMock).toHaveBeenCalled()
  })

  it('fetchProviders loads the provider list', async () => {
    const store = useSourcesStore()
    await store.fetchProviders()
    expect(store.providers).toEqual([{ provider: 'withings', oauth: true, credentialsConfigured: false }])
  })

  it('saveCredentials PUTs the map and refreshes status', async () => {
    const store = useSourcesStore()
    const ok = await store.saveCredentials('withings', { clientId: 'id', clientSecret: 'secret' })
    expect(ok).toBe(true)
    expect(putCredMock).toHaveBeenCalledWith(expect.objectContaining({
      path: { provider: 'withings' },
      body: { credentials: { clientId: 'id', clientSecret: 'secret' } }
    }))
    expect(getCredMock).toHaveBeenCalledWith(expect.objectContaining({ path: { provider: 'withings' } }))
    expect(store.credentials.withings).toEqual({ configured: true, keys: ['clientId', 'clientSecret'] })
  })

  it('deleteCredentials DELETEs and refreshes status', async () => {
    const store = useSourcesStore()
    const ok = await store.deleteCredentials('withings')
    expect(ok).toBe(true)
    expect(delCredMock).toHaveBeenCalledWith(expect.objectContaining({ path: { provider: 'withings' } }))
  })
})
