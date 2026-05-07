/**
 * Live-backend helpers for Track B (real registry e2e).
 *
 * Tests in this directory expect a real SkillHub backend running and a valid
 * API token. They are opt-in via env vars; without them the suites self-skip.
 *
 *   SKILLHUB_E2E_REGISTRY  e.g. http://localhost:3000
 *   SKILLHUB_E2E_TOKEN     a bearer token with publish/delete permission
 *
 * Convention: every published artifact uses a unique time-based slug so
 * concurrent or repeated runs don't collide. afterEach hooks call
 * deleteRemote() which is best-effort and never throws.
 */
import { runCli } from '../../helpers/run-cli'

export interface LiveRegistry {
  url: string
  token: string
}

export function getLiveRegistry(): LiveRegistry | null {
  const url = process.env.SKILLHUB_E2E_REGISTRY
  const token = process.env.SKILLHUB_E2E_TOKEN
  if (!url || !token) return null
  return { url, token }
}

/**
 * Probe the configured registry's whoami endpoint with the provided token.
 * Returns true only on 200 OK so misconfigured (auth-failed) backends fall
 * through to skip rather than producing 401-storm errors during tests.
 */
export async function isLiveRegistryReachable(reg: LiveRegistry): Promise<boolean> {
  try {
    const res = await fetch(`${reg.url}/api/cli/v1/auth/whoami`, {
      headers: { Authorization: `Bearer ${reg.token}` },
      signal: AbortSignal.timeout(3000)
    })
    return res.ok
  } catch {
    return false
  }
}

/**
 * Build a slug guaranteed-unique for this test process. Format:
 *   e2e-trackb-<base36 timestamp>-<4-char base36 random>
 * Stays well under any reasonable slug length cap and matches the kebab-case
 * pattern used elsewhere in the project.
 */
export function uniqueSlug(prefix = 'e2e-trackb'): string {
  const ts = Date.now().toString(36)
  const rand = Math.random().toString(36).slice(2, 6)
  return `${prefix}-${ts}-${rand}`
}

/**
 * Best-effort cleanup of a remotely-published skill. Swallow all errors so
 * cleanup of one fixture never breaks the next test's afterEach.
 */
export async function deleteRemote(reg: LiveRegistry, slug: string, namespace = 'global'): Promise<void> {
  try {
    await runCli(
      [
        'remove', slug,
        '--remote', '--hard',
        '--namespace', namespace,
        '--registry', reg.url,
        '--token', reg.token,
        '--json'
      ],
      {}
    )
  } catch {
    // Cleanup is best-effort; do not let it fail the suite.
  }
}

/**
 * Poll a function until its result satisfies the predicate or the deadline
 * expires. Used to absorb async indexing latency between publish and search.
 */
export async function eventually<T>(
  fn: () => Promise<T>,
  predicate: (value: T) => boolean,
  options: { timeoutMs?: number; intervalMs?: number } = {}
): Promise<T> {
  const timeoutMs = options.timeoutMs ?? 5000
  const intervalMs = options.intervalMs ?? 250
  const deadline = Date.now() + timeoutMs
  let last: T | undefined
  // First attempt is immediate; we then back off.
  while (Date.now() < deadline) {
    last = await fn()
    if (predicate(last)) return last
    await Bun.sleep(intervalMs)
  }
  const preview = (() => {
    try { return JSON.stringify(last).slice(0, 500) } catch { return String(last) }
  })()
  throw new Error(`eventually: predicate not satisfied within ${timeoutMs}ms; last value: ${preview}`)
}
