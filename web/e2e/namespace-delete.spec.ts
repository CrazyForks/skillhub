import type { Page } from '@playwright/test'
import { expect, test } from '@playwright/test'
import { setEnglishLocale } from './helpers/auth-fixtures'
import { registerSession } from './helpers/session'
import { E2eTestDataBuilder, type SeededNamespace } from './helpers/test-data-builder'

async function archiveNamespaceViaApi(page: Page, slug: string): Promise<void> {
  const response = await page.context().request.post(
    `/api/web/namespaces/${encodeURIComponent(slug)}/archive`,
    { data: { reason: 'E2E prepare for deletion' } },
  )
  expect(response.ok(), `archive API failed: ${response.status()}`).toBeTruthy()
}

function namespaceCard(page: Page, namespace: SeededNamespace) {
  return page
    .locator('div')
    .filter({ hasText: `@${namespace.slug}` })
    .filter({ has: page.getByRole('heading', { name: namespace.displayName }) })
    .first()
}

test.describe('Namespace Delete (Real API)', () => {
  test.beforeEach(async ({ page }, testInfo) => {
    await setEnglishLocale(page)
    await registerSession(page, testInfo)
  })

  test('delete button only visible after namespace is archived', async ({ page }, testInfo) => {
    const builder = new E2eTestDataBuilder(page, testInfo)
    await builder.init()

    try {
      const namespace = await builder.ensureWritableNamespace()

      await page.goto('/dashboard/namespaces')
      await expect(page.getByText(`@${namespace.slug}`)).toBeVisible()

      const activeCard = namespaceCard(page, namespace)
      await expect(activeCard.getByRole('button', { name: /^delete$/i })).toHaveCount(0)

      await archiveNamespaceViaApi(page, namespace.slug)

      await page.reload()
      await expect(page.getByText(`@${namespace.slug}`)).toBeVisible()

      const archivedCard = namespaceCard(page, namespace)
      await expect(archivedCard.getByRole('button', { name: /^delete$/i })).toBeVisible()
    } finally {
      await builder.cleanup()
    }
  })

  test('delete confirm button stays disabled until reason is provided', async ({ page }, testInfo) => {
    const builder = new E2eTestDataBuilder(page, testInfo)
    await builder.init()

    try {
      const namespace = await builder.ensureWritableNamespace()
      await archiveNamespaceViaApi(page, namespace.slug)

      await page.goto('/dashboard/namespaces')
      await expect(page.getByText(`@${namespace.slug}`)).toBeVisible()

      const card = namespaceCard(page, namespace)
      await card.getByRole('button', { name: /^delete$/i }).click()

      const dialog = page.getByRole('dialog')
      await expect(dialog).toBeVisible()

      const confirmButton = dialog.getByRole('button', { name: /^delete$/i })
      await expect(confirmButton).toBeDisabled()

      await dialog.getByRole('textbox').fill('No longer needed')
      await expect(confirmButton).toBeEnabled()
    } finally {
      await builder.cleanup()
    }
  })

  test('successfully deletes an archived namespace', async ({ page }, testInfo) => {
    const builder = new E2eTestDataBuilder(page, testInfo)
    await builder.init()

    try {
      const namespace = await builder.ensureWritableNamespace()
      await archiveNamespaceViaApi(page, namespace.slug)

      await page.goto('/dashboard/namespaces')
      await expect(page.getByText(`@${namespace.slug}`)).toBeVisible()

      const card = namespaceCard(page, namespace)
      await card.getByRole('button', { name: /^delete$/i }).click()

      const dialog = page.getByRole('dialog')
      await expect(dialog).toBeVisible()

      await dialog.getByRole('textbox').fill('No longer needed')
      await dialog.getByRole('button', { name: /^delete$/i }).click()

      await expect(dialog).toBeHidden({ timeout: 10_000 })
      await expect(page.getByText(`@${namespace.slug}`)).toHaveCount(0, { timeout: 10_000 })
    } finally {
      await builder.cleanup()
    }
  })
})
