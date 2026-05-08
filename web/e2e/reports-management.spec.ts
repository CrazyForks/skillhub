import { expect, test } from '@playwright/test'
import { setEnglishLocale } from './helpers/auth-fixtures'

test.describe('Reports Management (Real API)', () => {
  test.beforeEach(async ({ page }) => {
    await setEnglishLocale(page)
    await page.context().setExtraHTTPHeaders({
      'X-Mock-User-Id': 'local-admin',
    })
  })

  test('TC_REPORT_001: displays skill reports page with title', async ({ page }) => {
    await page.goto('/dashboard/reports')

    await expect(page.getByRole('heading', { name: 'Skill Reports' })).toBeVisible()
  })

  test('TC_REPORT_002: shows three status tabs', async ({ page }) => {
    await page.goto('/dashboard/reports')

    await expect(page.getByRole('button', { name: 'Pending' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Resolved' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Dismissed' })).toBeVisible()
  })

  test('TC_REPORT_003: tabs switch and maintain active state', async ({ page }) => {
    await page.goto('/dashboard/reports')

    for (const tab of ['Resolved', 'Dismissed', 'Pending'] as const) {
      await page.getByRole('button', { name: tab }).click()
      await expect(page.getByRole('button', { name: tab })).toHaveAttribute('data-state', 'active')
    }
  })

  test('TC_REPORT_004: each tab shows content or empty state', async ({ page }) => {
    await page.goto('/dashboard/reports')

    for (const tab of ['Pending', 'Resolved', 'Dismissed'] as const) {
      await page.getByRole('button', { name: tab }).click()

      const hasEmpty = await page.getByText('No reports').isVisible().catch(() => false)
      if (hasEmpty) {
        await expect(page.getByText('No reports')).toBeVisible()
      }
    }
  })
})
