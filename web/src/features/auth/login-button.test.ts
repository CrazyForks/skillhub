import { describe, expect, it } from 'vitest'
import type { AuthMethod } from '@/api/types'
import { LoginButton, isExternalRedirectMethod } from './login-button'

function method(overrides: Partial<AuthMethod>): AuthMethod {
  return {
    id: overrides.id ?? 'test',
    methodType: overrides.methodType ?? 'PASSWORD',
    provider: overrides.provider ?? 'test',
    displayName: overrides.displayName ?? 'Test',
    actionUrl: overrides.actionUrl ?? '/test',
  }
}

describe('isExternalRedirectMethod', () => {
  it('matches OAUTH_REDIRECT methods', () => {
    expect(isExternalRedirectMethod(method({ methodType: 'OAUTH_REDIRECT' }))).toBe(true)
  })

  it('matches CAS_REDIRECT methods', () => {
    expect(isExternalRedirectMethod(method({ methodType: 'CAS_REDIRECT' }))).toBe(true)
  })

  it('rejects local password method', () => {
    expect(isExternalRedirectMethod(method({ methodType: 'PASSWORD' }))).toBe(false)
  })

  it('rejects direct password method', () => {
    expect(isExternalRedirectMethod(method({ methodType: 'DIRECT_PASSWORD' }))).toBe(false)
  })

  it('rejects session-bootstrap method', () => {
    expect(isExternalRedirectMethod(method({ methodType: 'SESSION_BOOTSTRAP' }))).toBe(false)
  })
})

describe('login-button module exports', () => {
  it('exports LoginButton component', () => {
    expect(LoginButton).toBeTypeOf('function')
  })
})
