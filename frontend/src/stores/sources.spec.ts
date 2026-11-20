import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

const { exportDataMock, deleteAllDataMock, listConnectionsMock } = vi.hoisted(() => ({
  exportDataMock: vi.fn(async () => ({ data: { measurements: [] }, error: undefined })),
  deleteAllDataMock: vi.fn(async () => ({
    data: { id: 'job-1', status: 'PENDING', createdAt: '2026-11-20T12:00Z' },
    error: undefined
  })),
  listConnectionsMock: vi.fn(async () => ({ data: { items: [] }, error: undefined }))
}))

vi.mock('../api/client', () => ({
  apiClient: {},
  sdk: {
    listConnections: listConnectionsMock,
    exportData: exportDataMock,
    deleteAllData: deleteAllDataMock
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
})
