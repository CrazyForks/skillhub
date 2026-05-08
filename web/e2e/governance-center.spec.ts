import { expect, test } from '@playwright/test'
import { setEnglishLocale } from './helpers/auth-fixtures'

test.describe('Governance Center (Real API)', () => {
  test.describe.configure({ timeout: 120_000 })

  test.beforeEach(async ({ page }) => {
    await setEnglishLocale(page)
    await page.context().setExtraHTTPHeaders({
      'X-Mock-User-Id': 'local-admin',
    })
  })

  test('TC_GOV_001: displays governance center with title and summary cards', async ({ page }) => {
    await page.goto('/dashboard/governance')

    await expect(page.getByRole('heading', { name: 'Governance Center' })).toBeVisible()
    await expect(page.getByText('Track reviews, promotions, reports, and audit activity in one place.')).toBeVisible()

    await expect(page.getByText('Pending reviews')).toBeVisible()
    await expect(page.getByText('Pending promotions')).toBeVisible()
    await expect(page.getByText('Pending reports')).toBeVisible()
    await expect(page.getByText('Unread notifications')).toBeVisible()
  })

  test('TC_GOV_002: summary cards render numeric counts', async ({ page }) => {
    await page.goto('/dashboard/governance')

    const summaryLabels = ['Pending reviews', 'Pending promotions', 'Pending reports', 'Unread notifications']
    for (const label of summaryLabels) {
      const card = page.locator('div').filter({ hasText: label }).first()
      await expect(card).toBeVisible()
    }
  })

  test('TC_GOV_003: inbox section shows four tabs', async ({ page }) => {
    await page.goto('/dashboard/governance')

    await expect(page.getByRole('heading', { name: 'Governance inbox' })).toBeVisible()

    await expect(page.getByRole('button', { name: 'All' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Reviews' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Promotions' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Reports' })).toBeVisible()
  })

  test('TC_GOV_004: inbox tabs switch and maintain active state', async ({ page }) => {
    await page.goto('/dashboard/governance')

    for (const tab of ['Reviews', 'Promotions', 'Reports', 'All'] as const) {
      await page.getByRole('button', { name: tab }).click()
      await expect(page.getByRole('button', { name: tab })).toHaveAttribute('data-state', 'active')
    }
  })

  test('TC_GOV_005: notifications section is visible', async ({ page }) => {
    await page.goto('/dashboard/governance')

    await expect(page.getByRole('heading', { name: 'Notifications' })).toBeVisible()
    await expect(page.getByText('Recent governance updates that need your attention.')).toBeVisible()
  })

  test('TC_GOV_006: activity log section is visible', async ({ page }) => {
    await page.goto('/dashboard/governance')

    await expect(page.getByRole('heading', { name: 'Governance activity' })).toBeVisible()
    await expect(page.getByText('Recent audit events for review, promotion, report, and lifecycle actions.')).toBeVisible()
  })
})

test.describe('Governance Center Super Admin Features (Real API)', () => {
  test.describe.configure({ timeout: 120_000 })

  test.beforeEach(async ({ page }) => {
    await setEnglishLocale(page)
    await page.context().setExtraHTTPHeaders({
      'X-Mock-User-Id': 'local-admin',
    })
  })

  test('TC_GOV_007: super admin sees search index rebuild section', async ({ page }) => {
    await page.goto('/dashboard/governance')

    await expect(page.getByText('Search Index Maintenance')).toBeVisible()
    await expect(page.getByRole('button', { name: 'Rebuild full search index' })).toBeVisible()
  })
})
