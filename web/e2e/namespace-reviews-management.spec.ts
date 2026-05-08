import { expect, test } from '@playwright/test'
import { setEnglishLocale } from './helpers/auth-fixtures'
import { createNamespaceReviewData } from './helpers/review-seed'

test.describe('Namespace Reviews Management (Real API)', () => {
  test.describe.configure({ timeout: 120_000 })

  test.beforeEach(async ({ page }) => {
    await setEnglishLocale(page)
  })

  test('TC_NS_REVIEW_001: displays namespace reviews page with title and context', async ({ browser, page }, testInfo) => {
    let seeded: Awaited<ReturnType<typeof createNamespaceReviewData>> | undefined
    try {
      seeded = await createNamespaceReviewData(browser, page, testInfo)
      await page.goto(`/dashboard/namespaces/${seeded.namespace.slug}/reviews`)

      await expect(page.getByRole('heading', { name: 'Namespace Reviews' })).toBeVisible()
      await expect(page.getByText(`Review tasks for ${seeded.namespace.displayName}`)).toBeVisible()
    } finally {
      await seeded?.cleanup()
    }
  })

  test('TC_NS_REVIEW_002: displays three status tabs', async ({ browser, page }, testInfo) => {
    let seeded: Awaited<ReturnType<typeof createNamespaceReviewData>> | undefined
    try {
      seeded = await createNamespaceReviewData(browser, page, testInfo)
      await page.goto(`/dashboard/namespaces/${seeded.namespace.slug}/reviews`)

      await expect(page.getByRole('button', { name: 'Pending' })).toBeVisible()
      await expect(page.getByRole('button', { name: 'Approved' })).toBeVisible()
      await expect(page.getByRole('button', { name: 'Rejected' })).toBeVisible()
    } finally {
      await seeded?.cleanup()
    }
  })

  test('TC_NS_REVIEW_003: tabs switch and maintain active state', async ({ browser, page }, testInfo) => {
    let seeded: Awaited<ReturnType<typeof createNamespaceReviewData>> | undefined
    try {
      seeded = await createNamespaceReviewData(browser, page, testInfo)
      await page.goto(`/dashboard/namespaces/${seeded.namespace.slug}/reviews`)

      for (const tab of ['Approved', 'Rejected', 'Pending'] as const) {
        await page.getByRole('button', { name: tab }).click()
        await expect(page.getByRole('button', { name: tab })).toHaveAttribute('data-state', 'active')
      }
    } finally {
      await seeded?.cleanup()
    }
  })

  test('TC_NS_REVIEW_004: sort order selector works', async ({ browser, page }, testInfo) => {
    let seeded: Awaited<ReturnType<typeof createNamespaceReviewData>> | undefined
    try {
      seeded = await createNamespaceReviewData(browser, page, testInfo)
      await page.goto(`/dashboard/namespaces/${seeded.namespace.slug}/reviews`)

      const sortTrigger = page.getByRole('combobox')
      await expect(sortTrigger).toBeVisible()
      await expect(sortTrigger).toContainText('Newest first')

      await sortTrigger.click()
      await page.getByRole('option', { name: 'Oldest first' }).click()
      await expect(sortTrigger).toContainText('Oldest first')
    } finally {
      await seeded?.cleanup()
    }
  })
})
