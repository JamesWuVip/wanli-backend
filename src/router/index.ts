import { createRouter, createWebHistory } from 'vue-router';

import HomePage from '../views/HomePage.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomePage,
      meta: {
        title: '首页',
      },
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
      meta: {
        title: '关于',
      },
    },
    {
      path: '/env-test',
      name: 'env-test',
      component: () => import('../views/EnvTestPage.vue'),
      meta: {
        title: '环境变量测试',
      },
    },
  ],
});

// 路由守卫
router.beforeEach((to, _from, next) => {
  // 设置页面标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} - 万里前端项目`;
  }
  next();
});

export default router;