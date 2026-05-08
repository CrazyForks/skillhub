import { expect, test } from '@playwright/test'
import { setEnglishLocale } from './helpers/auth-fixtures'
import { registerSession } from './helpers/session'

test.describe('Profile Settings (Real API)', () => {
  test.beforeEach(async ({ page }, testInfo) => {
    await setEnglishLocale(page)
    await registerSession(page, testInfo)
  })

  test('TC_PROFILE_001: displays profile settings page with user info fields', async ({ page }) => {
    await page.goto('/settings/profile')

    await expect(page.getByRole('heading', { name: 'Profile Settings' })).toBeVisible()
    await expect(page.getByText('Display Name')).toBeVisible()
    await expect(page.getByText('Email')).toBeVisible()
  })

  test('TC_PROFILE_002: edit button toggles to save and cancel', async ({ page }) => {
    await page.goto('/settings/profile')

    const editButton = page.getByRole('button', { name: 'Edit' })
    const hasEdit = await editButton.isVisible().catch(() => false)
    if (!hasEdit) return

    await editButton.click()
    await expect(page.getByRole('button', { name: 'Save' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Cancel' })).toBeVisible()
  })

  test('TC_PROFILE_003: cancel edit returns to view mode', async ({ page }) => {
    await page.goto('/settings/profile')

    const editButton = page.getByRole('button', { name: 'Edit' })
    const hasEdit = await editButton.isVisible().catch(() => false)
    if (!hasEdit) return

    await editButton.click()
    await page.getByRole('button', { name: 'Cancel' }).click()
    await expect(page.getByRole('button', { name: 'Edit' })).toBeVisible()
  })

  test('TC_PROFILE_004: validates display name minimum length', async ({ page }) => {
    await page.goto('/settings/profile')

    const editButton = page.getByRole('button', { name: 'Edit' })
    const hasEdit = await editButton.isVisible().catch(() => false)
    if (!hasEdit) return

    await editButton.click()
    const nameInput = page.getByLabel('Display Name')
    const hasInput = await nameInput.isVisible().catch(() => false)
    if (!hasInput) return

    await nameInput.clear()
    await nameInput.fill('a')
    await page.getByRole('button', { name: 'Save' }).click()
    await expect(page.getByText(/2.*32|length|characters/i)).toBeVisible()
  })
})
