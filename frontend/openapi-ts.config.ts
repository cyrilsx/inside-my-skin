import { defineConfig } from '@hey-api/openapi-ts'

export default defineConfig({
  input: '../docs/openapi.yaml',
  output: 'src/api/generated',
  plugins: ['@hey-api/typescript', '@hey-api/schemas', '@hey-api/client-fetch', '@hey-api/sdk']
})
