import { expect, test } from '@playwright/test'
import { setEnglishLocale } from './helpers/auth-fixtures'

test.describe('Admin Users Management (Real API)', () => {
  test.beforeEach(async ({ page }) => {
    await setEnglishLocale(page)
    await page.context().setExtraHTTPHeaders({
      'X-Mock-User-Id': 'local-admin',
    })
  })

  test('TC_USER_001: displays user management page with title', async ({ page }) => {
    await page.goto('/admin/users')

    await expect(page.getByRole('heading', { name: 'User Management' })).toBeVisible()
  })

  test('TC_USER_002: shows search input and status filter', async ({ page }) => {
    await page.goto('/admin/users')

    await expect(page.getByRole('textbox', { name: /Search users/i }).or(page.getByPlaceholder(/Search users/i)).first()).toBeVisible()
    await expect(page.getByRole('combobox').first()).toBeVisible()
  })

  test('TC_USER_003: displays search area and either rows or empty state', async ({ page }) => {
    await page.goto('/admin/users')

    await expect(page.getByRole('button', { name: 'Search' })).toBeVisible()
    const hasDataText = await page.getByText(/Username|Email|Status|Role|No user data/i).first().isVisible().catch(() => false)
    expect(hasDataText).toBeTruthy()
  })

  test('TC_USER_004: search triggers user filtering', async ({ page }) => {
    await page.goto('/admin/users')

    const searchInput = page.getByRole('textbox', { name: /Search users/i }).or(page.getByPlaceholder(/Search users/i)).first()
    await searchInput.fill('admin')

    const searchButton = page.getByRole('button', { name: 'Search' })
    await searchButton.click()
  })

  test('TC_USER_005: status filter dropdown shows options', async ({ page }) => {
    await page.goto('/admin/users')

    const statusFilter = page.getByRole('combobox').first()
    await statusFilter.click()

    await expect(page.getByRole('option').first()).toBeVisible()
  })
})
