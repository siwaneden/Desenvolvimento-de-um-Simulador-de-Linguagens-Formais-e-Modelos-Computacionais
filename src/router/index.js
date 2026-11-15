import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ModuleView from '../views/ModuleView.vue'
import { modules } from '../data/modules'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/modulos/:slug',
      name: 'module',
      component: ModuleView,
      beforeEnter: (to) => {
        if (!modules.some((item) => item.slug === to.params.slug)) {
          return { name: 'home' }
        }
      },
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
  scrollBehavior: () => ({ top: 0 }),
})

export default router
