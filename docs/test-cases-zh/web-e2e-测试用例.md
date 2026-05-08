# SkillHub Web E2E 测试用例

## 1. 文档说明

- 来源目录：`web/e2e`
- 来源范围：26 个 `.spec.ts` 文件，已排除 `helpers/` 夹具文件
- 用例总数：126
- 整理方式：按业务分类组织，每个场景保留原始 Playwright `describe/test` 名称，便于回溯到自动化脚本
- 适用方式：可用于测试评审、回归范围梳理、补充手工用例和生成测试报告

## 2. 分类总览

1. 认证与权限
2. 搜索与发现
3. 工作台与设置
4. 命名空间与治理
5. 公共页面与导航
6. 发布流程

## 3. 认证与权限

### 3.1 认证入口

- 来源：`web/e2e/auth-entry.spec.ts`
- 公共前置条件：未登录访问登录页
- 基本步骤：
  1. 打开登录页
  2. 检查必填校验
  3. 点击注册链接并观察 `returnTo` 参数保留情况

| 用例ID | 核心检查点 |
|---|---|
| AUTH_ENTRY_001 | validates required fields and preserves returnTo on register link |

### 3.2 CLI 认证入口

- 来源：`web/e2e/cli-auth.spec.ts`
- 公共前置条件：通过浏览器访问 CLI 授权回调页或认证页
- 基本步骤：
  1. 构造缺失重定向参数的授权请求
  2. 打开页面
  3. 检查错误提示

| 用例ID | 核心检查点 |
|---|---|
| CLI_AUTH_001 | shows error for missing redirect params |

### 3.3 注册用户名校验

- 来源：`web/e2e/register-login-validation.spec.ts`
- 公共前置条件：访问 `/register`，页面语言设置为 English
- 基本步骤：
  1. 在用户名输入框输入指定数据或留空
  2. 触发失焦或点击注册
  3. 检查页面校验文案

| 用例ID | 核心检查点 |
|---|---|
| TC_UN_008 | shows required error when username is empty |
| TC_UN_001 | accepts valid username with minimum 3 characters |
| TC_UN_006 | shows length error for 2-character username |
| TC_UN_009 | shows error for username with special characters like @ |
| TC_UN_010 | shows error for username containing Chinese characters |

### 3.4 注册邮箱校验

- 来源：`web/e2e/register-login-validation.spec.ts`
- 公共前置条件：访问 `/register`，邮箱输入框可见
- 基本步骤：
  1. 在邮箱输入框输入指定数据或清空
  2. 触发失焦
  3. 检查邮箱格式校验是否符合预期

| 用例ID | 核心检查点 |
|---|---|
| TC_EM_007 | allows empty email (email is optional) |
| TC_EM_008 | shows error for email missing @ symbol |
| TC_EM_009 | shows error for email missing domain after @ |

### 3.5 注册密码校验

- 来源：`web/e2e/register-login-validation.spec.ts`
- 公共前置条件：访问 `/register`
- 基本步骤：
  1. 在密码框输入指定密码或留空
  2. 触发失焦或提交表单
  3. 检查长度、复杂度和必填校验

| 用例ID | 核心检查点 |
|---|---|
| TC_PW_013 | shows required error when password is empty |
| TC_PW_007 | shows length error for 7-character password |
| TC_PW_008 | shows complexity error for password with only 2 character types |
| TC_PW_001 | accepts valid password with 3 character types and minimum length |

### 3.6 注册流程

- 来源：`web/e2e/register-login-validation.spec.ts`
- 公共前置条件：可访问注册页，后端真实 API 可用
- 基本步骤：
  1. 打开 `/register`
  2. 输入用户名、邮箱、密码等测试数据
  3. 提交注册并检查跳转或错误提示

| 用例ID | 核心检查点 |
|---|---|
| TC_REG_001 | registers successfully with valid username, email and password |
| TC_REG_002 | registers successfully without email (email is optional) |
| TC_REG_003 | shows error when registering with existing username |
| TC_REG_005 | shows validation errors when submitting empty required fields |

### 3.7 登录流程

- 来源：`web/e2e/register-login-validation.spec.ts`
- 公共前置条件：可访问登录页，存在已注册或临时创建的测试账号
- 基本步骤：
  1. 打开 `/login`
  2. 输入指定账号和密码
  3. 提交表单并校验错误提示或安全处理结果

| 用例ID | 核心检查点 |
|---|---|
| TC_REG_006 | shows required field errors when submitting empty login form |
| TC_REG_007 | shows error for wrong password on existing account |
| TC_REG_008 | shows error for non-existent username login attempt |
| TC_REG_010 | safely handles SQL injection input in username field |
| TC_REG_011 | safely handles XSS payload in username field without executing script |

### 3.8 注册/登录 UI 交互

- 来源：`web/e2e/register-login-validation.spec.ts`
- 公共前置条件：访问登录或注册页面
- 基本步骤：
  1. 操作密码显示开关、回车提交、注册链接
  2. 观察字段展示、提交行为和 URL 参数

| 用例ID | 核心检查点 |
|---|---|
| TC_UI_003 | password visibility toggle switches between masked and plain text |
| TC_UI_005 | pressing Enter in the last input field submits the login form |
| UI_RETURNTO_001 | preserves returnTo param when navigating from register link on login page |

### 3.9 受保护路由重定向

- 来源：`web/e2e/protected-routes.spec.ts`
- 公共前置条件：用户未登录
- 基本步骤：
  1. 直接访问 dashboard 或 admin 相关路径
  2. 观察跳转位置

| 用例ID | 核心检查点 |
|---|---|
| PROTECTED_ROUTE_001 | redirects anonymous users from dashboard and admin routes |

### 3.10 路由守卫

- 来源：`web/e2e/route-guard.spec.ts`
- 公共前置条件：分别准备匿名用户与已登录用户
- 基本步骤：
  1. 匿名访问受保护路由
  2. 已登录后重新访问 dashboard
  3. 对比路由守卫行为

| 用例ID | 核心检查点 |
|---|---|
| ROUTE_GUARD_001 | redirects anonymous users to login for protected routes |
| ROUTE_GUARD_002 | allows authenticated users to open dashboard |

### 3.11 角色访问控制

- 来源：`web/e2e/role-access-control.spec.ts`
- 公共前置条件：使用普通用户登录
- 基本步骤：
  1. 访问 review center、promotions、reports、admin 页面
  2. 检查是否被拒绝或跳转

| 用例ID | 核心检查点 |
|---|---|
| ROLE_AC_001 | redirects regular user from review center |
| ROLE_AC_002 | redirects regular user from promotions and reports pages |
| ROLE_AC_003 | redirects regular user from admin pages |

## 4. 搜索与发现

### 4.1 搜索流程基础行为

- 来源：`web/e2e/search-flow.spec.ts`
- 公共前置条件：访问搜索页，页面语言设置为 English
- 基本步骤：
  1. 打开搜索页
  2. 检查搜索控件和 URL 参数
  3. 切换 `Starred only` 验证匿名态行为

| 用例ID | 核心检查点 |
|---|---|
| SEARCH_FLOW_001 | renders search controls and keeps query state in URL |
| SEARCH_FLOW_002 | redirects anonymous user to login when enabling starred filter |

### 4.2 搜索输入

- 来源：`web/e2e/search-page-full.spec.ts`
- 公共前置条件：访问 `/search`
- 基本步骤：
  1. 输入关键词、空值、特殊字符或中英文内容
  2. 点击搜索或按 Enter
  3. 检查结果页、URL 与稳定性

| 用例ID | 核心检查点 |
|---|---|
| TC_SEARCH_INPUT_001 | searches with a single keyword and shows results |
| TC_SEARCH_INPUT_003 | empty search shows default skill list |
| TC_SEARCH_INPUT_004 | pressing Enter in search box triggers search |
| TC_SEARCH_INPUT_009 | supports Chinese keyword search without error |
| TC_SEARCH_INPUT_010 | supports English keyword search |
| TC_SEARCH_INPUT_007 | handles special characters in search without crashing |
| TC_SEARCH_INPUT_011 | trims leading and trailing spaces from search query |

### 4.3 搜索排序与筛选

- 来源：`web/e2e/search-page-full.spec.ts`
- 公共前置条件：访问带查询参数的搜索结果页
- 基本步骤：
  1. 在 `Relevance`、`Downloads`、`Newest` 间切换
  2. 操作 `Starred only`
  3. 检查 URL、关键词保留、页码重置和登录重定向

| 用例ID | 核心检查点 |
|---|---|
| TC_SEARCH_SORT_001 | relevance sort tab is selected by default |
| TC_SEARCH_SORT_004 | clicking Downloads tab updates sort in URL |
| TC_SEARCH_SORT_005 | clicking Newest tab updates sort in URL |
| TC_SEARCH_SORT_006 | switching sort tab preserves the search keyword |
| TC_SEARCH_SORT_007 | switching sort tab resets page to 0 |
| TC_SEARCH_SORT_012 | URL contains sort parameter after switching tabs |
| SEARCH_STARRED_001 | starred only filter redirects anonymous user to login |
| SEARCH_STARRED_002 | starred only filter stays on search page for authenticated user |

### 4.4 搜索结果数量展示

- 来源：`web/e2e/search-page-full.spec.ts`
- 公共前置条件：访问搜索页并具备可返回结果的测试数据
- 基本步骤：
  1. 打开默认列表或执行搜索
  2. 观察技能数量展示
  3. 切换排序或构造无结果数据再比对数量

| 用例ID | 核心检查点 |
|---|---|
| TC_SEARCH_COUNT_001 | skill count indicator is visible on search page |
| TC_SEARCH_COUNT_007 | skill count updates after performing a search |
| TC_SEARCH_COUNT_009 | shows 0 skills count when search returns no results |
| TC_SEARCH_COUNT_008 | skill count remains the same after switching sort tab |

### 4.5 搜索结果展示

- 来源：`web/e2e/search-page-full.spec.ts`
- 公共前置条件：访问搜索结果页
- 基本步骤：
  1. 执行有结果和无结果搜索
  2. 检查卡片、空态、错误态
  3. 切换不同排序方式并观察结果呈现

| 用例ID | 核心检查点 |
|---|---|
| TC_SEARCH_RESULT_001 | shows skill cards when search returns results |
| TC_SEARCH_RESULT_002 | shows empty state message when no results found |
| TC_SEARCH_RESULT_006 | page renders without error during and after search |
| TC_SEARCH_RESULT_008 | number of displayed cards matches the count indicator |
| TC_SEARCH_RESULT_009 | results are sorted by downloads when Downloads tab is selected |
| TC_SEARCH_RESULT_010 | results are sorted by newest when Newest tab is selected |

### 4.6 搜索分页

- 来源：`web/e2e/search-page-full.spec.ts`
- 公共前置条件：搜索结果数足够触发分页
- 基本步骤：
  1. 进入有分页的搜索结果页
  2. 切换页码
  3. 检查 URL 与查询条件保持情况

| 用例ID | 核心检查点 |
|---|---|
| TC_SEARCH_PAGE_011 | URL contains page parameter |
| TC_SEARCH_PAGE_012 | switching page preserves search keyword and sort |
| TC_SEARCH_PAGE_007 | previous page button is disabled on first page |

### 4.7 搜索安全性

- 来源：`web/e2e/search-page-full.spec.ts`
- 公共前置条件：访问搜索页
- 基本步骤：
  1. 输入 XSS、SQL 注入、篡改 URL 参数等异常数据
  2. 提交搜索或直接访问 URL
  3. 检查页面是否稳定、脚本是否执行、参数是否被安全处理

| 用例ID | 核心检查点 |
|---|---|
| TC_SEARCH_SEC_001 | XSS payload in search box is not executed |
| TC_SEARCH_SEC_002 | SQL injection payload in search box is handled safely |
| TC_SEARCH_SEC_003 | tampered URL parameters are handled gracefully |

### 4.8 搜索卡片展示

- 来源：`web/e2e/search-card-interaction.spec.ts`
- 公共前置条件：执行搜索并返回技能卡片列表
- 基本步骤：
  1. 打开搜索结果页
  2. 观察卡片是否及时渲染以及字段展示
  3. 针对空结果、长描述、大结果集等场景检查界面表现

| 用例ID | 核心检查点 |
|---|---|
| TC_SEARCH_INTERACT_001 | cards appear immediately after search |
| TC_SEARCH_INTERACT_005 | each card shows name, description, and version |
| TC_SEARCH_INTERACT_039 | version number is displayed in v1.2.3 format |
| TC_SEARCH_INTERACT_038 | long descriptions are truncated with ellipsis |
| TC_SEARCH_INTERACT_031 | no results shows empty state instead of cards |
| TC_SEARCH_INTERACT_035 | large result sets show pagination controls |

### 4.9 搜索卡片内容一致性

- 来源：`web/e2e/search-card-interaction.spec.ts`
- 公共前置条件：搜索结果存在技能卡片和数量信息
- 基本步骤：
  1. 执行搜索
  2. 对比卡片数量、数量指示器和下载量格式

| 用例ID | 核心检查点 |
|---|---|
| TC_SEARCH_INTERACT_003 | displayed card count is consistent with skill count indicator |
| TC_SEARCH_INTERACT_040 | download counts are formatted correctly (numbers or K/M) |

### 4.10 搜索卡片导航

- 来源：`web/e2e/search-card-interaction.spec.ts`
- 公共前置条件：搜索结果中至少有一张技能卡片
- 基本步骤：
  1. 点击卡片或使用 Ctrl+Click
  2. 进入技能详情页
  3. 检查详情页内容是否与原卡片一致

| 用例ID | 核心检查点 |
|---|---|
| TC_SEARCH_INTERACT_007 | clicking a skill card navigates to the skill detail page |
| TC_SEARCH_INTERACT_008 | skill detail page matches the card that was clicked |
| TC_SEARCH_INTERACT_009 | Ctrl+click on card opens skill detail in new tab |

### 4.11 搜索卡片排序交互

- 来源：`web/e2e/search-card-interaction.spec.ts`
- 公共前置条件：搜索结果页可切换排序并重新搜索
- 基本步骤：
  1. 切换排序标签
  2. 使用新关键词重新搜索
  3. 观察卡片列表刷新和页码重置

| 用例ID | 核心检查点 |
|---|---|
| TC_SEARCH_INTERACT_021 | switching sort tab re-renders card list |
| TC_SEARCH_INTERACT_026 | re-searching with new keyword replaces card list |
| TC_SEARCH_INTERACT_027 | re-searching resets page number to 0 |

### 4.12 搜索卡片分页交互

- 来源：`web/e2e/search-card-interaction.spec.ts`
- 公共前置条件：结果集足够分页
- 基本步骤：
  1. 切换到下一页
  2. 对比卡片内容是否变化
  3. 检查结果区域是否自动回到顶部

| 用例ID | 核心检查点 |
|---|---|
| TC_SEARCH_INTERACT_023 | switching to next page shows different cards |
| TC_SEARCH_INTERACT_025 | switching page scrolls back to top of results |

### 4.13 搜索卡片加载态

- 来源：`web/e2e/search-card-interaction.spec.ts`
- 公共前置条件：执行搜索操作
- 基本步骤：
  1. 发起搜索
  2. 观察骨架屏和真实卡片切换

| 用例ID | 核心检查点 |
|---|---|
| TC_SEARCH_INTERACT_030 | skeleton screen disappears and real cards appear after load |

### 4.14 搜索卡片响应式布局

- 来源：`web/e2e/search-card-interaction.spec.ts`
- 公共前置条件：准备桌面、平板、移动端视口
- 基本步骤：
  1. 在不同视口尺寸打开搜索结果页
  2. 检查卡片列数与布局变化
  3. 在移动端执行点击操作验证跳转

| 用例ID | 核心检查点 |
|---|---|
| TC_SEARCH_INTERACT_042 | desktop viewport shows 3-column card grid |
| TC_SEARCH_INTERACT_044 | mobile viewport shows single-column card layout |
| TC_SEARCH_INTERACT_043 | tablet viewport shows 2-column card layout |
| TC_SEARCH_INTERACT_045 | card layout adjusts when browser window is resized |
| TC_SEARCH_INTERACT_046 | mobile touch on card navigates to skill detail |

### 4.15 搜索卡片键盘可访问性

- 来源：`web/e2e/search-card-interaction.spec.ts`
- 公共前置条件：搜索结果页有可聚焦卡片
- 基本步骤：
  1. 使用 Tab 在卡片间移动焦点
  2. 使用 Enter 打开详情
  3. 观察焦点可见性

| 用例ID | 核心检查点 |
|---|---|
| TC_SEARCH_INTERACT_049 | Tab key can navigate between skill cards |
| TC_SEARCH_INTERACT_050 | pressing Enter on a focused card opens the skill detail |
| TC_SEARCH_INTERACT_051 | focused card has a visible focus indicator |

### 4.16 搜索卡片异常处理与缓存返回

- 来源：`web/e2e/search-card-interaction.spec.ts`
- 公共前置条件：执行搜索并支持返回搜索页
- 基本步骤：
  1. 验证单结果场景下的卡片布局
  2. 进入详情页后返回
  3. 检查搜索结果缓存恢复速度

| 用例ID | 核心检查点 |
|---|---|
| TC_SEARCH_INTERACT_033 | single search result displays card layout correctly |
| TC_SEARCH_INTERACT_060 | returning to search page shows cached results quickly |

### 4.17 技能详情浏览异常场景

- 来源：`web/e2e/skill-detail-browse.spec.ts`
- 公共前置条件：直接访问技能详情相关路由
- 基本步骤：
  1. 访问不存在的 namespace 路由
  2. 访问不存在的 skill 详情路由
  3. 检查 404 或 not found 展示

| 用例ID | 核心检查点 |
|---|---|
| SKILL_DETAIL_001 | shows not found for unknown namespace |
| SKILL_DETAIL_002 | shows not found for unknown skill detail route |

## 5. 工作台与设置

### 5.1 Dashboard 个人模块

- 来源：`web/e2e/dashboard-personal-modules.spec.ts`
- 公共前置条件：用户已登录并进入 dashboard
- 基本步骤：
  1. 打开个人中心模块入口
  2. 分别进入 stars 与 notifications 页面
  3. 校验跳转结果

| 用例ID | 核心检查点 |
|---|---|
| DASH_PERSONAL_001 | opens stars page |
| DASH_PERSONAL_002 | opens notifications page |

### 5.2 Dashboard 路由

- 来源：`web/e2e/dashboard-routes.spec.ts`
- 公共前置条件：用户已登录
- 基本步骤：
  1. 访问主要 dashboard 页面
  2. 访问 governance 和 namespace management 页面
  3. 确认各页面可正常打开

| 用例ID | 核心检查点 |
|---|---|
| DASH_ROUTE_001 | opens major dashboard pages |
| DASH_ROUTE_002 | opens governance and namespace management pages |

### 5.3 Dashboard 外壳与摘要

- 来源：`web/e2e/dashboard-shell.spec.ts`
- 公共前置条件：用户已登录进入 dashboard 首屏
- 基本步骤：
  1. 打开 dashboard
  2. 检查账户摘要和快捷入口

| 用例ID | 核心检查点 |
|---|---|
| DASH_SHELL_001 | renders account summary and quick links |

### 5.4 我的技能数据展示

- 来源：`web/e2e/my-skills-data.spec.ts`
- 公共前置条件：通过请求辅助工具已发布测试技能，用户已登录
- 基本步骤：
  1. 打开 dashboard 我的技能列表
  2. 检查请求创建的技能是否出现

| 用例ID | 核心检查点 |
|---|---|
| MY_SKILL_DATA_001 | shows request-published skill in dashboard list |

### 5.5 我的技能导航

- 来源：`web/e2e/my-skills-navigation.spec.ts`
- 公共前置条件：dashboard 列表中已存在种子技能
- 基本步骤：
  1. 从我的技能列表进入技能详情
  2. 再返回列表页
  3. 验证导航链路完整

| 用例ID | 核心检查点 |
|---|---|
| MY_SKILL_NAV_001 | opens seeded skill detail from dashboard list and returns back |

### 5.6 我的命名空间数据展示

- 来源：`web/e2e/my-namespaces-data.spec.ts`
- 公共前置条件：测试助手已创建命名空间，用户已登录
- 基本步骤：
  1. 打开我的命名空间页
  2. 检查刚创建的命名空间是否展示

| 用例ID | 核心检查点 |
|---|---|
| MY_NS_DATA_001 | shows namespace created by request helper |

### 5.7 Workspace 页面

- 来源：`web/e2e/workspace-pages.spec.ts`
- 公共前置条件：用户已登录
- 基本步骤：
  1. 打开 workspace 区域
  2. 进入 my skills 和 my namespaces 页面
  3. 校验页面可正常访问

| 用例ID | 核心检查点 |
|---|---|
| WORKSPACE_001 | opens my skills and my namespaces pages |

### 5.8 Token 管理页

- 来源：`web/e2e/tokens.spec.ts`
- 公共前置条件：用户已登录
- 基本步骤：
  1. 打开 token 管理页
  2. 检查页面主内容与创建入口是否可见

| 用例ID | 核心检查点 |
|---|---|
| TOKEN_001 | renders token management page and create action |

### 5.9 设置页面

- 来源：`web/e2e/settings-pages.spec.ts`
- 公共前置条件：用户已登录进入设置中心
- 基本步骤：
  1. 进入 profile settings
  2. 尝试修改密码但缺少 current password
  3. 进入 notification settings

| 用例ID | 核心检查点 |
|---|---|
| SETTINGS_PAGE_001 | opens profile settings page |
| SETTINGS_PAGE_002 | shows validation when current password is missing |
| SETTINGS_PAGE_003 | opens notification settings page |

### 5.10 设置路由

- 来源：`web/e2e/settings-routing.spec.ts`
- 公共前置条件：用户已登录
- 基本步骤：
  1. 访问 accounts 路由
  2. 检查是否自动跳转至 security settings

| 用例ID | 核心检查点 |
|---|---|
| SETTINGS_ROUTE_001 | redirects accounts route to security settings |

## 6. 命名空间与治理

### 6.1 命名空间主页数据

- 来源：`web/e2e/namespace-page-data.spec.ts`
- 公共前置条件：已通过请求辅助工具在命名空间下生成技能数据
- 基本步骤：
  1. 打开目标 namespace 页面
  2. 检查页面中是否显示与种子技能相关的上下文信息

| 用例ID | 核心检查点 |
|---|---|
| NS_PAGE_001 | shows namespace page with request-seeded skill context |

### 6.2 命名空间成员管理

- 来源：`web/e2e/namespace-members-data.spec.ts`
- 公共前置条件：用户对某命名空间拥有可写权限
- 基本步骤：
  1. 打开成员管理页
  2. 检查成员管理页面是否成功加载

| 用例ID | 核心检查点 |
|---|---|
| NS_MEMBER_001 | opens members management for writable namespace |

### 6.3 命名空间评审数据

- 来源：`web/e2e/namespace-reviews-data.spec.ts`
- 公共前置条件：命名空间下存在种子评审数据
- 基本步骤：
  1. 进入 namespace reviews 页面
  2. 检查评审上下文和相关数据是否展示

| 用例ID | 核心检查点 |
|---|---|
| NS_REVIEW_001 | opens namespace reviews page with seeded review data context |

## 7. 公共页面与导航

### 7.1 首页导航与搜索入口

- 来源：`web/e2e/landing-navigation.spec.ts`
- 公共前置条件：未登录访问首页
- 基本步骤：
  1. 在 hero 区输入搜索内容并提交
  2. 从首页尝试执行发布动作
  3. 检查搜索跳转和登录拦截

| 用例ID | 核心检查点 |
|---|---|
| LANDING_NAV_001 | submits the hero search to the search page |
| LANDING_NAV_002 | redirects anonymous publish attempts to login |

### 7.2 公共法律页面

- 来源：`web/e2e/public-pages.spec.ts`
- 公共前置条件：未登录
- 基本步骤：
  1. 直接访问 privacy 和 terms 页面
  2. 检查文档内容是否可正常渲染

| 用例ID | 核心检查点 |
|---|---|
| PUBLIC_PAGE_001 | renders privacy and terms documents directly |

## 8. 发布流程

### 8.1 Dashboard 发布技能 UI 流程

- 来源：`web/e2e/publish-flow-ui.spec.ts`
- 公共前置条件：用户已登录并进入 dashboard，具备可上传的生成技能包
- 基本步骤：
  1. 在 dashboard 页面发起发布
  2. 上传生成的技能包
  3. 完成发布流程并检查结果

| 用例ID | 核心检查点 |
|---|---|
| PUBLISH_UI_001 | publishes a generated skill package from dashboard page |

### 8.2 发布审核通过与可见性

- 来源：`Image #1`
- 公共前置条件：用户已登录并具备技能上传权限，管理员可执行审核通过操作
- 基本步骤：
  1. 用户按可见性类型上传技能
  2. 管理员执行审核通过
  3. 检查我的技能列表、技能广场和详情页版本操作

| 用例ID | 核心检查点 |
|---|---|
| PUBLISH_REVIEW_001 | 用户上传公开技能并审核通过后，我的技能展示已发布，技能广场新增技能，详情页支持版本对比与重新发布 |
| PUBLISH_REVIEW_004 | 用户上传仅登录用户可见技能并审核通过后，我的技能展示已发布，非登录用户不可见 |
| PUBLISH_REVIEW_006 | 用户上传私有技能并审核通过后，我的技能展示已发布，技能广场仅发布者可见 |

### 8.3 审核不通过与撤销

- 来源：`Image #1`
- 公共前置条件：用户已上传技能并生成待审核卡片
- 基本步骤：
  1. 用户上传不同可见性的技能
  2. 管理员执行审核不通过，或用户主动撤销技能
  3. 检查我的技能列表中的状态展示

| 用例ID | 核心检查点 |
|---|---|
| PUBLISH_REVIEW_002 | 用户上传公开技能审核不通过后，我的技能列表展示审核不通过 |
| PUBLISH_REVIEW_003 | 用户上传技能后主动撤销，我的技能列表状态展示已撤销 |
| PUBLISH_REVIEW_005 | 用户上传仅登录用户可见技能审核不通过后，我的技能列表展示审核不通过 |
| PUBLISH_REVIEW_007 | 用户上传私有技能审核不通过后，我的技能列表展示审核不通过 |

### 8.4 归档与版本删除

- 来源：`Image #1`
- 公共前置条件：技能已进入已发布、审核不通过或撤销发布等可操作状态
- 基本步骤：
  1. 对已发布技能执行归档或版本删除操作
  2. 检查技能广场可见性、下载能力、列表标签和删除按钮状态

| 用例ID | 核心检查点 |
|---|---|
| PUBLISH_LIFECYCLE_001 | 技能归档后，技能广场不可见、不可下载，我的技能列表显示已归档标签 |
| PUBLISH_VERSION_001 | 审核不通过版本和撤销发布版本可删除，但仅剩一个版本时不允许删除 |

### 8.5 重新发布与版本可见性

- 来源：`Image #1`
- 公共前置条件：技能已存在至少一个已发布版本
- 基本步骤：
  1. 对已发布版本执行重新发布
  2. 在多版本场景下切换公开/私有可见性
  3. 检查当前版本展示和技能广场可见范围

| 用例ID | 核心检查点 |
|---|---|
| PUBLISH_VERSION_002 | 已发布版本重新发布后，审核通过版本展示为当前版本 |
| PUBLISH_VERSION_003 | 已发布公开版本重新发布为私有版本后，多版本场景下应以当前版本状态决定技能可见性 |

## 9. 覆盖补充建议

- 当前文档已完整覆盖 `web/e2e` 中全部 `.spec.ts` 的 `test(...)` 条目，但仍保持为“自动化场景转写版”。
- 如果后续要导入测试平台，建议继续补充字段：优先级、测试数据、执行结果、责任人、需求编号。
- `search-page-full.spec.ts`、`search-card-interaction.spec.ts`、`register-login-validation.spec.ts` 是回归主干，建议在测试平台单独建模块维护。
- 若要再细化成可执行手工用例，优先扩写以下高价值场景：
  - 注册/登录流程
  - 搜索排序、筛选、分页
  - 权限与路由守卫
  - 发布技能流程
