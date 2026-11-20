import { createRouter, createWebHistory } from 'vue-router'
import TodayView from '../views/TodayView.vue'
import TrendsView from '../views/TrendsView.vue'
import ActivitiesView from '../views/ActivitiesView.vue'
import MentalView from '../views/MentalView.vue'
import SourcesView from '../views/SourcesView.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', redirect: '/today' },
    { path: '/today', name: 'today', component: TodayView },
    { path: '/trends', name: 'trends', component: TrendsView },
    { path: '/activities', name: 'activities', component: ActivitiesView },
    { path: '/mental', name: 'mental', component: MentalView },
    { path: '/sources', name: 'sources', component: SourcesView }
  ]
})
