import { expect, test } from '@playwright/test'
import { setEnglishLocale } from './helpers/auth-fixtures'
import { registerSession } from './helpers/session'

test.describe('Settings Pages (Real API)', () => {
  test.beforeEach(async ({ page }, testInfo) => {
    await setEnglishLocale(page)
    await registerSession(page, testInfo)
  })

  test('navigates to reset-password page from profile settings', async ({ page }) => {
    await page.goto('/settings/profile')
    await page.getByRole('button', { name: 'Reset Password' }).click()
    await expect(page).toHaveURL('/reset-password')
    await expect(page.getByRole('heading', { name: 'Reset Password' })).toBeVisible()
  })
})
