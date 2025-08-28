# 部署和DevOps配置

## Issue描述
建立完整的前端部署流程和DevOps配置，实现自动化构建、测试、部署和监控，确保应用的稳定发布和运维。

## 任务清单

### CI/CD流水线
- [ ] GitHub Actions配置
- [ ] 自动化测试流程
- [ ] 代码质量检查
- [ ] 安全扫描集成
- [ ] 构建优化配置
- [ ] 部署自动化
- [ ] 回滚机制
- [ ] 通知机制

### 环境配置
- [ ] 开发环境配置
- [ ] 测试环境配置
- [ ] 预生产环境配置
- [ ] 生产环境配置
- [ ] 环境变量管理
- [ ] 配置文件管理
- [ ] 数据库连接配置
- [ ] API端点配置

### 容器化部署
- [ ] Docker镜像构建
- [ ] 多阶段构建优化
- [ ] 镜像安全扫描
- [ ] 容器编排配置
- [ ] 健康检查配置
- [ ] 资源限制配置
- [ ] 日志收集配置
- [ ] 监控指标配置

### 静态资源部署
- [ ] CDN配置
- [ ] 资源压缩优化
- [ ] 缓存策略配置
- [ ] HTTPS配置
- [ ] 域名配置
- [ ] 负载均衡配置
- [ ] 故障转移配置
- [ ] 性能监控

### 监控和日志
- [ ] 应用性能监控
- [ ] 错误监控配置
- [ ] 用户行为分析
- [ ] 日志聚合配置
- [ ] 告警规则配置
- [ ] 仪表盘配置
- [ ] 报告生成
- [ ] 故障排查工具

### 安全配置
- [ ] HTTPS强制配置
- [ ] 安全头配置
- [ ] CSP策略配置
- [ ] 防火墙规则
- [ ] 访问控制配置
- [ ] 漏洞扫描
- [ ] 安全审计
- [ ] 备份策略

## CI/CD配置

### GitHub Actions工作流
```yaml
# .github/workflows/ci.yml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, dev, staging ]
  pull_request:
    branches: [ main, dev ]

env:
  NODE_VERSION: '18'
  PNPM_VERSION: '8'

jobs:
  # 代码质量检查
  lint-and-test:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Setup Node.js
      uses: actions/setup-node@v4
      with:
        node-version: ${{ env.NODE_VERSION }}
        
    - name: Setup pnpm
      uses: pnpm/action-setup@v2
      with:
        version: ${{ env.PNPM_VERSION }}
        
    - name: Get pnpm store directory
      id: pnpm-cache
      shell: bash
      run: |
        echo "STORE_PATH=$(pnpm store path)" >> $GITHUB_OUTPUT
        
    - name: Setup pnpm cache
      uses: actions/cache@v3
      with:
        path: ${{ steps.pnpm-cache.outputs.STORE_PATH }}
        key: ${{ runner.os }}-pnpm-store-${{ hashFiles('**/pnpm-lock.yaml') }}
        restore-keys: |
          ${{ runner.os }}-pnpm-store-
          
    - name: Install dependencies
      run: pnpm install --frozen-lockfile
      
    - name: Run ESLint
      run: pnpm run lint
      
    - name: Run Prettier check
      run: pnpm run format:check
      
    - name: Run type check
      run: pnpm run type-check
      
    - name: Run unit tests
      run: pnpm run test:unit
      
    - name: Run coverage
      run: pnpm run test:coverage
      
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: ./coverage/lcov.info
        flags: unittests
        name: codecov-umbrella
        
  # 安全扫描
  security-scan:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Run Trivy vulnerability scanner
      uses: aquasecurity/trivy-action@master
      with:
        scan-type: 'fs'
        scan-ref: '.'
        format: 'sarif'
        output: 'trivy-results.sarif'
        
    - name: Upload Trivy scan results to GitHub Security tab
      uses: github/codeql-action/upload-sarif@v2
      with:
        sarif_file: 'trivy-results.sarif'
        
    - name: Run npm audit
      run: |
        npm audit --audit-level moderate
        
  # E2E测试
  e2e-test:
    runs-on: ubuntu-latest
    needs: [lint-and-test]
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Setup Node.js
      uses: actions/setup-node@v4
      with:
        node-version: ${{ env.NODE_VERSION }}
        
    - name: Setup pnpm
      uses: pnpm/action-setup@v2
      with:
        version: ${{ env.PNPM_VERSION }}
        
    - name: Install dependencies
      run: pnpm install --frozen-lockfile
      
    - name: Install Playwright browsers
      run: pnpm exec playwright install --with-deps
      
    - name: Build application
      run: pnpm run build
      
    - name: Run E2E tests
      run: pnpm run test:e2e
      
    - name: Upload E2E test results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: playwright-report
        path: playwright-report/
        retention-days: 30
        
  # 构建和部署
  build-and-deploy:
    runs-on: ubuntu-latest
    needs: [lint-and-test, security-scan, e2e-test]
    if: github.ref == 'refs/heads/main' || github.ref == 'refs/heads/staging' || github.ref == 'refs/heads/dev'
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Setup Node.js
      uses: actions/setup-node@v4
      with:
        node-version: ${{ env.NODE_VERSION }}
        
    - name: Setup pnpm
      uses: pnpm/action-setup@v2
      with:
        version: ${{ env.PNPM_VERSION }}
        
    - name: Install dependencies
      run: pnpm install --frozen-lockfile
      
    - name: Set environment variables
      run: |
        if [[ $GITHUB_REF == 'refs/heads/main' ]]; then
          echo "VITE_API_BASE_URL=${{ secrets.PROD_API_URL }}" >> $GITHUB_ENV
          echo "VITE_APP_ENV=production" >> $GITHUB_ENV
        elif [[ $GITHUB_REF == 'refs/heads/staging' ]]; then
          echo "VITE_API_BASE_URL=${{ secrets.STAGING_API_URL }}" >> $GITHUB_ENV
          echo "VITE_APP_ENV=staging" >> $GITHUB_ENV
        else
          echo "VITE_API_BASE_URL=${{ secrets.DEV_API_URL }}" >> $GITHUB_ENV
          echo "VITE_APP_ENV=development" >> $GITHUB_ENV
        fi
        
    - name: Build application
      run: pnpm run build
      
    - name: Run build analysis
      run: pnpm run build:analyze
      
    - name: Upload build artifacts
      uses: actions/upload-artifact@v3
      with:
        name: dist-files
        path: dist/
        
    # Docker构建和推送
    - name: Set up Docker Buildx
      uses: docker/setup-buildx-action@v2
      
    - name: Login to Container Registry
      uses: docker/login-action@v2
      with:
        registry: ${{ secrets.CONTAINER_REGISTRY }}
        username: ${{ secrets.REGISTRY_USERNAME }}
        password: ${{ secrets.REGISTRY_PASSWORD }}
        
    - name: Extract metadata
      id: meta
      uses: docker/metadata-action@v4
      with:
        images: ${{ secrets.CONTAINER_REGISTRY }}/wanli-frontend
        tags: |
          type=ref,event=branch
          type=ref,event=pr
          type=sha,prefix={{branch}}-
          
    - name: Build and push Docker image
      uses: docker/build-push-action@v4
      with:
        context: .
        push: true
        tags: ${{ steps.meta.outputs.tags }}
        labels: ${{ steps.meta.outputs.labels }}
        cache-from: type=gha
        cache-to: type=gha,mode=max
        
    # 部署到不同环境
    - name: Deploy to environment
      run: |
        if [[ $GITHUB_REF == 'refs/heads/main' ]]; then
          echo "Deploying to production..."
          # 生产环境部署逻辑
        elif [[ $GITHUB_REF == 'refs/heads/staging' ]]; then
          echo "Deploying to staging..."
          # 测试环境部署逻辑
        else
          echo "Deploying to development..."
          # 开发环境部署逻辑
        fi
        
  # 通知
  notify:
    runs-on: ubuntu-latest
    needs: [build-and-deploy]
    if: always()
    
    steps:
    - name: Notify deployment status
      uses: 8398a7/action-slack@v3
      with:
        status: ${{ job.status }}
        channel: '#deployments'
        webhook_url: ${{ secrets.SLACK_WEBHOOK }}
      env:
        SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK }}
```

### Docker配置
```dockerfile
# Dockerfile
# 多阶段构建
FROM node:18-alpine AS builder

# 设置工作目录
WORKDIR /app

# 安装pnpm
RUN npm install -g pnpm

# 复制package文件
COPY package.json pnpm-lock.yaml ./

# 安装依赖
RUN pnpm install --frozen-lockfile

# 复制源代码
COPY . .

# 构建应用
RUN pnpm run build

# 生产阶段
FROM nginx:alpine AS production

# 安装必要的包
RUN apk add --no-cache curl

# 复制nginx配置
COPY nginx.conf /etc/nginx/nginx.conf
COPY default.conf /etc/nginx/conf.d/default.conf

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 创建非root用户
RUN addgroup -g 1001 -S nodejs
RUN adduser -S nextjs -u 1001

# 设置权限
RUN chown -R nextjs:nodejs /usr/share/nginx/html
RUN chown -R nextjs:nodejs /var/cache/nginx
RUN chown -R nextjs:nodejs /var/log/nginx
RUN chown -R nextjs:nodejs /etc/nginx/conf.d
RUN touch /var/run/nginx.pid
RUN chown -R nextjs:nodejs /var/run/nginx.pid

# 切换到非root用户
USER nextjs

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:80/health || exit 1

# 暴露端口
EXPOSE 80

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]
```

### Nginx配置
```nginx
# nginx.conf
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
    use epoll;
    multi_accept on;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;
    
    # 日志格式
    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';
    
    access_log /var/log/nginx/access.log main;
    
    # 性能优化
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;
    
    # Gzip压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/json
        application/javascript
        application/xml+rss
        application/atom+xml
        image/svg+xml;
    
    # 安全头
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Referrer-Policy "strict-origin-when-cross-origin";
    
    # 包含站点配置
    include /etc/nginx/conf.d/*.conf;
}
```

```nginx
# default.conf
server {
    listen 80;
    server_name _;
    root /usr/share/nginx/html;
    index index.html;
    
    # 安全配置
    server_tokens off;
    
    # CSP策略
    add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self' https://api.wanli.edu;";
    
    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        add_header Vary Accept-Encoding;
    }
    
    # HTML文件不缓存
    location ~* \.html$ {
        expires -1;
        add_header Cache-Control "no-cache, no-store, must-revalidate";
        add_header Pragma "no-cache";
    }
    
    # API代理
    location /api/ {
        proxy_pass http://backend-service:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时配置
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
    }
    
    # SPA路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # 健康检查
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
    
    # 错误页面
    error_page 404 /index.html;
    error_page 500 502 503 504 /50x.html;
    
    location = /50x.html {
        root /usr/share/nginx/html;
    }
}
```

### Docker Compose配置
```yaml
# docker-compose.yml
version: '3.8'

services:
  frontend:
    build:
      context: .
      dockerfile: Dockerfile
      target: production
    ports:
      - "80:80"
    environment:
      - NODE_ENV=production
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./default.conf:/etc/nginx/conf.d/default.conf:ro
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:80/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
    networks:
      - app-network
    
  # 监控服务
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml:ro
    networks:
      - app-network
      
  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana-data:/var/lib/grafana
      - ./monitoring/grafana/dashboards:/etc/grafana/provisioning/dashboards:ro
      - ./monitoring/grafana/datasources:/etc/grafana/provisioning/datasources:ro
    networks:
      - app-network

volumes:
  grafana-data:

networks:
  app-network:
    driver: bridge
```

### Kubernetes部署配置
```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: wanli-frontend
  labels:
    app: wanli-frontend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: wanli-frontend
  template:
    metadata:
      labels:
        app: wanli-frontend
    spec:
      containers:
      - name: frontend
        image: registry.example.com/wanli-frontend:latest
        ports:
        - containerPort: 80
        env:
        - name: NODE_ENV
          value: "production"
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "200m"
        livenessProbe:
          httpGet:
            path: /health
            port: 80
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /health
            port: 80
          initialDelaySeconds: 5
          periodSeconds: 5
        securityContext:
          runAsNonRoot: true
          runAsUser: 1001
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          capabilities:
            drop:
            - ALL
---
apiVersion: v1
kind: Service
metadata:
  name: wanli-frontend-service
spec:
  selector:
    app: wanli-frontend
  ports:
  - protocol: TCP
    port: 80
    targetPort: 80
  type: ClusterIP
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: wanli-frontend-ingress
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
spec:
  tls:
  - hosts:
    - wanli.edu
    secretName: wanli-frontend-tls
  rules:
  - host: wanli.edu
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: wanli-frontend-service
            port:
              number: 80
```

## 环境配置管理

### 环境变量配置
```typescript
// src/config/env.ts
interface EnvConfig {
  apiBaseUrl: string
  appEnv: 'development' | 'staging' | 'production'
  enableAnalytics: boolean
  enableSentry: boolean
  sentryDsn?: string
  googleAnalyticsId?: string
}

const getEnvConfig = (): EnvConfig => {
  const config: EnvConfig = {
    apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    appEnv: (import.meta.env.VITE_APP_ENV as EnvConfig['appEnv']) || 'development',
    enableAnalytics: import.meta.env.VITE_ENABLE_ANALYTICS === 'true',
    enableSentry: import.meta.env.VITE_ENABLE_SENTRY === 'true',
    sentryDsn: import.meta.env.VITE_SENTRY_DSN,
    googleAnalyticsId: import.meta.env.VITE_GA_ID
  }
  
  // 验证必需的环境变量
  if (!config.apiBaseUrl) {
    throw new Error('VITE_API_BASE_URL is required')
  }
  
  return config
}

export const env = getEnvConfig()

// 环境检查工具
export const isDevelopment = env.appEnv === 'development'
export const isStaging = env.appEnv === 'staging'
export const isProduction = env.appEnv === 'production'

// 调试工具
export const enableDebug = isDevelopment || isStaging
```

### 配置文件管理
```typescript
// src/config/index.ts
import { env } from './env'

interface AppConfig {
  api: {
    baseUrl: string
    timeout: number
    retries: number
  }
  auth: {
    tokenKey: string
    refreshTokenKey: string
    tokenExpiry: number
  }
  ui: {
    theme: string
    language: string
    pageSize: number
  }
  features: {
    enableDarkMode: boolean
    enableNotifications: boolean
    enableAnalytics: boolean
  }
}

const createConfig = (): AppConfig => {
  const baseConfig: AppConfig = {
    api: {
      baseUrl: env.apiBaseUrl,
      timeout: 10000,
      retries: 3
    },
    auth: {
      tokenKey: 'wanli_token',
      refreshTokenKey: 'wanli_refresh_token',
      tokenExpiry: 24 * 60 * 60 * 1000 // 24小时
    },
    ui: {
      theme: 'light',
      language: 'zh-CN',
      pageSize: 20
    },
    features: {
      enableDarkMode: true,
      enableNotifications: true,
      enableAnalytics: env.enableAnalytics
    }
  }
  
  // 根据环境调整配置
  if (env.appEnv === 'development') {
    baseConfig.api.timeout = 30000 // 开发环境延长超时
  }
  
  if (env.appEnv === 'production') {
    baseConfig.features.enableAnalytics = true
  }
  
  return baseConfig
}

export const config = createConfig()
```

## 监控和日志配置

### 应用监控集成
```typescript
// src/utils/monitoring.ts
import * as Sentry from '@sentry/vue'
import { env } from '@/config/env'

// Sentry配置
export const initSentry = (app: any) => {
  if (env.enableSentry && env.sentryDsn) {
    Sentry.init({
      app,
      dsn: env.sentryDsn,
      environment: env.appEnv,
      integrations: [
        new Sentry.BrowserTracing({
          routingInstrumentation: Sentry.vueRouterInstrumentation(router)
        })
      ],
      tracesSampleRate: env.appEnv === 'production' ? 0.1 : 1.0,
      beforeSend(event) {
        // 过滤敏感信息
        if (event.exception) {
          const error = event.exception.values?.[0]
          if (error?.value?.includes('password')) {
            return null
          }
        }
        return event
      }
    })
  }
}

// 性能监控
export class PerformanceTracker {
  private static instance: PerformanceTracker
  
  static getInstance(): PerformanceTracker {
    if (!PerformanceTracker.instance) {
      PerformanceTracker.instance = new PerformanceTracker()
    }
    return PerformanceTracker.instance
  }
  
  trackPageLoad(pageName: string) {
    const startTime = performance.now()
    
    return () => {
      const endTime = performance.now()
      const loadTime = endTime - startTime
      
      // 发送到监控服务
      this.sendMetric('page_load_time', {
        page: pageName,
        duration: loadTime,
        timestamp: Date.now()
      })
    }
  }
  
  trackApiCall(endpoint: string, method: string) {
    const startTime = performance.now()
    
    return (success: boolean, statusCode?: number) => {
      const endTime = performance.now()
      const duration = endTime - startTime
      
      this.sendMetric('api_call', {
        endpoint,
        method,
        duration,
        success,
        statusCode,
        timestamp: Date.now()
      })
    }
  }
  
  trackUserAction(action: string, data?: any) {
    this.sendMetric('user_action', {
      action,
      data,
      timestamp: Date.now(),
      userAgent: navigator.userAgent,
      url: window.location.href
    })
  }
  
  private sendMetric(type: string, data: any) {
    if (env.appEnv === 'production') {
      // 发送到实际的监控服务
      fetch('/api/metrics', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ type, data })
      }).catch(console.error)
    } else {
      console.log(`[Metric] ${type}:`, data)
    }
  }
}

export const performanceTracker = PerformanceTracker.getInstance()
```

### 日志配置
```typescript
// src/utils/logger.ts
import { env } from '@/config/env'

enum LogLevel {
  DEBUG = 0,
  INFO = 1,
  WARN = 2,
  ERROR = 3
}

class Logger {
  private level: LogLevel
  
  constructor() {
    this.level = env.appEnv === 'production' ? LogLevel.WARN : LogLevel.DEBUG
  }
  
  debug(message: string, ...args: any[]) {
    if (this.level <= LogLevel.DEBUG) {
      console.debug(`[DEBUG] ${message}`, ...args)
    }
  }
  
  info(message: string, ...args: any[]) {
    if (this.level <= LogLevel.INFO) {
      console.info(`[INFO] ${message}`, ...args)
    }
  }
  
  warn(message: string, ...args: any[]) {
    if (this.level <= LogLevel.WARN) {
      console.warn(`[WARN] ${message}`, ...args)
    }
    
    // 发送到监控服务
    this.sendToMonitoring('warn', message, args)
  }
  
  error(message: string, error?: Error, ...args: any[]) {
    console.error(`[ERROR] ${message}`, error, ...args)
    
    // 发送到监控服务
    this.sendToMonitoring('error', message, { error, args })
  }
  
  private sendToMonitoring(level: string, message: string, data: any) {
    if (env.enableSentry) {
      // 发送到Sentry
      if (level === 'error') {
        Sentry.captureException(data.error || new Error(message))
      } else {
        Sentry.captureMessage(message, level as any)
      }
    }
  }
}

export const logger = new Logger()
```

## 验收标准
- CI/CD流水线正常运行
- 自动化测试覆盖完整
- 多环境部署配置正确
- 容器化部署成功
- 监控和告警正常工作
- 安全配置符合要求
- 性能指标达标
- 日志收集完整

## 技术要求
- 使用GitHub Actions
- 配置Docker容器化
- 集成Kubernetes部署
- 实现多环境管理
- 配置监控和日志
- 遵循安全最佳实践

## 优先级
高优先级 - 部署基础设施

## 预估工时
3个工作日

## 相关文档
- SP1前端开发方案-技术架构文档.md
- DevOps最佳实践
- 安全配置指南