import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import { router } from './router'
import { i18n } from './i18n'
import { useAppStore } from './stores/app'
import { logger } from './utils/logger'
import './style.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(i18n)
app.config.errorHandler = (err) => {
  logger.error('unhandled render error', err instanceof Error ? err.message : String(err))
  useAppStore().setError(i18n.global.t('errors.renderError'))
}
app.mount('#app')
