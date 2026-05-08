import { expect, test } from '@playwright/test'
import { setEnglishLocale } from './helpers/auth-fixtures'

test.describe('Admin Labels Management (Real API)', () => {
  test.beforeEach(async ({ page }) => {
    await setEnglishLocale(page)
    await page.context().setExtraHTTPHeaders({
      'X-Mock-User-Id': 'local-admin',
    })
  })

  test('TC_LABEL_001: displays label management page with title', async ({ page }) => {
    await page.goto('/admin/labels')

    await expect(page.getByRole('heading', { name: 'Label Management' })).toBeVisible()
  })

  test('TC_LABEL_002: shows create label button', async ({ page }) => {
    await page.goto('/admin/labels')

    await expect(page.getByRole('button', { name: /Create Label/i })).toBeVisible()
  })

  test('TC_LABEL_003: shows labels table or empty state', async ({ page }) => {
    await page.goto('/admin/labels')

    const hasTable = await page.getByRole('table').isVisible().catch(() => false)

    if (hasTable) {
      await expect(page.getByRole('columnheader', { name: /Label/i })).toBeVisible()
    } else {
      await expect(page.getByText(/No labels/i)).toBeVisible()
    }
  })

  test('TC_LABEL_004: create label button opens dialog with slug field', async ({ page }) => {
    await page.goto('/admin/labels')

    await page.getByRole('button', { name: /Create Label/i }).click()

    await expect(page.getByLabel(/Slug/i)).toBeVisible()
  })
})
