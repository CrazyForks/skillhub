import { describe, expect, it } from 'vitest'
import * as mod from './delete-namespace-dialog'

/**
 * delete-namespace-dialog.tsx exports the DeleteNamespaceDialog component.
 * We verify the public export contract here.
 */
describe('delete-namespace-dialog module exports', () => {
  it('exports the DeleteNamespaceDialog component', () => {
    expect(mod.DeleteNamespaceDialog).toBeDefined()
    expect(typeof mod.DeleteNamespaceDialog).toBe('function')
  })
})
