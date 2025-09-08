# 万里教育后端系统

这是万里教育加盟连锁门店作业管理系统的后端代码。

## 技术栈
- React + TypeScript
- Ant Design
- Zustand (状态管理)
- Supabase (数据库)
- Vite (构建工具)
- Vitest (测试框架)

## 项目结构
```
src/
├── components/          # 公共组件
├── pages/              # 页面组件
├── hooks/              # 自定义Hooks
├── services/           # API服务
├── stores/             # 状态管理
├── types/              # TypeScript类型定义
├── utils/              # 工具函数
└── constants/          # 常量定义
```

## 开发规范
- 遵循GitFlow工作流
- dev分支用于开发
- staging分支用于测试
- main分支用于生产

## 安装和运行
```bash
npm install
npm run dev
```

## 测试
```bash
npm run test
npm run test:e2e
```