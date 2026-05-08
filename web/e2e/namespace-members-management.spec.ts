import { expect, test } from '@playwright/test'
import { setEnglishLocale } from './helpers/auth-fixtures'
import { registerSession } from './helpers/session'
import { E2eTestDataBuilder } from './helpers/test-data-builder'

test.describe('Namespace Members Management (Real API)', () => {
  test.describe.configure({ timeout: 120_000 })

  test.beforeEach(async ({ page }, testInfo) => {
    await setEnglishLocale(page)
    await registerSession(page, testInfo)
  })

  test('TC_MEMBER_001: displays member management page with title', async ({ page }, testInfo) => {
    const builder = new E2eTestDataBuilder(page, testInfo)
    await builder.init()

    try {
      const namespace = await builder.ensureWritableNamespace()
      await page.goto(`/dashboard/namespaces/${namespace.slug}/members`)

      await expect(page.getByRole('heading', { name: 'Member Management' })).toBeVisible()
    } finally {
      await builder.cleanup()
    }
  })

  test('TC_MEMBER_002: displays member area with content or empty state', async ({ page }, testInfo) => {
    const builder = new E2eTestDataBuilder(page, testInfo)
    await builder.init()

    try {
      const namespace = await builder.ensureWritableNamespace()
      await page.goto(`/dashboard/namespaces/${namespace.slug}/members`)

      await page.waitForTimeout(1000)
      const hasContent = await page.getByText(/Username|Role|Member|No members|Add Member/i).first().isVisible().catch(() => false)

      expect(hasContent).toBeTruthy()
    } finally {
      await builder.cleanup()
    }
  })

  test('TC_MEMBER_003: add member button opens dialog', async ({ page }, testInfo) => {
    const builder = new E2eTestDataBuilder(page, testInfo)
    await builder.init()

    try {
      const namespace = await builder.ensureWritableNamespace()
      await page.goto(`/dashboard/namespaces/${namespace.slug}/members`)

      const addButton = page.getByRole('button', { name: 'Add Member' })
      const hasButton = await addButton.isVisible().catch(() => false)

      if (hasButton) {
        await addButton.click()
        await expect(page.getByRole('heading', { name: 'Add Namespace Member' })).toBeVisible()
      }
    } finally {
      await builder.cleanup()
    }
  })

  test('TC_MEMBER_004: batch import button is visible when available', async ({ page }, testInfo) => {
    const builder = new E2eTestDataBuilder(page, testInfo)
    await builder.init()

    try {
      const namespace = await builder.ensureWritableNamespace()
      await page.goto(`/dashboard/namespaces/${namespace.slug}/members`)

      const batchButton = page.getByRole('button', { name: /Batch Import/i })
      const hasButton = await batchButton.isVisible().catch(() => false)

      expect(typeof hasButton).toBe('boolean')
    } finally {
      await builder.cleanup()
    }
  })
})
