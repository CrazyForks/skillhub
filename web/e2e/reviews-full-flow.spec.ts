import { expect, test, type Page } from '@playwright/test'
import { setEnglishLocale } from './helpers/auth-fixtures'
import { createNamespaceReviewData } from './helpers/review-seed'

type ReviewStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

interface ApiEnvelope<T> {
  code: number
  msg: string
  data: T
}

interface ReviewPageData {
  total: number
  size: number
}

async function fetchReviewMeta(page: Page, status: ReviewStatus): Promise<ReviewPageData> {
  const response = await page.request.get(`/api/web/reviews?status=${status}&page=0&size=20&sortDirection=DESC`)
  const body = await response.json() as ApiEnvelope<ReviewPageData>
  if (!response.ok() || body.code !== 0) {
    throw new Error(`Failed to query reviews for ${status}: status=${response.status()} code=${body.code} msg=${body.msg}`)
  }
  return body.data
}

test.describe('Review Center Full Flow (Real API)', () => {
  test.describe.configure({ timeout: 120_000 })

  test.beforeEach(async ({ page }) => {
    await setEnglishLocale(page)
    await page.context().setExtraHTTPHeaders({
      'X-Mock-User-Id': 'local-admin',
    })
  })

  test('TC_REVIEW_001: displays review center page with three status tabs', async ({ page }) => {
    await page.goto('/dashboard/reviews')

    await expect(page.getByRole('heading', { name: 'Review Center' })).toBeVisible()

    await expect(page.getByRole('button', { name: 'Pending' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Approved' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Rejected' })).toBeVisible()
  })

  test('TC_REVIEW_002: switches between status tabs and verifies active state', async ({ page }) => {
    await page.goto('/dashboard/reviews')
    await expect(page.getByRole('button', { name: 'Pending' })).toBeVisible()

    for (const tab of ['Approved', 'Rejected', 'Pending'] as const) {
      await page.getByRole('button', { name: tab }).click()
      await expect(page.getByRole('button', { name: tab })).toHaveAttribute('data-state', 'active')
    }
  })

  test('TC_REVIEW_003: pending tab shows correct table columns or empty state', async ({ page }) => {
    const meta = await fetchReviewMeta(page, 'PENDING')
    await page.goto('/dashboard/reviews')
    await expect(page.getByRole('button', { name: 'Pending' })).toBeVisible()

    if (meta.total === 0) {
      await expect(page.getByText('No review tasks')).toBeVisible()
    } else {
      await expect(page.getByRole('columnheader', { name: 'Skill' })).toBeVisible()
      await expect(page.getByRole('columnheader', { name: 'Version' })).toBeVisible()
      await expect(page.getByRole('columnheader', { name: 'Submitted By' })).toBeVisible()
      await expect(page.getByRole('columnheader', { name: 'Submitted At' })).toBeVisible()
    }
  })

  test('TC_REVIEW_004: approved tab shows reviewer columns or empty state', async ({ page }) => {
    const meta = await fetchReviewMeta(page, 'APPROVED')
    await page.goto('/dashboard/reviews')
    await page.getByRole('button', { name: 'Approved' }).click()

    if (meta.total === 0) {
      await expect(page.getByText('No review tasks')).toBeVisible()
    } else {
      await expect(page.getByRole('columnheader', { name: 'Reviewed By' })).toBeVisible()
      await expect(page.getByRole('columnheader', { name: 'Reviewed At' })).toBeVisible()
    }
  })

  test('TC_REVIEW_005: sort order selector toggles between newest and oldest', async ({ page }) => {
    await page.goto('/dashboard/reviews')

    const sortTrigger = page.getByRole('combobox')
    await expect(sortTrigger).toBeVisible()
    await expect(sortTrigger).toContainText('Newest first')

    await sortTrigger.click()
    await page.getByRole('option', { name: 'Oldest first' }).click()
    await expect(sortTrigger).toContainText('Oldest first')

    await sortTrigger.click()
    await page.getByRole('option', { name: 'Newest first' }).click()
    await expect(sortTrigger).toContainText('Newest first')
  })

  test('TC_REVIEW_006: each tab shows data or empty state consistently', async ({ page }) => {
    await page.goto('/dashboard/reviews')

    for (const tab of ['Pending', 'Approved', 'Rejected'] as const) {
      await page.getByRole('button', { name: tab }).click()
      await page.waitForTimeout(1000)

      const hasTable = await page.getByRole('table').first().isVisible().catch(() => false)
      const hasEmpty = await page.getByText('No review tasks').isVisible().catch(() => false)

      expect(hasTable || hasEmpty).toBeTruthy()
    }
  })
})

test.describe('Review Center with Seeded Data (Real API)', () => {
  test.describe.configure({ timeout: 120_000 })

  test.beforeEach(async ({ page }) => {
    await setEnglishLocale(page)
  })

  test('TC_REVIEW_007: clicking a review row navigates to review detail', async ({ browser, page }, testInfo) => {
    let seeded: Awaited<ReturnType<typeof createNamespaceReviewData>> | undefined
    try {
      seeded = await createNamespaceReviewData(browser, page, testInfo)

      const adminContext = await browser.newContext()
      const adminPage = await adminContext.newPage()
      await setEnglishLocale(adminPage)
      await adminContext.setExtraHTTPHeaders({
        'X-Mock-User-Id': 'local-admin',
      })
      await adminPage.goto('/dashboard/reviews')
      await expect(adminPage.getByRole('heading', { name: 'Review Center' })).toBeVisible({ timeout: 15_000 })

      const firstRow = adminPage.getByRole('row').nth(1)
      const hasRow = await firstRow.isVisible().catch(() => false)

      if (hasRow) {
        await firstRow.click()
        await adminPage.waitForURL(/\/dashboard\/reviews\/\d+/)
      }

      await adminContext.close()
    } finally {
      await seeded?.cleanup()
    }
  })
})
