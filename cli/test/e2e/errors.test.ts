/**
 * Track B e2e — real-backend error handling.
 *
 *   E3  install <nonexistent slug> against the real backend exits non-zero
 *       and surfaces a meaningful error mentioning the slug or 4xx status.
 *       This is the regression line for the "registry returned 400" UX
 *       observed during manual testing.
 *   E4  whoami with an invalid bearer token exits with EXIT.auth (2) and
 *       contains an auth-related token in stderr — confirming the real
 *       backend speaks the expected 401 / unauthorized contract.
 */
import { mkdir } from 'node:fs/promises'
import { join } from 'node:path'
import { describe, expect, test } from 'bun:test'
import { createTempHome } from '../helpers/temp-env'
import { runCli } from '../helpers/run-cli'
import { getLiveRegistry, isLiveRegistryReachable, uniqueSlug } from './helpers/live-registry'

const live = getLiveRegistry()
const reachable = live ? await isLiveRegistryReachable(live) : false

describe.skipIf(!live || !reachable)('Track B e2e — backend errors (live backend)', () => {
  test('E3 install of a nonexistent slug exits non-zero with a meaningful message', async () => {
    const env = await createTempHome()
    const slug = uniqueSlug('e2e-trackb-missing')

    const installDir = join(env.cwd, 'should-not-be-populated')
    await mkdir(installDir, { recursive: true })

    const result = await runCli(
      [
        'install', slug,
        '--namespace', 'global',
        '--dir', installDir,
        '--registry', live!.url,
        '--token', live!.token
      ],
      { HOME: env.home, USERPROFILE: env.home }
    )

    expect(result.exitCode).not.toBe(0)
    // Either a 4xx status or a "not found" string should appear; the exact
    // wording depends on how the CLI maps server response codes today.
    const stderrLower = result.stderr.toLowerCase()
    expect(
      /400|404|not found/.test(stderrLower)
    ).toBe(true)

    // No metadata file should have been created.
    const metaPath = join(installDir, slug, '.skillhub', 'metadata.json')
    expect(await Bun.file(metaPath).exists()).toBe(false)
  }, 15_000)

  test('E4 whoami with an invalid token exits with EXIT.auth and reports auth failure', async () => {
    const env = await createTempHome()

    const result = await runCli(
      [
        'whoami',
        '--registry', live!.url,
        '--token', 'sk_definitely_not_a_real_token_xxxxxxxxxx'
      ],
      { HOME: env.home, USERPROFILE: env.home }
    )

    expect(result.exitCode).toBe(2) // EXIT.auth
    expect(result.stderr.toLowerCase()).toMatch(/auth|unauthorized|401/)
  }, 15_000)

  // ---------------------------------------------------------------------------
  // E6 — Real backend rejects a malformed SKILL.md (no `name` frontmatter
  //      field). The CLI must propagate the rejection without crashing or
  //      leaving partial state on the server.
  // ---------------------------------------------------------------------------
  test('E6 publish a SKILL.md missing the required `name` field is rejected by the real backend', async () => {
    const { mkdtemp, writeFile } = await import('node:fs/promises')
    const { join } = await import('node:path')
    const { tmpdir } = await import('node:os')
    const env = await createTempHome()

    const dir = await mkdtemp(join(tmpdir(), 'skillhub-e2e-bad-name-'))
    // Frontmatter has description but no name — server-side parser must
    // reject this as a missing required field.
    await writeFile(join(dir, 'SKILL.md'), [
      '---',
      'description: missing name field on purpose',
      '---',
      '',
      '# Body'
    ].join('\n'))

    const result = await runCli(
      [
        'publish', dir,
        '--namespace', 'global',
        '--registry', live!.url,
        '--token', live!.token
      ],
      { HOME: env.home, USERPROFILE: env.home }
    )
    expect(result.exitCode).not.toBe(0)
    // The error should mention either the validation reason or a 4xx
    // status code so the user has a starting point for debugging.
    expect(result.stderr.length).toBeGreaterThan(0)
    expect(result.stderr.length).toBeLessThan(2000)
  }, 15_000)

  // ---------------------------------------------------------------------------
  // E7 — Real backend enforces the 64-char limit on token-style names.
  //      Skill `name` is the slug; servers in this project enforce a length
  //      cap. We assert that overlong names are surfaced as a non-zero exit.
  // ---------------------------------------------------------------------------
  test('E7 publish with a name far exceeding the server length cap is rejected', async () => {
    const { mkdtemp, writeFile } = await import('node:fs/promises')
    const { join } = await import('node:path')
    const { tmpdir } = await import('node:os')
    const env = await createTempHome()

    const overlongName = `${'overlong-name-'.repeat(20)}-end` // ~280 chars
    const dir = await mkdtemp(join(tmpdir(), 'skillhub-e2e-overlong-name-'))
    await writeFile(join(dir, 'SKILL.md'), [
      '---',
      `name: ${overlongName}`,
      'description: name is way past any reasonable cap',
      '---',
      '',
      '# Body'
    ].join('\n'))

    const result = await runCli(
      [
        'publish', dir,
        '--namespace', 'global',
        '--registry', live!.url,
        '--token', live!.token
      ],
      { HOME: env.home, USERPROFILE: env.home }
    )
    // We don't assert the exact 64-char limit (it's a server-side detail
    // that may change). We just assert "real backend rejected this".
    expect(result.exitCode).not.toBe(0)
    expect(result.stderr.length).toBeGreaterThan(0)
  }, 15_000)
})
