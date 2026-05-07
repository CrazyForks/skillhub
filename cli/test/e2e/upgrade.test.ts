/**
 * Track B e2e — version upgrade against a real backend.
 *
 *   E2  publish slug, then publish slug a second time so the server
 *       auto-bumps the version. install (no --version) returns the latest
 *       version; install --version=<earlier> still resolves the older one.
 *
 * The server uses timestamp-based version strings (e.g. "20260506.173534")
 * — we don't assert on the exact format, only that the two publish calls
 * yield two distinct, non-empty version strings, and that latest = the
 * second one.
 */
import { mkdir, readFile } from 'node:fs/promises'
import { join } from 'node:path'
import { afterEach, describe, expect, test } from 'bun:test'
import { createTempHome } from '../helpers/temp-env'
import { runCli } from '../helpers/run-cli'
import {
  deleteRemote,
  getLiveRegistry,
  isLiveRegistryReachable,
  uniqueSlug
} from './helpers/live-registry'
import { makeSkillDir } from './helpers/skill-fixtures'

const live = getLiveRegistry()
const reachable = live ? await isLiveRegistryReachable(live) : false

describe.skipIf(!live || !reachable)('Track B e2e — version upgrade (live backend)', () => {
  const publishedSlugs: string[] = []

  afterEach(async () => {
    while (publishedSlugs.length > 0) {
      const slug = publishedSlugs.pop()!
      await deleteRemote(live!, slug)
    }
  })

  test('E2 second publish bumps version; install latest yields new version, --version pins to old', async () => {
    const env = await createTempHome()
    const slug = uniqueSlug('e2e-trackb-up')

    // --- publish v1 -------------------------------------------------------
    const dir1 = await makeSkillDir(slug, { bodyMarker: 'body-of-version-1' })
    const pub1 = await runCli(
      [
        'publish', dir1.dir,
        '--namespace', 'global',
        '--registry', live!.url,
        '--token', live!.token,
        '--json'
      ],
      { HOME: env.home, USERPROFILE: env.home }
    )
    expect(pub1.exitCode).toBe(0)
    const v1 = JSON.parse(pub1.stdout).version as string
    expect(typeof v1).toBe('string')
    expect(v1.length).toBeGreaterThan(0)
    publishedSlugs.push(slug)

    // Server auto-versions on a per-second granularity in some configs; sleep
    // briefly so the second publish lands in a distinct version bucket.
    await Bun.sleep(1100)

    // --- publish v2 (same slug, new body) ---------------------------------
    const dir2 = await makeSkillDir(slug, { bodyMarker: 'body-of-version-2' })
    const pub2 = await runCli(
      [
        'publish', dir2.dir,
        '--namespace', 'global',
        '--registry', live!.url,
        '--token', live!.token,
        '--json'
      ],
      { HOME: env.home, USERPROFILE: env.home }
    )
    expect(pub2.exitCode).toBe(0)
    const v2 = JSON.parse(pub2.stdout).version as string
    expect(v2).not.toBe(v1)

    // --- install latest → should be v2 ------------------------------------
    const latestDir = join(env.cwd, 'install-latest')
    await mkdir(latestDir, { recursive: true })
    const insLatest = await runCli(
      [
        'install', slug,
        '--namespace', 'global',
        '--dir', latestDir,
        '--registry', live!.url,
        '--token', live!.token,
        '--json'
      ],
      { HOME: env.home, USERPROFILE: env.home }
    )
    expect(insLatest.exitCode).toBe(0)

    const latestMeta = JSON.parse(
      await readFile(join(latestDir, slug, '.skillhub', 'metadata.json'), 'utf-8')
    ) as { version: string }
    expect(latestMeta.version).toBe(v2)

    const latestBody = await readFile(join(latestDir, slug, 'SKILL.md'), 'utf-8')
    expect(latestBody).toContain('body-of-version-2')

    // --- install --version=<v1> → should resolve and install v1 ----------
    const pinnedDir = join(env.cwd, 'install-pinned')
    await mkdir(pinnedDir, { recursive: true })
    const insPinned = await runCli(
      [
        'install', slug,
        '--namespace', 'global',
        '--version', v1,
        '--dir', pinnedDir,
        '--registry', live!.url,
        '--token', live!.token,
        '--json'
      ],
      { HOME: env.home, USERPROFILE: env.home }
    )
    expect(insPinned.exitCode).toBe(0)

    const pinnedMeta = JSON.parse(
      await readFile(join(pinnedDir, slug, '.skillhub', 'metadata.json'), 'utf-8')
    ) as { version: string }
    expect(pinnedMeta.version).toBe(v1)

    const pinnedBody = await readFile(join(pinnedDir, slug, 'SKILL.md'), 'utf-8')
    expect(pinnedBody).toContain('body-of-version-1')
  }, 30_000)
})
