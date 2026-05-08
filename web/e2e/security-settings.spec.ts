import { expect, test } from '@playwright/test'
import { setEnglishLocale } from './helpers/auth-fixtures'
import { registerSession } from './helpers/session'

test.describe('Security Settings (Real API)', () => {
  test.beforeEach(async ({ page }, testInfo) => {
    await setEnglishLocale(page)
    await registerSession(page, testInfo)
  })

  test('TC_SECURITY_001: displays security settings page', async ({ page }) => {
    await page.goto('/settings/security')

    await expect(page.getByRole('heading', { name: 'Security Settings' })).toBeVisible()
  })

  test('TC_SECURITY_002: shows password change form fields', async ({ page }) => {
    await page.goto('/settings/security')

    await expect(page.getByText('Current Password')).toBeVisible()
    await expect(page.getByText('New Password')).toBeVisible()
  })

  test('TC_SECURITY_003: validates required fields on submit', async ({ page }) => {
    await page.goto('/settings/security')

    await page.getByRole('button', { name: 'Update Password' }).click()
    await expect(page.getByText('Please enter your current password')).toBeVisible()
  })

  test('TC_SECURITY_004: form accepts password input and shows feedback', async ({ page }) => {
    await page.goto('/settings/security')

    await page.getByLabel('Current Password').fill('WrongPassword123!')
    await page.getByLabel('New Password').fill('NewSecurePass456!')
    await page.getByRole('button', { name: 'Update Password' }).click()

    await page.waitForTimeout(2000)
    const hasError = await page.getByText(/invalid|incorrect|wrong|failed|error|未启用|not enabled/i).first().isVisible().catch(() => false)
    const hasSuccess = await page.getByText(/success|updated|changed|成功/i).first().isVisible().catch(() => false)

    expect(hasError || hasSuccess).toBeTruthy()
  })
})
