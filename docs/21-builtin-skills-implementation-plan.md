# Built-in Skills Implementation Plan

> **Spec:** `docs/20-builtin-skills-design.md`

> **Goal:** Implement the MVP built-in skill initialization flow for `skillhub-hello`, using Java service resources and publishing to `@global` on startup.

## Scope

Implement only the MVP decisions from the design:

- Java service classpath resource source.
- `skillhub-hello` as the first built-in skill.
- Initialization enabled by default.
- Fixed target namespace `global`.
- `PUBLIC + PUBLISHED` publication.
- System publisher `builtin-skill-publisher`.
- Idempotent same-version skip.
- Same-version content drift warning and skip.
- No seed table.
- No distributed lock.
- No label binding.
- No frontend UI changes.

## Work Items

### 1. Add Built-In Skill Resource

- [ ] Create `server/skillhub-app/src/main/resources/builtin-skills/skillhub-hello/`.
- [ ] Add `SKILL.md` with `name`, `description`, and `version: 1.0.0`.
- [ ] Add a concise `README.md` explaining this is a bundled verification skill.
- [ ] Confirm the package passes the existing skill package policy.

Acceptance criteria:

- `skillhub-hello/SKILL.md` is at the package root.
- The package can be converted to `PackageEntry` values without path rewriting.

### 2. Add Configuration Binding

- [ ] Add `BuiltinSkillProperties` under `com.iflytek.skillhub.bootstrap`.
- [ ] Bind prefix `skillhub.builtin-skills`.
- [ ] Add `enabled=true` default.
- [ ] Register the configuration properties if current application setup requires explicit registration.
- [ ] Add a properties binding test.

Acceptance criteria:

- `SKILLHUB_BUILTIN_SKILLS_ENABLED=false` disables initialization.
- Default behavior is enabled.

### 3. Implement Package Loader

- [ ] Add `BuiltinSkillPackageLoader`.
- [ ] Discover built-in packages with a jar-safe classpath scan.
- [ ] Use `PathMatchingResourcePatternResolver` or an equivalent Spring resource resolver.
- [ ] Discover package roots by matching `classpath*:builtin-skills/*/SKILL.md` instead of enumerating `classpath:builtin-skills/` as a filesystem directory.
- [ ] Require root-level `SKILL.md` for each skill directory.
- [ ] Normalize relative paths with `/` separators.
- [ ] Reject unsafe paths if encountered.
- [ ] Build `PackageEntry` records with content bytes, size, and content type.
- [ ] Sort entries deterministically by path.
- [ ] Extract shared content-type resolution instead of adding a third private mapping copy.
- [ ] Reuse that shared content-type helper from upload archive extraction and the built-in loader where practical.

Acceptance criteria:

- Loader returns one package for `skillhub-hello`.
- Entry order is stable.
- Missing `SKILL.md` is reported without crashing the whole initializer.

### 4. Implement Fingerprint Logic

- [ ] Add package fingerprint calculation from `PackageEntry` list.
- [ ] Calculate per-file SHA-256.
- [ ] Calculate aggregate SHA-256 from sorted `path + sha256` pairs.
- [ ] Add existing-version fingerprint calculation from `SkillFile` rows.
- [ ] Keep this logic package-private or in a small helper, covered by unit tests.

Acceptance criteria:

- Same entries in different input order produce the same fingerprint.
- Changing file content changes the fingerprint.
- Changing file path changes the fingerprint.

### 5. Implement System Publisher Setup

- [ ] In initializer flow, ensure `UserAccount("builtin-skill-publisher")` exists.
- [ ] Ensure `@global` namespace exists or log error and skip built-in publish.
- [ ] Ensure the publisher has `NamespaceRole.OWNER` membership in `@global`.
- [ ] Do not create local credentials.
- [ ] Do not persist a real platform role binding unless existing code requires it.
- [ ] Keep system publisher setup transactionally separate from per-package publish attempts.

Acceptance criteria:

- Fresh database startup creates the system publisher and membership.
- Existing publisher and membership are reused idempotently.

### 6. Implement BuiltinSkillInitializer

- [ ] Add `BuiltinSkillInitializer` as an `ApplicationRunner`.
- [ ] Do not annotate `run(...)` with a single outer `@Transactional`.
- [ ] Keep exception handling outside the transactional publish call so rollback-only state cannot escape and fail application startup.
- [ ] If helper methods need transactions, split them into separate bean methods or services so Spring transaction proxies apply correctly.
- [ ] Exit early when `enabled=false`.
- [ ] Load built-in packages.
- [ ] Parse each package's `SKILL.md` metadata to resolve slug and version.
- [ ] For each package, check existing `@global/{slug}`, owner, and version state.
- [ ] Only manage skills owned by `builtin-skill-publisher`.
- [ ] If the same slug has a published version owned by another user, log a warning and skip the built-in package.
- [ ] Publish when no same version exists.
- [ ] Skip when same version is `PUBLISHED` and fingerprint matches.
- [ ] Warn and skip when same version is `PUBLISHED` and fingerprint differs.
- [ ] Warn and skip when same version exists but is not `PUBLISHED`.
- [ ] Catch expected publish conflicts from concurrent startup and re-check existing version.
- [ ] Catch unexpected exceptions per package and continue.

Acceptance criteria:

- Repeated startup of the same artifact publishes only once.
- Rebuilding the app with modified `skillhub-hello` content but unchanged version does not overwrite the existing version.
- A new `version` in `SKILL.md` publishes a new version.
- Any single package failure does not abort application startup.
- A user-owned `@global/skillhub-hello` is not overwritten or replaced by the built-in initializer.
- A caught publish failure cannot mark an outer startup transaction rollback-only.

### 7. Reuse Publish Pipeline

- [ ] Call `SkillPublishService.publishFromEntries(...)`.
- [ ] Use namespace `global`.
- [ ] Use publisher `builtin-skill-publisher`.
- [ ] Use `SkillVisibility.PUBLIC`.
- [ ] Pass `Set.of("SUPER_ADMIN")`.
- [ ] Pass `confirmWarnings=false`.
- [ ] Treat any package validation or pre-publish warning as a built-in package quality issue.
- [ ] If warnings are present, log an error and skip the package instead of confirming them.

Acceptance criteria:

- Published version is `PUBLISHED`.
- `latestVersionId` points to the built-in version.
- `SkillFile` rows and bundle object are created by the existing publish service.
- `SkillPublishedEvent` is emitted through the existing path.

### 8. Add Tests

- [ ] Add loader tests.
- [ ] Add properties binding test.
- [ ] Add initializer tests for disabled mode.
- [ ] Add initializer tests for first publish.
- [ ] Add initializer tests for same-version same-fingerprint skip.
- [ ] Add initializer tests for same-version changed-fingerprint skip.
- [ ] Add initializer tests for non-published same version skip.
- [ ] Add initializer tests for a newer built-in version publishing when an older built-in version already exists.
- [ ] Add initializer tests for same slug owned by another published skill owner warning and skipping.
- [ ] Add initializer tests for publish exception swallow-and-log behavior.
- [ ] Add system publisher and membership idempotency tests.
- [ ] Add tests that publish failures do not occur inside a single outer initializer transaction.
- [ ] Add a test that loads the real `skillhub-hello` resource and validates it with `SkillPackageValidator`.

Acceptance criteria:

- Tests cover all MVP idempotency branches.
- Tests do not require real object storage.
- Tests cover jar-safe resource discovery behavior as closely as practical without relying on filesystem-only assumptions.

### 9. Update Documentation

- [ ] Update `README.md` with `skillhub-hello` search/install verification commands.
- [ ] Update `docs/openclaw-integration.md`.
- [ ] Update `docs/openclaw-integration-en.md`.
- [ ] Mention `SKILLHUB_BUILTIN_SKILLS_ENABLED=false` for operators who want to disable built-in initialization.

Acceptance criteria:

- Users can find a documented command to search and install `skillhub-hello`.
- Docs do not mention AgentGuard as the MVP built-in skill.

### 10. Validation

- [ ] Run `make test-backend-app`.
- [ ] Run `make staging` because this feature depends on packaged runtime resources and startup behavior.
- [ ] Manually start local app and verify `skillhub-hello` appears in search.
- [ ] Verify reinstall/restart does not create duplicate published versions.

Acceptance criteria:

- Backend tests pass.
- Packaged/staging startup can discover `skillhub-hello` from classpath resources.
- Manual search returns `skillhub-hello`.
- Repeated startup is idempotent.

### 11. Code Review

- [ ] Perform a focused backend code review after tests and validation pass.
- [ ] Review transaction boundaries around `BuiltinSkillInitializer`.
- [ ] Verify `run(...)` is not wrapped in a single outer transaction.
- [ ] Verify publish failures cannot mark startup rollback-only or abort application startup.
- [ ] Verify classpath scanning is jar-safe and does not rely on filesystem-only directory enumeration.
- [ ] Verify the initializer only manages skills owned by `builtin-skill-publisher`.
- [ ] Verify same-version published skills are never overwritten.
- [ ] Verify warning handling uses `confirmWarnings=false` and skips invalid built-in packages.
- [ ] Verify no label, seed table, distributed lock, frontend UI, or controller/API drift was introduced.
- [ ] Verify tests cover all MVP idempotency and failure branches.

Acceptance criteria:

- Review findings are resolved or explicitly accepted before PR.
- No unresolved blocker remains in startup reliability, idempotency, ownership, or resource packaging.

## Implementation Order

1. Add `skillhub-hello` resource.
2. Add properties binding.
3. Extract shared content-type helper.
4. Add jar-safe loader and fingerprint helper.
5. Add initializer with separated transaction boundaries, publisher setup, owner checks, and idempotency checks.
6. Add tests.
7. Update README and OpenClaw docs.
8. Run backend and staging validation.
9. Perform focused code review and resolve findings.

## Files Expected To Change

Backend:

- `server/skillhub-app/src/main/java/com/iflytek/skillhub/bootstrap/BuiltinSkillProperties.java`
- `server/skillhub-app/src/main/java/com/iflytek/skillhub/bootstrap/BuiltinSkillPackageLoader.java`
- `server/skillhub-app/src/main/java/com/iflytek/skillhub/bootstrap/BuiltinSkillInitializer.java`
- `server/skillhub-app/src/main/java/com/iflytek/skillhub/controller/support/SkillPackageContentTypeResolver.java`
- `server/skillhub-app/src/main/resources/builtin-skills/skillhub-hello/SKILL.md`
- `server/skillhub-app/src/main/resources/builtin-skills/skillhub-hello/README.md`

Tests:

- `server/skillhub-app/src/test/java/com/iflytek/skillhub/bootstrap/BuiltinSkillPropertiesBindingTest.java`
- `server/skillhub-app/src/test/java/com/iflytek/skillhub/bootstrap/BuiltinSkillPackageLoaderTest.java`
- `server/skillhub-app/src/test/java/com/iflytek/skillhub/bootstrap/BuiltinSkillInitializerTest.java`
- `server/skillhub-app/src/test/java/com/iflytek/skillhub/controller/support/SkillPackageContentTypeResolverTest.java`

Docs:

- `README.md`
- `docs/openclaw-integration.md`
- `docs/openclaw-integration-en.md`

No generated OpenAPI files are expected to change because this implementation does not add or modify controllers.

## Risks And Mitigations

| Risk | Mitigation |
|------|------------|
| Classpath resource scanning behaves differently from filesystem scanning | Test loader against test resources and verify packaged startup |
| Same-version conflict during multi-instance startup | Catch conflict, re-read existing version, skip if published |
| Publish service writes object storage before a later failure | Keep failure non-fatal and rely on existing publish behavior; avoid custom storage writes |
| Built-in package accidentally changes without version bump | Fingerprint mismatch warning and skip |
| System publisher missing foreign-key requirements | Ensure `UserAccount` and `@global` membership before publish |
| Publish failure marks an outer transaction rollback-only | Do not wrap initializer `run(...)` in one transaction; catch outside publish transactions |
| User already owns the target slug | Only manage built-in-publisher-owned skills; warn and skip other owners |
| Built-in package warnings are silently accepted | Use `confirmWarnings=false`; treat warnings as package quality failures |

## Out Of Scope Follow-Ups

- Labels such as `official` or `example`.
- Homepage or landing recommended sections.
- Seed state table.
- Distributed lock.
- External `file:` locations.
- Zip package support.
- AgentGuard as a built-in official skill.
