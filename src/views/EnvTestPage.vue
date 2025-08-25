<template>
  <div class="env-test-page">
    <h1>环境变量测试页面</h1>
    <div class="env-info">
      <h2>当前环境信息</h2>
      <div class="env-item">
        <label>NODE_ENV:</label>
        <span>{{ nodeEnv }}</span>
      </div>
      <div class="env-item">
        <label>应用标题:</label>
        <span>{{ appTitle }}</span>
      </div>
      <div class="env-item">
        <label>应用版本:</label>
        <span>{{ appVersion }}</span>
      </div>
      <div class="env-item">
        <label>API基础URL:</label>
        <span>{{ apiBaseUrl }}</span>
      </div>
      <div class="env-item">
        <label>API超时时间:</label>
        <span>{{ apiTimeout }}ms</span>
      </div>
      <div class="env-item">
        <label>开发工具启用:</label>
        <span>{{ enableDevtools ? '是' : '否' }}</span>
      </div>
      <div class="env-item">
        <label>Mock启用:</label>
        <span>{{ enableMock ? '是' : '否' }}</span>
      </div>
      <div class="env-item">
        <label>日志级别:</label>
        <span>{{ logLevel }}</span>
      </div>
      <div class="env-item">
        <label>控制台日志:</label>
        <span>{{ enableConsoleLog ? '启用' : '禁用' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

// 获取环境变量
const nodeEnv = computed(() => import.meta.env.MODE);
const appTitle = computed(() => import.meta.env.VITE_APP_TITLE || '未设置');
const appVersion = computed(() => import.meta.env.VITE_APP_VERSION || '未设置');
const apiBaseUrl = computed(
  () => import.meta.env.VITE_API_BASE_URL || '未设置'
);
const apiTimeout = computed(() => import.meta.env.VITE_API_TIMEOUT || '未设置');
const enableDevtools = computed(
  () => import.meta.env.VITE_ENABLE_DEVTOOLS === 'true'
);
const enableMock = computed(() => import.meta.env.VITE_ENABLE_MOCK === 'true');
const logLevel = computed(() => import.meta.env.VITE_LOG_LEVEL || '未设置');
const enableConsoleLog = computed(
  () => import.meta.env.VITE_ENABLE_CONSOLE_LOG === 'true'
);
</script>

<style scoped>
.env-test-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.env-info {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin-top: 20px;
}

.env-item {
  display: flex;
  margin-bottom: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #ddd;
}

.env-item label {
  font-weight: bold;
  width: 150px;
  color: #333;
}

.env-item span {
  color: #666;
  flex: 1;
}

h1 {
  color: #2c3e50;
  text-align: center;
}

h2 {
  color: #34495e;
  margin-bottom: 15px;
}
</style>