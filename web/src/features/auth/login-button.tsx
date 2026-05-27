import { useTranslation } from 'react-i18next'
import { Button } from '@/shared/ui/button'
import type { AuthMethod } from '@/api/types'
import { useAuthMethods } from './use-auth-methods'

interface LoginButtonProps {
  returnTo?: string
}

/**
 * Method types this button renders. CAS uses the same redirect-and-callback shape as OAuth from
 * the UI's perspective, so we treat both as external-redirect providers.
 */
export function isExternalRedirectMethod(method: AuthMethod): boolean {
  return method.methodType === 'OAUTH_REDIRECT' || method.methodType === 'CAS_REDIRECT'
}

function ExternalProviderIcon({ provider }: { provider: string }) {
  const normalizedProvider = provider.toLowerCase()
  return (
    <img
      src={`/${normalizedProvider}-logo.svg`}
      alt={provider}
      className="w-5 h-5 mr-3"
    />
  )
}

/**
 * Renders external-IdP login buttons (OAuth and CAS) from the auth-method catalog.
 */
export function LoginButton({ returnTo }: LoginButtonProps) {
  const { t } = useTranslation()
  const { data, isLoading } = useAuthMethods(returnTo)

  const providers = (data ?? []).filter(isExternalRedirectMethod)

  if (isLoading) {
    return (
      <div className="space-y-3">
        <Button className="w-full h-12" disabled>
          <div className="w-5 h-5 rounded-full animate-shimmer mr-3" />
          {t('loginButton.loading')}
        </Button>
      </div>
    )
  }

  return (
    <div className="space-y-3">
      {providers.map((provider) => (
        <Button
          key={provider.id}
          className="w-full h-12 text-base"
          variant="outline"
          onClick={() => {
            window.location.href = provider.actionUrl
          }}
        >
          <ExternalProviderIcon provider={provider.provider} />
          {t('loginButton.loginWith', { name: provider.displayName })}
        </Button>
      ))}
    </div>
  )
}

