import { createRouter, createWebHistory } from 'vue-router'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', redirect: '/today' },
    { path: '/today', name: 'today', component: () => import('../views/TodayView.vue') },
    { path: '/trends', name: 'trends', component: () => import('../views/TrendsView.vue') },
    { path: '/activities', name: 'activities', component: () => import('../views/ActivitiesView.vue') },
    { path: '/mental', name: 'mental', component: () => import('../views/MentalView.vue') },
    { path: '/sources', name: 'sources', component: () => import('../views/SourcesView.vue') },
    { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('../views/NotFoundView.vue') }
  ]
})
