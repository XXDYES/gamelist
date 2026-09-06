import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
    redirect: '/login'
  },
  {
    path: '/about',
    name: 'about',
    component: () => import(/* webpackChunkName: "about" */ '../views/AboutView.vue'),
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
    path: '/test',
    name: 'test',
    component: () => import(/* webpackChunkName: "about" */ '../views/test.vue')
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
