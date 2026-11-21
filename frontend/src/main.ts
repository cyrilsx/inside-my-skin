import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import { router } from './router'
import { useAppStore } from './stores/app'
import './style.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.config.errorHandler = (err) => {
  console.error(err)
  useAppStore().setError('Something went wrong while rendering this page.')
}
app.mount('#app')
