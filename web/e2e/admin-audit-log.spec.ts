import { expect, test } from '@playwright/test'
import { setEnglishLocale } from './helpers/auth-fixtures'

test.describe('Admin Audit Log (Real API)', () => {
  test.beforeEach(async ({ page }) => {
    await setEnglishLocale(page)
    await page.context().setExtraHTTPHeaders({
      'X-Mock-User-Id': 'local-admin',
    })
  })

  test('TC_AUDIT_001: displays audit log page with title', async ({ page }) => {
    await page.goto('/admin/audit-log')

    await expect(page.getByRole('heading', { name: 'Audit Log' })).toBeVisible()
  })

  test('TC_AUDIT_002: shows filter controls with action dropdown and user id input', async ({ page }) => {
    await page.goto('/admin/audit-log')

    await expect(page.getByRole('combobox').first()).toBeVisible()
    await expect(page.getByRole('textbox', { name: /User ID/i }).or(page.getByPlaceholder(/User ID/i)).first()).toBeVisible()
  })

  test('TC_AUDIT_003: displays filter area and either data rows or empty state', async ({ page }) => {
    await page.goto('/admin/audit-log')

    await expect(page.getByRole('button', { name: /Clear filters/i })).toBeVisible()
    await expect(
      page.getByRole('table').or(page.getByText(/No audit logs/i)).first()
    ).toBeVisible({ timeout: 10_000 })
  })

  test('TC_AUDIT_004: action filter dropdown shows options', async ({ page }) => {
    await page.goto('/admin/audit-log')

    const actionFilter = page.getByRole('combobox').first()
    await actionFilter.click()

    await expect(page.getByRole('option').first()).toBeVisible()
  })
})
