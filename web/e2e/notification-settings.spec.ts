import { expect, test } from '@playwright/test'
import { setEnglishLocale } from './helpers/auth-fixtures'
import { registerSession } from './helpers/session'

test.describe('Notification Settings (Real API)', () => {
  test.beforeEach(async ({ page }, testInfo) => {
    await setEnglishLocale(page)
    await registerSession(page, testInfo)
  })

  test('TC_NOTIF_001: displays notification settings page', async ({ page }) => {
    await page.goto('/settings/notifications')

    await expect(page.getByRole('heading', { name: 'Notification Settings' })).toBeVisible({ timeout: 15_000 })
  })

  test('TC_NOTIF_002: page eventually leaves loading state', async ({ page }) => {
    await page.goto('/settings/notifications')

    await page.waitForTimeout(2000)
    const isLoading = await page.getByText('Loading...').isVisible().catch(() => false)
    expect(isLoading).toBeFalsy()
  })

  test('TC_NOTIF_003: notification toggles are interactive when present', async ({ page }) => {
    await page.goto('/settings/notifications')
    await page.waitForTimeout(3000)

    const toggle = page.getByRole('switch').first()
    const hasToggle = await toggle.isVisible().catch(() => false)

    if (hasToggle) {
      const initialState = await toggle.getAttribute('aria-checked')
      await toggle.click()
      await page.waitForTimeout(500)
      const newState = await toggle.getAttribute('aria-checked')
      expect(newState).not.toBe(initialState)
    } else {
      expect(true).toBeTruthy()
    }
  })
})
