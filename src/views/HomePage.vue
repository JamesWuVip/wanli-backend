<template>
  <div class="home-page">
    <div class="container">
      <header class="hero-section">
        <h1 class="hero-title">欢迎使用万里前端项目</h1>
        <p class="hero-subtitle">
          基于 Vue 3 + TypeScript + Vite 构建的现代化前端应用
        </p>
      </header>

      <main class="main-content">
        <section class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">🚀</div>
            <h3 class="feature-title">现代化技术栈</h3>
            <p class="feature-description">Vue 3 + TypeScript + Vite + Pinia</p>
          </div>

          <div class="feature-card">
            <div class="feature-icon">🛠️</div>
            <h3 class="feature-title">开发工具</h3>
            <p class="feature-description">ESLint + Prettier + Husky</p>
          </div>

          <div class="feature-card">
            <div class="feature-icon">📱</div>
            <h3 class="feature-title">响应式设计</h3>
            <p class="feature-description">移动端友好的用户界面</p>
          </div>
        </section>

        <section class="env-info">
          <h2 class="section-title">环境信息</h2>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">环境模式:</span>
              <span class="info-value">{{ envInfo.mode }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">应用版本:</span>
              <span class="info-value">{{ envInfo.version }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">API 地址:</span>
              <span class="info-value">{{ envInfo.apiUrl }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">开发工具:</span>
              <span class="info-value">{{ envInfo.devtools }}</span>
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

import { getAppConfig, getApiConfig, isDev } from '@/utils/env';

const envInfo = computed(() => {
  const appConfig = getAppConfig();
  const apiConfig = getApiConfig();

  return {
    mode: isDev() ? '开发环境' : '生产环境',
    version: appConfig.version,
    apiUrl: apiConfig.baseURL,
    devtools: isDev() ? '启用' : '禁用',
  };
});
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.hero-section {
  text-align: center;
  margin-bottom: 4rem;
  color: white;
}

.hero-title {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 1rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.hero-subtitle {
  font-size: 1.25rem;
  opacity: 0.9;
  max-width: 600px;
  margin: 0 auto;
}

.main-content {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}

.feature-card {
  text-align: center;
  padding: 2rem;
  border-radius: 0.5rem;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  transition:
    transform 0.2s,
    box-shadow 0.2s;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.feature-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #1a202c;
}

.feature-description {
  color: #4a5568;
  line-height: 1.6;
}

.env-info {
  background: #f7fafc;
  border-radius: 0.5rem;
  padding: 2rem;
  border: 1px solid #e2e8f0;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
  color: #2d3748;
  text-align: center;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background: white;
  border-radius: 0.375rem;
  border: 1px solid #e2e8f0;
}

.info-label {
  font-weight: 500;
  color: #4a5568;
}

.info-value {
  font-weight: 600;
  color: #2d3748;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.875rem;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2rem;
  }

  .hero-subtitle {
    font-size: 1rem;
  }

  .main-content {
    padding: 1rem;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}
</style>