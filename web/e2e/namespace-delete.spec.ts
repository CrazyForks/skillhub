import type { Page } from '@playwright/test'
import { expect, test } from '@playwright/test'
import { setEnglishLocale } from './helpers/auth-fixtures'
import { loginWithCredentials } from './helpers/session'
import { E2eTestDataBuilder, type SeededNamespace } from './helpers/test-data-builder'

function getAdminCredentials() {
  const username = process.env.E2E_ADMIN_USERNAME ?? process.env.BOOTSTRAP_ADMIN_USERNAME ?? 'admin'
  const password = process.env.E2E_ADMIN_PASSWORD ?? process.env.BOOTSTRAP_ADMIN_PASSWORD ?? 'ChangeMe!2026'
  return { username, password }
}

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

// Namespace lifecycle ties archive and delete to the OWNER role, and only
// SKILL_ADMIN/SUPER_ADMIN can create a TEAM namespace via the portal API.
// Since there is no REST endpoint to transfer ownership to a regular E2E user,
// we run this spec under the admin account so the created namespace belongs to
// the acting session (creator becomes OWNER automatically).
test.describe('Namespace Delete (Real API)', () => {
  test.beforeEach(async ({ page }) => {
    await setEnglishLocale(page)
  })

  test('delete button only visible after namespace is archived', async ({ page }, testInfo) => {
    await loginWithCredentials(page, getAdminCredentials(), testInfo)
    const builder = new E2eTestDataBuilder(page, testInfo)
    await builder.init()

    try {
      const namespace = await builder.createNamespace('e2e-delete')

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
    await loginWithCredentials(page, getAdminCredentials(), testInfo)
    const builder = new E2eTestDataBuilder(page, testInfo)
    await builder.init()

    try {
      const namespace = await builder.createNamespace('e2e-delete')
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
    await loginWithCredentials(page, getAdminCredentials(), testInfo)
    const builder = new E2eTestDataBuilder(page, testInfo)
    await builder.init()

    try {
      const namespace = await builder.createNamespace('e2e-delete')
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
