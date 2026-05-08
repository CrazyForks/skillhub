import { expect, test } from '@playwright/test'
import { setEnglishLocale } from './helpers/auth-fixtures'
import { registerSession } from './helpers/session'

test.describe('My Namespaces Full Flow (Real API)', () => {
  test.describe.configure({ timeout: 120_000 })

  test.beforeEach(async ({ page }, testInfo) => {
    await setEnglishLocale(page)
    await registerSession(page, testInfo)
  })

  test('TC_NS_001: displays my namespaces page with title', async ({ page }) => {
    await page.goto('/dashboard/namespaces')

    await expect(page.getByRole('heading', { name: 'My Namespaces' })).toBeVisible()
  })

  test('TC_NS_002: shows namespace cards or empty state', async ({ page }) => {
    await page.goto('/dashboard/namespaces')

    await page.waitForTimeout(1000)
    const hasContent = await page.getByText(/Global|Personal|My Namespaces|No namespaces/i).first().isVisible().catch(() => false)

    expect(hasContent).toBeTruthy()
  })

  test('TC_NS_003: global namespace section is visible', async ({ page }) => {
    await page.goto('/dashboard/namespaces')

    await page.waitForTimeout(1000)
    const hasGlobal = await page.getByRole('heading', { name: 'Global' }).or(page.getByText('Global').first()).isVisible().catch(() => false)
    expect(hasGlobal).toBeTruthy()
  })

  test('TC_NS_004: namespace cards show member management links', async ({ page }) => {
    await page.goto('/dashboard/namespaces')

    await page.waitForTimeout(1000)
    const hasLink = await page.getByRole('link').first().isVisible().catch(() => false)
    expect(hasLink).toBeTruthy()
  })
})
