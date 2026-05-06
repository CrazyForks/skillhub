import { expect, test } from '@playwright/test'
import { setEnglishLocale } from './helpers/auth-fixtures'
import { registerSession } from './helpers/session'
import { E2eTestDataBuilder } from './helpers/test-data-builder'

test.describe('Namespace Delete (Real API)', () => {
  test.beforeEach(async ({ page }, testInfo) => {
    await setEnglishLocale(page)
    await registerSession(page, testInfo)
  })

  test('successfully deletes an archived namespace', async ({ page }, testInfo) => {
    const builder = new E2eTestDataBuilder(page, testInfo)
    await builder.init()

    try {
      const namespace = await builder.ensureWritableNamespace()

      await page.goto('/dashboard/namespaces')
      await expect(page.getByText(`@${namespace.slug}`)).toBeVisible()

      await page.getByRole('link', { name: `@${namespace.slug}` }).click()
      await expect(page.getByRole('heading', { name: namespace.displayName })).toBeVisible()

      const archiveButton = page.getByRole('button', { name: /archive/i })
      await expect(archiveButton).toBeVisible()
      await archiveButton.click()

      const archiveDialog = page.getByRole('dialog')
      await expect(archiveDialog).toBeVisible()

      const reasonTextarea = archiveDialog.getByRole('textbox')
      await reasonTextarea.fill('Preparing for deletion')

      const confirmArchiveButton = archiveDialog.getByRole('button', { name: /archive/i })
      await confirmArchiveButton.click()

      await expect(page.getByText(/archived/i)).toBeVisible({ timeout: 10000 })

      const deleteButton = page.getByRole('button', { name: /delete/i })
      await expect(deleteButton).toBeVisible()
      await deleteButton.click()

      const deleteDialog = page.getByRole('dialog')
      await expect(deleteDialog).toBeVisible()
      await expect(deleteDialog.getByText(namespace.displayName)).toBeVisible()

      const deleteReasonTextarea = deleteDialog.getByRole('textbox')
      await deleteReasonTextarea.fill('No longer needed')

      const confirmDeleteButton = deleteDialog.getByRole('button', { name: /delete/i })
      await expect(confirmDeleteButton).toBeEnabled()
      await confirmDeleteButton.click()

      await expect(page).toHaveURL('/dashboard/namespaces', { timeout: 10000 })
      await expect(page.getByText(`@${namespace.slug}`)).not.toBeVisible()
    } finally {
      await builder.cleanup()
    }
  })

  test('delete button only visible when namespace is archived', async ({ page }, testInfo) => {
    const builder = new E2eTestDataBuilder(page, testInfo)
    await builder.init()

    try {
      const namespace = await builder.ensureWritableNamespace()

      await page.goto('/dashboard/namespaces')
      await page.getByRole('link', { name: `@${namespace.slug}` }).click()
      await expect(page.getByRole('heading', { name: namespace.displayName })).toBeVisible()

      await expect(page.getByRole('button', { name: /delete/i })).not.toBeVisible()

      const archiveButton = page.getByRole('button', { name: /archive/i })
      await archiveButton.click()

      const archiveDialog = page.getByRole('dialog')
      const reasonTextarea = archiveDialog.getByRole('textbox')
      await reasonTextarea.fill('Test archive')

      const confirmArchiveButton = archiveDialog.getByRole('button', { name: /archive/i })
      await confirmArchiveButton.click()

      await expect(page.getByText(/archived/i)).toBeVisible({ timeout: 10000 })
      await expect(page.getByRole('button', { name: /delete/i })).toBeVisible()
    } finally {
      await builder.cleanup()
    }
  })

  test('requires delete reason to be filled', async ({ page }, testInfo) => {
    const builder = new E2eTestDataBuilder(page, testInfo)
    await builder.init()

    try {
      const namespace = await builder.ensureWritableNamespace()

      await page.goto('/dashboard/namespaces')
      await page.getByRole('link', { name: `@${namespace.slug}` }).click()

      const archiveButton = page.getByRole('button', { name: /archive/i })
      await archiveButton.click()

      const archiveDialog = page.getByRole('dialog')
      const reasonTextarea = archiveDialog.getByRole('textbox')
      await reasonTextarea.fill('Test archive')

      const confirmArchiveButton = archiveDialog.getByRole('button', { name: /archive/i })
      await confirmArchiveButton.click()

      await expect(page.getByText(/archived/i)).toBeVisible({ timeout: 10000 })

      const deleteButton = page.getByRole('button', { name: /delete/i })
      await deleteButton.click()

      const deleteDialog = page.getByRole('dialog')
      await expect(deleteDialog).toBeVisible()

      const confirmDeleteButton = deleteDialog.getByRole('button', { name: /delete/i })
      await expect(confirmDeleteButton).toBeDisabled()

      const deleteReasonTextarea = deleteDialog.getByRole('textbox')
      await deleteReasonTextarea.fill('No longer needed')

      await expect(confirmDeleteButton).toBeEnabled()
    } finally {
      await builder.cleanup()
    }
  })
})
