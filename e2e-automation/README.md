# SkillHub E2E 自动化测试流水线

> 在 SkillHub 项目内部使用的"代码推送 → 自动需求分析 → 用例生成 → Playwright 脚本生成 → 执行"全流程。
> 使用的核心知识资产是 [`test-case-design-skill`](../test-case-design-skill/),通过本地路径或安装后的方式加载。

## 与项目现状的对接关系

| 项目现有 | 本流水线对接方式 |
|---------|----------------|
| `docs/prds/*.md` (PRD 文档) | 作为需求分析阶段的输入源 |
| `docs/skillhub/`、`docs/superpowers/specs/*.md` (设计文档) | 同上,补充背景 |
| `web/e2e/auto-generated/` (流水线产出) | 生成的脚本落到此目录,按 module 分子目录(见下方"产出位置约定") |
| `web/e2e/*.spec.ts` (手写 e2e 用例) | 手写用例保留在 `web/e2e/` 顶层,不被流水线覆盖 |
| `web/e2e/helpers/auth-fixtures.ts` | 复用 `setEnglishLocale`、`registerSession` 等 helper |
| `web/playwright.config.ts` | 不修改,直接复用 baseURL=localhost:3000 等配置 |
| `cli/` (TypeScript + bun) | 流水线技术栈与 cli 一致,可共享构建工具 |
| `.github/workflows/` | 集成 GitHub Actions 触发流水线 |

## 集成位置

把 `e2e-automation/` 作为独立子目录放到项目根:

```
skillhub/
├── cli/
├── server/
├── web/
│   └── e2e/                    # 现有手写 e2e 测试
├── docs/
├── e2e-automation/             # ← 本流水线
│   ├── config/
│   ├── src/
│   ├── package.json
│   └── README.md
└── .github/workflows/
    └── e2e-auto.yml            # ← 触发流水线的工作流
```

## 整体流水线

```
触发: PR / Push / 新增 PRD 文档
   ↓
[Stage 1] 需求分析  ← 读取 docs/prds/*.md + git diff
   ↓ outputs/requirements.json
[Stage 2] 测试点拆分
   ↓ outputs/test-points.json
[Stage 3] 用例生成  ← 加载 test-case-design Skill
   ↓ outputs/test-cases.json
[Stage 4] Playwright 脚本生成  ← 复用 web/e2e/helpers
   ↓ web/e2e/auto-generated/*.spec.ts
[Stage 5] Playwright 执行  ← 走现有 playwright.config.ts
   ↓ 报告 + PR 评论
```

## 快速开始

```bash
# 1. 安装依赖
cd e2e-automation
bun install

# 2. 配置 LLM
export ANTHROPIC_API_KEY=sk-...
export SKILLHUB_BASE_URL=http://localhost:3000

# 3. 跑一次完整流水线 (输入: 某个 PRD)
bun run pipeline -- --requirement ../docs/prds/skill-file-browser-sidebar-v1.0-prd.md

# 4. 只跑某一阶段
bun run analyze -- --input ../docs/prds/skill-file-browser-sidebar-v1.0-prd.md
bun run generate-cases -- --input outputs/test-points.json
bun run generate-scripts -- --input outputs/test-cases.json

# 5. 查看生成的脚本并执行
cd ../web
pnpm exec playwright test e2e/auto-generated/
```

## 与 test-case-design Skill 的两种集成模式

### 模式 A: 本地路径加载 (开发/CI)

直接读取仓库内 `../test-case-design-skill/` 目录,适合本地开发与 CI:

```ts
// config/default.yaml
skill:
  source: local
  path: ../test-case-design-skill
```

### 模式 B: 从 SkillHub Registry 加载 (推荐生产)

先用 CLI 安装,再加载到流水线:

```bash
skillhub install @your-team/test-case-design
```

```ts
// config/default.yaml
skill:
  source: installed
  # 自动从 .agent/skills/test-case-design 或 ~/.agent/skills/test-case-design 找
```

模式 B 的好处: Skill 版本可被正式管理,跨项目共享,所有团队用同一份方法论。

## 产出位置约定

流水线 Stage 4 生成的 Playwright 脚本统一落到 `web/e2e/auto-generated/`:

```
web/e2e/auto-generated/
  <module>/
    TC-<MODULE>-NNN.spec.ts       # 一个用例一个文件
  pages/
    <module>/
      <name>.page.ts              # Page Object (如需要)
```

已有产出示例:

| Module | 产出目录 | 说明 |
|--------|---------|------|
| cli-auth | `web/e2e/auto-generated/cli-auth/` | 10 个 spec,覆盖 CLI OAuth 跳转流(浏览器端) |

### 重要边界

本流水线**只产出以浏览器为被测主语的 Playwright spec**(page.goto / getByRole / locator)。

以下测试类型不由本流水线产出,有各自独立的维护方式:

| 测试类型 | 位置 | 说明 |
|---------|------|------|
| CLI 子进程 vs 真实后端 | `cli/test/e2e/` | Bun test, 真后端, `SKILLHUB_E2E_*` 跳过条件 |
| 跨栈 CLI ↔ Web | `web/e2e/cross-stack-*.spec.ts` | Playwright + execFileSync 混合驱动 |
| CLI 单命令集成测试 | `cli/test/integration/` | Bun test + fake-registry |

完整的测试位置决策树见 `cli/test/e2e/README.md`。

如果未来流水线扩展支持 CLI 子进程驱动的 spec 生成,产出应落到 `cli/test/e2e/` 而非本目录。

## 文件清单

详见各文件内的注释和 `src/pipeline.ts` 的入口编排逻辑。
