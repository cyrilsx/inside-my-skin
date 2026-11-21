import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import fs from 'node:fs'
import path from 'node:path'

// CSP is a response-header concern; in a static SPA we can only ship it as a <meta> tag,
// which is injected for production builds only (dev mode needs HMR websockets/eval).
const CSP = "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; font-src 'self'; connect-src 'self'; object-src 'none'; base-uri 'self'; form-action 'self'; upgrade-insecure-requests"

export default defineConfig({
  plugins: [
    vue(),
    {
      name: 'inject-csp-meta',
      transformIndexHtml: {
        order: 'post',
        handler(html, ctx) {
          if (ctx.bundle) {
            return html.replace('</head>', `    <meta http-equiv="Content-Security-Policy" content="${CSP}" />\n  </head>`)
          }
          return html
        }
      }
    },
    {
      name: 'copy-dot-well-known',
      closeBundle() {
        const src = path.resolve(__dirname, 'public/.well-known')
        const dest = path.resolve(__dirname, 'dist/.well-known')
        if (fs.existsSync(src)) {
          const opts = { recursive: true, dot: true } as unknown as fs.CopySyncOptions
          fs.cpSync(src, dest, opts)
        }
      }
    }
  ],
  test: {
    environment: 'jsdom',
    globals: true
  }
})
