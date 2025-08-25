import { test, expect } from '@playwright/test'

test.describe('万里书院前端 - E2E测试', () => {
  test('应该能够访问首页', async ({ page }) => {
    await page.goto('/')
    
    // 等待页面加载完成
    await page.waitForLoadState('networkidle')
    
    // 验证页面标题
    await expect(page).toHaveTitle(/万里学院/)
  })

  test('应该能够进行基本的页面交互', async ({ page }) => {
    await page.goto('/')
    
    // 等待页面加载
    await page.waitForLoadState('networkidle')
    
    // 检查页面是否包含基本元素
    const body = page.locator('body')
    await expect(body).toBeVisible()
  })

  test('应该能够处理响应式布局', async ({ page }) => {
    // 测试桌面视图
    await page.setViewportSize({ width: 1200, height: 800 })
    await page.goto('/')
    await page.waitForLoadState('networkidle')
    
    // 测试移动端视图
    await page.setViewportSize({ width: 375, height: 667 })
    await page.reload()
    await page.waitForLoadState('networkidle')
    
    const body = page.locator('body')
    await expect(body).toBeVisible()
  })

  test('应该能够处理网络错误', async ({ page }) => {
    // 模拟网络离线
    await page.context().setOffline(true)
    
    await page.goto('/', { waitUntil: 'domcontentloaded' })
    
    // 恢复网络连接
    await page.context().setOffline(false)
  })

  test('应该能够进行性能测试', async ({ page }) => {
    const startTime = Date.now()
    
    await page.goto('/')
    await page.waitForLoadState('networkidle')
    
    const loadTime = Date.now() - startTime
    
    // 页面加载时间应该在合理范围内（5秒内）
    expect(loadTime).toBeLessThan(5000)
  })
})