import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'home',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'login',
    component: () => import(/* webpackChunkName: "about" */ '../views/LoginView.vue')
  },
  {
    path: '/gamelist',
    name: 'admin',
    component: () => import(/* webpackChunkName: "about" */ '../views/MainView.vue')
  },
  {
    path: '/register',
    name: 'register',
    component: () => import(/* webpackChunkName: "about" */ '../views/register.vue')
  },
  {
    path: '/friend/:id',
    name: 'friendview',
    component: () => import(/* webpackChunkName: "friend" */ '../views/FriendView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
