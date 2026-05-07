/**
 * Build temporary skill-package directories for live-backend tests.
 *
 * Each fixture is a fresh tmp directory containing a SKILL.md whose
 * `name:` frontmatter matches the slug. The CLI reads the name to derive
 * the slug at publish time, so tests can publish, then look the skill up
 * by the same slug they passed to makeSkillDir().
 */
import { mkdtemp, writeFile } from 'node:fs/promises'
import { join } from 'node:path'
import { tmpdir } from 'node:os'

export interface SkillFixture {
  /** Temp directory containing SKILL.md (and any other authored files). */
  dir: string
  /** Skill slug — matches the `name:` field in SKILL.md. */
  slug: string
  /** SKILL.md body excluding frontmatter — useful for asserting on install content. */
  bodyMarker: string
}

/**
 * Compose a SKILL.md file with the given slug + a unique body marker. The
 * marker is asserted on the install side to confirm the same artifact made
 * the round-trip through the registry.
 */
export async function makeSkillDir(slug: string, options: {
  description?: string
  bodyMarker?: string
} = {}): Promise<SkillFixture> {
  const dir = await mkdtemp(join(tmpdir(), 'skillhub-e2e-skill-'))
  const description = options.description ?? `Track B e2e fixture for ${slug}.`
  const bodyMarker = options.bodyMarker ?? `marker-${slug}`
  const content = [
    '---',
    `name: ${slug}`,
    `description: ${description}`,
    '---',
    '',
    `# ${slug}`,
    '',
    `${bodyMarker}`,
    ''
  ].join('\n')
  await writeFile(join(dir, 'SKILL.md'), content)
  return { dir, slug, bodyMarker }
}
