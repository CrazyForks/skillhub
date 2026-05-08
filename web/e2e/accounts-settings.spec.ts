import { expect, test } from '@playwright/test'
import { setEnglishLocale } from './helpers/auth-fixtures'
import { registerSession } from './helpers/session'

test.describe('Accounts Settings (Real API)', () => {
  test.beforeEach(async ({ page }, testInfo) => {
    await setEnglishLocale(page)
    await registerSession(page, testInfo)
  })

  test('TC_ACCOUNT_001: settings accounts route is reachable', async ({ page }) => {
    await page.goto('/settings/accounts')

    await expect(page).toHaveURL(/\/settings\//)
    await expect(page.getByRole('heading').first()).toBeVisible()
  })

  test('TC_ACCOUNT_002: route renders settings content', async ({ page }) => {
    await page.goto('/settings/accounts')

    await page.waitForTimeout(1000)
    const hasHeading = await page.getByRole('heading').first().isVisible().catch(() => false)
    expect(hasHeading).toBeTruthy()
  })

  test('TC_ACCOUNT_003: page has interactive elements', async ({ page }) => {
    await page.goto('/settings/accounts')

    await page.waitForTimeout(1000)
    const hasButton = await page.getByRole('button').first().isVisible().catch(() => false)
    expect(hasButton).toBeTruthy()
  })
})
