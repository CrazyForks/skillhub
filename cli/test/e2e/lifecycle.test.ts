/**
 * Track B e2e — full lifecycle against a real SkillHub backend.
 *
 * Covers two scenarios that fake-registry cannot reproduce faithfully:
 *   E1  publish → search (exact slug) → install → SKILL.md content matches
 *       the published body → remove --remote --hard → search no longer hits.
 *   E5  publish → search by keyword (substring of slug) → result set
 *       contains the new skill (exercises the search index, not just exact
 *       lookup).
 *
 * Skipped automatically when SKILLHUB_E2E_REGISTRY / SKILLHUB_E2E_TOKEN are
 * not set or the backend is unreachable. See helpers/live-registry.ts.
 */
import { mkdir, readFile } from 'node:fs/promises'
import { join } from 'node:path'
import { afterEach, describe, expect, test } from 'bun:test'
import { createTempHome } from '../helpers/temp-env'
import { runCli } from '../helpers/run-cli'
import {
  deleteRemote,
  eventually,
  getLiveRegistry,
  isLiveRegistryReachable,
  uniqueSlug
} from './helpers/live-registry'
import { makeSkillDir } from './helpers/skill-fixtures'

const live = getLiveRegistry()
const reachable = live ? await isLiveRegistryReachable(live) : false

describe.skipIf(!live || !reachable)('Track B e2e — lifecycle (live backend)', () => {
  // Track every slug that gets published so afterEach can delete them even
  // when the test body throws partway through.
  const publishedSlugs: string[] = []

  afterEach(async () => {
    while (publishedSlugs.length > 0) {
      const slug = publishedSlugs.pop()!
      await deleteRemote(live!, slug)
    }
  })

  test('E1 publish → search exact slug → install matches body → remote remove', async () => {
    const env = await createTempHome()
    const slug = uniqueSlug('e2e-trackb-life')
    const fixture = await makeSkillDir(slug)

    // --- publish -----------------------------------------------------------
    const pubResult = await runCli(
      [
        'publish', fixture.dir,
        '--namespace', 'global',
        '--registry', live!.url,
        '--token', live!.token,
        '--json'
      ],
      { HOME: env.home, USERPROFILE: env.home }
    )
    if (pubResult.exitCode !== 0) {
      // eslint-disable-next-line no-console
      console.error('E1 publish failed', { stdout: pubResult.stdout, stderr: pubResult.stderr })
    }
    expect(pubResult.exitCode).toBe(0)
    const pubJson = JSON.parse(pubResult.stdout) as {
      ok: boolean
      slug: string
      version: string
      namespace: string
    }
    expect(pubJson.ok).toBe(true)
    expect(pubJson.slug).toBe(slug)
    expect(pubJson.namespace).toBe('global')
    publishedSlugs.push(slug)

    // --- search (exact slug) — allow indexing latency ---------------------
    await eventually(
      () => runCli(
        ['search', slug, '--registry', live!.url, '--json'],
        { HOME: env.home, USERPROFILE: env.home }
      ),
      r => r.exitCode === 0 && JSON.parse(r.stdout).items.some(
        (i: { slug: string }) => i.slug === slug
      ),
      { timeoutMs: 8000, intervalMs: 300 }
    )

    // --- install -----------------------------------------------------------
    const installDir = join(env.cwd, 'live-install')
    await mkdir(installDir, { recursive: true })
    const insResult = await runCli(
      [
        'install', slug,
        '--namespace', 'global',
        '--dir', installDir,
        '--registry', live!.url,
        '--token', live!.token
      ],
      { HOME: env.home, USERPROFILE: env.home }
    )
    expect(insResult.exitCode).toBe(0)

    // --- content integrity check ------------------------------------------
    const skillMd = await readFile(join(installDir, slug, 'SKILL.md'), 'utf-8')
    expect(skillMd).toContain(`name: ${slug}`)
    expect(skillMd).toContain(fixture.bodyMarker)

    const meta = JSON.parse(
      await readFile(join(installDir, slug, '.skillhub', 'metadata.json'), 'utf-8')
    ) as { registry: string; namespace: string; slug: string; version: string }
    expect(meta.namespace).toBe('global')
    expect(meta.slug).toBe(slug)
    expect(meta.version).toBe(pubJson.version)
    expect(meta.registry).toBe(live!.url)

    // --- remote remove + post-condition: search no longer hits ------------
    const rmResult = await runCli(
      [
        'remove', slug,
        '--remote', '--hard',
        '--namespace', 'global',
        '--registry', live!.url,
        '--token', live!.token,
        '--json'
      ],
      { HOME: env.home, USERPROFILE: env.home }
    )
    expect(rmResult.exitCode).toBe(0)
    // Mark cleanup-done so afterEach doesn't double-delete.
    publishedSlugs.length = 0

    await eventually(
      () => runCli(
        ['search', slug, '--registry', live!.url, '--json'],
        { HOME: env.home, USERPROFILE: env.home }
      ),
      r => r.exitCode === 0 && JSON.parse(r.stdout).items.every(
        (i: { slug: string }) => i.slug !== slug
      ),
      { timeoutMs: 8000, intervalMs: 300 }
    )
  }, 30_000)

  test('E5 search by keyword substring finds a freshly-published skill', async () => {
    const env = await createTempHome()
    // Embed a distinctive token in the slug; we'll search for that token
    // alone, not the full slug, so we exercise substring/keyword matching.
    const keyword = `kw${Date.now().toString(36).slice(-6)}`
    const slug = `e2e-trackb-${keyword}-search`
    const fixture = await makeSkillDir(slug, {
      description: `Has the keyword ${keyword} for substring search.`
    })

    const pubResult = await runCli(
      [
        'publish', fixture.dir,
        '--namespace', 'global',
        '--registry', live!.url,
        '--token', live!.token,
        '--json'
      ],
      { HOME: env.home, USERPROFILE: env.home }
    )
    expect(pubResult.exitCode).toBe(0)
    publishedSlugs.push(slug)

    await eventually(
      () => runCli(
        ['search', keyword, '--registry', live!.url, '--json'],
        { HOME: env.home, USERPROFILE: env.home }
      ),
      r => r.exitCode === 0 && JSON.parse(r.stdout).items.some(
        (i: { slug: string }) => i.slug === slug
      ),
      { timeoutMs: 8000, intervalMs: 300 }
    )
  }, 30_000)
})
