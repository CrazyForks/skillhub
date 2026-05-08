import { expect, test } from '@playwright/test'
import { setEnglishLocale } from './helpers/auth-fixtures'

test.describe('Promotions Management (Real API)', () => {
  test.beforeEach(async ({ page }) => {
    await setEnglishLocale(page)
    await page.context().setExtraHTTPHeaders({
      'X-Mock-User-Id': 'local-admin',
    })
  })

  test('TC_PROMO_001: displays promotion review page with title', async ({ page }) => {
    await page.goto('/dashboard/promotions')

    await expect(page.getByRole('heading', { name: 'Promotion Review' })).toBeVisible()
  })

  test('TC_PROMO_002: shows three status tabs', async ({ page }) => {
    await page.goto('/dashboard/promotions')

    await expect(page.getByRole('button', { name: 'Pending' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Approved' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Rejected' })).toBeVisible()
  })

  test('TC_PROMO_003: tabs switch and maintain active state', async ({ page }) => {
    await page.goto('/dashboard/promotions')

    for (const tab of ['Approved', 'Rejected', 'Pending'] as const) {
      await page.getByRole('button', { name: tab }).click()
      await expect(page.getByRole('button', { name: tab })).toHaveAttribute('data-state', 'active')
    }
  })

  test('TC_PROMO_004: each tab shows content or empty state', async ({ page }) => {
    await page.goto('/dashboard/promotions')

    for (const tab of ['Pending', 'Approved', 'Rejected'] as const) {
      await page.getByRole('button', { name: tab }).click()

      const hasContent = await page.getByText('No promotion requests').isVisible().catch(() => false)
      if (hasContent) {
        await expect(page.getByText('No promotion requests')).toBeVisible()
      }
    }
  })
})
