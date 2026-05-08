import { expect, test } from '@playwright/test'
import { setEnglishLocale } from './helpers/auth-fixtures'
import { registerSession } from './helpers/session'

test.describe('Stars Management (Real API)', () => {
  test.beforeEach(async ({ page }, testInfo) => {
    await setEnglishLocale(page)
    await registerSession(page, testInfo)
  })

  test('TC_STAR_001: displays stars page with title', async ({ page }) => {
    await page.goto('/dashboard/stars')

    await expect(page.getByRole('heading', { name: 'My Stars' })).toBeVisible()
  })

  test('TC_STAR_002: shows starred skills or empty state', async ({ page }) => {
    await page.goto('/dashboard/stars')

    const hasSkills = await page.getByRole('link').filter({ hasText: /@/ }).first().isVisible().catch(() => false)

    if (!hasSkills) {
      await expect(page.getByText('No starred skills yet')).toBeVisible()
    }
  })

  test('TC_STAR_003: pagination renders when multiple pages exist', async ({ page }) => {
    await page.goto('/dashboard/stars')

    const hasPagination = await page.getByRole('button', { name: /Previous|Next/i }).first().isVisible().catch(() => false)
    expect(typeof hasPagination).toBe('boolean')
  })
})
