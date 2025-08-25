/**
 * 环境配置工具函数
 */

/**
 * 获取环境变量
 * @param key 环境变量键名
 * @param defaultValue 默认值
 * @returns 环境变量值
 */
export function getEnv(
  key: keyof ImportMetaEnv,
  defaultValue?: string
): string {
  return import.meta.env[key] || defaultValue || '';
}

/**
 * 获取布尔类型环境变量
 * @param key 环境变量键名
 * @param defaultValue 默认值
 * @returns 布尔值
 */
export function getBooleanEnv(
  key: keyof ImportMetaEnv,
  defaultValue = false
): boolean {
  const value = getEnv(key);
  return value === 'true' || value === '1' || defaultValue;
}

/**
 * 获取数字类型环境变量
 * @param key 环境变量键名
 * @param defaultValue 默认值
 * @returns 数字值
 */
export function getNumberEnv(
  key: keyof ImportMetaEnv,
  defaultValue = 0
): number {
  const value = getEnv(key);
  const num = parseInt(value, 10);
  return isNaN(num) ? defaultValue : num;
}

/**
 * 判断是否为开发环境
 */
export function isDev(): boolean {
  return import.meta.env.NODE_ENV === 'development';
}

/**
 * 判断是否为生产环境
 */
export function isProd(): boolean {
  return import.meta.env.NODE_ENV === 'production';
}

/**
 * 判断是否为测试环境
 */
export function isTest(): boolean {
  return import.meta.env.NODE_ENV === 'test';
}

/**
 * 获取应用配置
 */
export function getAppConfig() {
  return {
    title: getEnv('VITE_APP_TITLE'),
    version: getEnv('VITE_APP_VERSION'),
    description: getEnv('VITE_APP_DESCRIPTION'),
    isDev: isDev(),
    isProd: isProd(),
    isTest: isTest(),
  };
}

/**
 * 获取API配置
 */
export function getApiConfig() {
  return {
    baseURL: getEnv('VITE_API_BASE_URL'),
    timeout: getNumberEnv('VITE_API_TIMEOUT', 10000),
    enableMock: getBooleanEnv('VITE_ENABLE_MOCK'),
  };
}

/**
 * 获取日志配置
 */
export function getLogConfig() {
  return {
    level: getEnv('VITE_LOG_LEVEL', 'info'),
    enableConsole: getBooleanEnv('VITE_ENABLE_CONSOLE_LOG', true),
  };
}