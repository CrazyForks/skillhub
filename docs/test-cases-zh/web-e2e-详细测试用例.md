# SkillHub Web E2E 详细测试用例

## 1. 文档说明

- 来源目录：`web/e2e`
- 来源范围：26 个 `.spec.ts` 文件，已排除 `helpers/` 夹具文件
- 用例总数：126
- 输出目标：将 Playwright 自动化测试转写为更适合评审、执行和导入测试平台的 Markdown 用例
- 字段说明：优先级以原注释中的 `P0/P1` 为准；未显式标注的用例按场景风险推定

## 2. 分类总览

1. 认证与权限
2. 搜索与发现
3. 工作台与设置
4. 命名空间与治理
5. 公共页面与导航
6. 发布流程

## 认证与权限

### 3.1 登录页入口校验

- 模块：认证入口
- 自动化来源：`web/e2e/auth-entry.spec.ts`
- 场景说明：登录页入口校验
- 公共前置条件：未登录用户可访问 `/login`，页面语言已切换为 English。
- 公共步骤：
  1. 打开登录页
  2. 不填写必填项直接提交
  3. 点击注册链接并观察 `returnTo` 参数
- 公共预期：页面对必填项给出明确校验，且从登录页跳转注册页时保留 `returnTo` 参数。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| AUTH_ENTRY_001 | validates required fields and preserves returnTo on register link | P0 | 未登录用户可访问 `/login`，页面语言已切换为 English。 | 1. 打开登录页<br>2. 不填写必填项直接提交<br>3. 点击注册链接并观察 `returnTo` 参数 | 符合断言：validates required fields and preserves returnTo on register link | 功能/UI | auth-entry.spec.ts / 登录页入口校验 |

### 3.2 CLI 授权参数校验

- 模块：CLI 认证
- 自动化来源：`web/e2e/cli-auth.spec.ts`
- 场景说明：CLI 授权参数校验
- 公共前置条件：浏览器可访问 CLI 授权页面或相关回调页面。
- 公共步骤：
  1. 构造缺失重定向参数的授权链接
  2. 访问页面
  3. 观察错误反馈
- 公共预期：系统应识别缺失参数并展示错误提示，不继续执行授权流程。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| CLI_AUTH_001 | shows error for missing redirect params | P0 | 浏览器可访问 CLI 授权页面或相关回调页面。 | 1. 构造缺失重定向参数的授权链接<br>2. 访问页面<br>3. 观察错误反馈 | 符合断言：shows error for missing redirect params | 功能/异常 | cli-auth.spec.ts / CLI 授权参数校验 |

### 3.3 匿名用户访问受保护页面

- 模块：路由保护
- 自动化来源：`web/e2e/protected-routes.spec.ts`
- 场景说明：匿名用户访问受保护页面
- 公共前置条件：匿名用户未登录。
- 公共步骤：
  1. 直接访问 dashboard 或 admin 路由
  2. 观察跳转结果
- 公共预期：匿名用户访问受保护页面时应被重定向到登录页或拦截页。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| PROTECTED_ROUTE_001 | redirects anonymous users from dashboard and admin routes | P0 | 匿名用户未登录。 | 1. 直接访问 dashboard 或 admin 路由<br>2. 观察跳转结果 | 符合断言：redirects anonymous users from dashboard and admin routes | 功能/权限 | protected-routes.spec.ts / 匿名用户访问受保护页面 |

### 3.4 用户名校验

- 模块：注册
- 自动化来源：`web/e2e/register-login-validation.spec.ts`
- 场景说明：用户名校验
- 公共前置条件：访问 `/register`，用户名输入框可操作。
- 公共步骤：
  1. 在用户名输入框输入指定测试数据或保持为空
  2. 触发失焦或点击 Register
  3. 观察校验提示
- 公共预期：用户名校验符合长度、字符集和必填规则。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_UN_008 | shows required error when username is empty | P0 | 访问 `/register`，用户名输入框可操作。 | 1. 在用户名输入框输入指定测试数据或保持为空<br>2. 触发失焦或点击 Register<br>3. 观察校验提示 | 符合断言：shows required error when username is empty | 功能/表单校验 | register-login-validation.spec.ts / 用户名校验 |
| TC_UN_001 | accepts valid username with minimum 3 characters | P0 | 访问 `/register`，用户名输入框可操作。 | 1. 在用户名输入框输入指定测试数据或保持为空<br>2. 触发失焦或点击 Register<br>3. 观察校验提示 | 符合断言：accepts valid username with minimum 3 characters | 功能/表单校验 | register-login-validation.spec.ts / 用户名校验 |
| TC_UN_006 | shows length error for 2-character username | P1 | 访问 `/register`，用户名输入框可操作。 | 1. 在用户名输入框输入指定测试数据或保持为空<br>2. 触发失焦或点击 Register<br>3. 观察校验提示 | 符合断言：shows length error for 2-character username | 功能/表单校验 | register-login-validation.spec.ts / 用户名校验 |
| TC_UN_009 | shows error for username with special characters like @ | P1 | 访问 `/register`，用户名输入框可操作。 | 1. 在用户名输入框输入指定测试数据或保持为空<br>2. 触发失焦或点击 Register<br>3. 观察校验提示 | 符合断言：shows error for username with special characters like @ | 功能/表单校验 | register-login-validation.spec.ts / 用户名校验 |
| TC_UN_010 | shows error for username containing Chinese characters | P1 | 访问 `/register`，用户名输入框可操作。 | 1. 在用户名输入框输入指定测试数据或保持为空<br>2. 触发失焦或点击 Register<br>3. 观察校验提示 | 符合断言：shows error for username containing Chinese characters | 功能/表单校验 | register-login-validation.spec.ts / 用户名校验 |

### 3.5 邮箱校验

- 模块：注册
- 自动化来源：`web/e2e/register-login-validation.spec.ts`
- 场景说明：邮箱校验
- 公共前置条件：访问 `/register`，邮箱输入框可见。
- 公共步骤：
  1. 在邮箱输入框输入指定测试数据或清空
  2. 触发失焦
  3. 观察邮箱格式校验结果
- 公共预期：邮箱字段应满足可选和格式校验规则。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_EM_007 | allows empty email (email is optional) | P0 | 访问 `/register`，邮箱输入框可见。 | 1. 在邮箱输入框输入指定测试数据或清空<br>2. 触发失焦<br>3. 观察邮箱格式校验结果 | 符合断言：allows empty email (email is optional) | 功能/表单校验 | register-login-validation.spec.ts / 邮箱校验 |
| TC_EM_008 | shows error for email missing @ symbol | P1 | 访问 `/register`，邮箱输入框可见。 | 1. 在邮箱输入框输入指定测试数据或清空<br>2. 触发失焦<br>3. 观察邮箱格式校验结果 | 符合断言：shows error for email missing @ symbol | 功能/表单校验 | register-login-validation.spec.ts / 邮箱校验 |
| TC_EM_009 | shows error for email missing domain after @ | P1 | 访问 `/register`，邮箱输入框可见。 | 1. 在邮箱输入框输入指定测试数据或清空<br>2. 触发失焦<br>3. 观察邮箱格式校验结果 | 符合断言：shows error for email missing domain after @ | 功能/表单校验 | register-login-validation.spec.ts / 邮箱校验 |

### 3.6 密码校验

- 模块：注册
- 自动化来源：`web/e2e/register-login-validation.spec.ts`
- 场景说明：密码校验
- 公共前置条件：访问 `/register`，密码输入框可操作。
- 公共步骤：
  1. 在密码输入框输入指定密码或留空
  2. 触发失焦或点击 Register
  3. 观察长度、复杂度和必填校验
- 公共预期：密码应满足最小长度、复杂度和必填规则。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_PW_013 | shows required error when password is empty | P0 | 访问 `/register`，密码输入框可操作。 | 1. 在密码输入框输入指定密码或留空<br>2. 触发失焦或点击 Register<br>3. 观察长度、复杂度和必填校验 | 符合断言：shows required error when password is empty | 功能/表单校验 | register-login-validation.spec.ts / 密码校验 |
| TC_PW_007 | shows length error for 7-character password | P1 | 访问 `/register`，密码输入框可操作。 | 1. 在密码输入框输入指定密码或留空<br>2. 触发失焦或点击 Register<br>3. 观察长度、复杂度和必填校验 | 符合断言：shows length error for 7-character password | 功能/表单校验 | register-login-validation.spec.ts / 密码校验 |
| TC_PW_008 | shows complexity error for password with only 2 character types | P1 | 访问 `/register`，密码输入框可操作。 | 1. 在密码输入框输入指定密码或留空<br>2. 触发失焦或点击 Register<br>3. 观察长度、复杂度和必填校验 | 符合断言：shows complexity error for password with only 2 character types | 功能/表单校验 | register-login-validation.spec.ts / 密码校验 |
| TC_PW_001 | accepts valid password with 3 character types and minimum length | P0 | 访问 `/register`，密码输入框可操作。 | 1. 在密码输入框输入指定密码或留空<br>2. 触发失焦或点击 Register<br>3. 观察长度、复杂度和必填校验 | 符合断言：accepts valid password with 3 character types and minimum length | 功能/表单校验 | register-login-validation.spec.ts / 密码校验 |

### 3.7 注册流程

- 模块：注册
- 自动化来源：`web/e2e/register-login-validation.spec.ts`
- 场景说明：注册流程
- 公共前置条件：可访问 `/register` 且后端真实 API 可用。
- 公共步骤：
  1. 打开注册页
  2. 输入用户名、可选邮箱和密码
  3. 点击 Register 提交并观察页面反馈
- 公共预期：合法数据可注册成功，异常数据会给出可识别错误提示。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_REG_001 | registers successfully with valid username, email and password | P0 | 可访问 `/register` 且后端真实 API 可用。 | 1. 打开注册页<br>2. 输入用户名、可选邮箱和密码<br>3. 点击 Register 提交并观察页面反馈 | 符合断言：registers successfully with valid username, email and password | 功能/流程 | register-login-validation.spec.ts / 注册流程 |
| TC_REG_002 | registers successfully without email (email is optional) | P0 | 可访问 `/register` 且后端真实 API 可用。 | 1. 打开注册页<br>2. 输入用户名、可选邮箱和密码<br>3. 点击 Register 提交并观察页面反馈 | 符合断言：registers successfully without email (email is optional) | 功能/流程 | register-login-validation.spec.ts / 注册流程 |
| TC_REG_003 | shows error when registering with existing username | P0 | 可访问 `/register` 且后端真实 API 可用。 | 1. 打开注册页<br>2. 输入用户名、可选邮箱和密码<br>3. 点击 Register 提交并观察页面反馈 | 符合断言：shows error when registering with existing username | 功能/流程 | register-login-validation.spec.ts / 注册流程 |
| TC_REG_005 | shows validation errors when submitting empty required fields | P0 | 可访问 `/register` 且后端真实 API 可用。 | 1. 打开注册页<br>2. 输入用户名、可选邮箱和密码<br>3. 点击 Register 提交并观察页面反馈 | 符合断言：shows validation errors when submitting empty required fields | 功能/流程 | register-login-validation.spec.ts / 注册流程 |

### 3.8 登录流程与安全输入

- 模块：登录
- 自动化来源：`web/e2e/register-login-validation.spec.ts`
- 场景说明：登录流程与安全输入
- 公共前置条件：可访问 `/login`，并具备已注册账号或可构造异常输入。
- 公共步骤：
  1. 打开登录页
  2. 输入账号密码或异常测试数据
  3. 点击 Login 并观察反馈
- 公共预期：登录页应正确处理必填、错误账号密码以及恶意输入，不发生脚本执行或异常崩溃。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_REG_006 | shows required field errors when submitting empty login form | P0 | 可访问 `/login`，并具备已注册账号或可构造异常输入。 | 1. 打开登录页<br>2. 输入账号密码或异常测试数据<br>3. 点击 Login 并观察反馈 | 符合断言：shows required field errors when submitting empty login form | 功能/安全 | register-login-validation.spec.ts / 登录流程与安全输入 |
| TC_REG_007 | shows error for wrong password on existing account | P0 | 可访问 `/login`，并具备已注册账号或可构造异常输入。 | 1. 打开登录页<br>2. 输入账号密码或异常测试数据<br>3. 点击 Login 并观察反馈 | 符合断言：shows error for wrong password on existing account | 功能/安全 | register-login-validation.spec.ts / 登录流程与安全输入 |
| TC_REG_008 | shows error for non-existent username login attempt | P0 | 可访问 `/login`，并具备已注册账号或可构造异常输入。 | 1. 打开登录页<br>2. 输入账号密码或异常测试数据<br>3. 点击 Login 并观察反馈 | 符合断言：shows error for non-existent username login attempt | 功能/安全 | register-login-validation.spec.ts / 登录流程与安全输入 |
| TC_REG_010 | safely handles SQL injection input in username field | P1 | 可访问 `/login`，并具备已注册账号或可构造异常输入。 | 1. 打开登录页<br>2. 输入账号密码或异常测试数据<br>3. 点击 Login 并观察反馈 | 符合断言：safely handles SQL injection input in username field | 功能/安全 | register-login-validation.spec.ts / 登录流程与安全输入 |
| TC_REG_011 | safely handles XSS payload in username field without executing script | P1 | 可访问 `/login`，并具备已注册账号或可构造异常输入。 | 1. 打开登录页<br>2. 输入账号密码或异常测试数据<br>3. 点击 Login 并观察反馈 | 符合断言：safely handles XSS payload in username field without executing script | 功能/安全 | register-login-validation.spec.ts / 登录流程与安全输入 |

### 3.9 登录注册交互

- 模块：认证 UI
- 自动化来源：`web/e2e/register-login-validation.spec.ts`
- 场景说明：登录注册交互
- 公共前置条件：访问登录页或注册页。
- 公共步骤：
  1. 操作密码可见性开关
  2. 在最后一个输入框按 Enter
  3. 通过登录页跳转注册页
- 公共预期：UI 交互符合用户预期，支持快捷提交，并保留返回参数。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_UI_003 | password visibility toggle switches between masked and plain text | P2 | 访问登录页或注册页。 | 1. 操作密码可见性开关<br>2. 在最后一个输入框按 Enter<br>3. 通过登录页跳转注册页 | 符合断言：password visibility toggle switches between masked and plain text | UI/易用性 | register-login-validation.spec.ts / 登录注册交互 |
| TC_UI_005 | pressing Enter in the last input field submits the login form | P2 | 访问登录页或注册页。 | 1. 操作密码可见性开关<br>2. 在最后一个输入框按 Enter<br>3. 通过登录页跳转注册页 | 符合断言：pressing Enter in the last input field submits the login form | UI/易用性 | register-login-validation.spec.ts / 登录注册交互 |
| AUTH_UI_003 | preserves returnTo param when navigating from register link on login page | P1 | 访问登录页或注册页。 | 1. 操作密码可见性开关<br>2. 在最后一个输入框按 Enter<br>3. 通过登录页跳转注册页 | 符合断言：preserves returnTo param when navigating from register link on login page | UI/易用性 | register-login-validation.spec.ts / 登录注册交互 |

### 3.10 普通用户越权访问控制

- 模块：角色权限
- 自动化来源：`web/e2e/role-access-control.spec.ts`
- 场景说明：普通用户越权访问控制
- 公共前置条件：使用普通用户账号登录。
- 公共步骤：
  1. 访问 review center、promotions、reports、admin 页面
  2. 观察页面是否拒绝或跳转
- 公共预期：普通用户不能访问超出角色权限的治理和管理页面。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| ROLE_AC_001 | redirects regular user from review center | P0 | 使用普通用户账号登录。 | 1. 访问 review center、promotions、reports、admin 页面<br>2. 观察页面是否拒绝或跳转 | 符合断言：redirects regular user from review center | 功能/权限 | role-access-control.spec.ts / 普通用户越权访问控制 |
| ROLE_AC_002 | redirects regular user from promotions and reports pages | P0 | 使用普通用户账号登录。 | 1. 访问 review center、promotions、reports、admin 页面<br>2. 观察页面是否拒绝或跳转 | 符合断言：redirects regular user from promotions and reports pages | 功能/权限 | role-access-control.spec.ts / 普通用户越权访问控制 |
| ROLE_AC_003 | redirects regular user from admin pages | P0 | 使用普通用户账号登录。 | 1. 访问 review center、promotions、reports、admin 页面<br>2. 观察页面是否拒绝或跳转 | 符合断言：redirects regular user from admin pages | 功能/权限 | role-access-control.spec.ts / 普通用户越权访问控制 |

### 3.11 匿名与登录态守卫差异

- 模块：路由守卫
- 自动化来源：`web/e2e/route-guard.spec.ts`
- 场景说明：匿名与登录态守卫差异
- 公共前置条件：分别准备匿名用户和已认证用户。
- 公共步骤：
  1. 匿名访问受保护路由
  2. 已登录后再次访问 dashboard
  3. 比较两种状态的行为
- 公共预期：匿名用户被拦截，已认证用户可正常访问受保护页面。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| ROUTE_GUARD_001 | redirects anonymous users to login for protected routes | P0 | 分别准备匿名用户和已认证用户。 | 1. 匿名访问受保护路由<br>2. 已登录后再次访问 dashboard<br>3. 比较两种状态的行为 | 符合断言：redirects anonymous users to login for protected routes | 功能/权限 | route-guard.spec.ts / 匿名与登录态守卫差异 |
| ROUTE_GUARD_002 | allows authenticated users to open dashboard | P0 | 分别准备匿名用户和已认证用户。 | 1. 匿名访问受保护路由<br>2. 已登录后再次访问 dashboard<br>3. 比较两种状态的行为 | 符合断言：allows authenticated users to open dashboard | 功能/权限 | route-guard.spec.ts / 匿名与登录态守卫差异 |

## 搜索与发现

### 4.1 卡片展示

- 模块：搜索卡片
- 自动化来源：`web/e2e/search-card-interaction.spec.ts`
- 场景说明：卡片展示
- 公共前置条件：搜索结果页存在技能卡片数据。
- 公共步骤：
  1. 执行搜索
  2. 观察卡片出现时间、字段、版本和描述展示
  3. 检查空结果及大结果集场景
- 公共预期：卡片展示应完整、及时，长文本截断合理，空结果和大结果集有正确反馈。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_SEARCH_INTERACT_001 | cards appear immediately after search | P0 | 搜索结果页存在技能卡片数据。 | 1. 执行搜索<br>2. 观察卡片出现时间、字段、版本和描述展示<br>3. 检查空结果及大结果集场景 | 符合断言：cards appear immediately after search | UI/功能 | search-card-interaction.spec.ts / 卡片展示 |
| TC_SEARCH_INTERACT_005 | each card shows name, description, and version | P0 | 搜索结果页存在技能卡片数据。 | 1. 执行搜索<br>2. 观察卡片出现时间、字段、版本和描述展示<br>3. 检查空结果及大结果集场景 | 符合断言：each card shows name, description, and version | UI/功能 | search-card-interaction.spec.ts / 卡片展示 |
| TC_SEARCH_INTERACT_039 | version number is displayed in v1.2.3 format | P0 | 搜索结果页存在技能卡片数据。 | 1. 执行搜索<br>2. 观察卡片出现时间、字段、版本和描述展示<br>3. 检查空结果及大结果集场景 | 符合断言：version number is displayed in v1.2.3 format | UI/功能 | search-card-interaction.spec.ts / 卡片展示 |
| TC_SEARCH_INTERACT_038 | long descriptions are truncated with ellipsis | P0 | 搜索结果页存在技能卡片数据。 | 1. 执行搜索<br>2. 观察卡片出现时间、字段、版本和描述展示<br>3. 检查空结果及大结果集场景 | 符合断言：long descriptions are truncated with ellipsis | UI/功能 | search-card-interaction.spec.ts / 卡片展示 |
| TC_SEARCH_INTERACT_031 | no results shows empty state instead of cards | P0 | 搜索结果页存在技能卡片数据。 | 1. 执行搜索<br>2. 观察卡片出现时间、字段、版本和描述展示<br>3. 检查空结果及大结果集场景 | 符合断言：no results shows empty state instead of cards | UI/功能 | search-card-interaction.spec.ts / 卡片展示 |
| TC_SEARCH_INTERACT_035 | large result sets show pagination controls | P0 | 搜索结果页存在技能卡片数据。 | 1. 执行搜索<br>2. 观察卡片出现时间、字段、版本和描述展示<br>3. 检查空结果及大结果集场景 | 符合断言：large result sets show pagination controls | UI/功能 | search-card-interaction.spec.ts / 卡片展示 |

### 4.2 卡片内容一致性

- 模块：搜索卡片
- 自动化来源：`web/e2e/search-card-interaction.spec.ts`
- 场景说明：卡片内容一致性
- 公共前置条件：搜索结果页已显示技能卡片与数量信息。
- 公共步骤：
  1. 执行搜索
  2. 比对卡片数量与数量指示器
  3. 检查下载量格式
- 公共预期：卡片数量与计数一致，下载量格式符合页面展示规范。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_SEARCH_INTERACT_003 | displayed card count is consistent with skill count indicator | P0 | 搜索结果页已显示技能卡片与数量信息。 | 1. 执行搜索<br>2. 比对卡片数量与数量指示器<br>3. 检查下载量格式 | 符合断言：displayed card count is consistent with skill count indicator | 功能/UI | search-card-interaction.spec.ts / 卡片内容一致性 |
| TC_SEARCH_INTERACT_040 | download counts are formatted correctly (numbers or K/M) | P0 | 搜索结果页已显示技能卡片与数量信息。 | 1. 执行搜索<br>2. 比对卡片数量与数量指示器<br>3. 检查下载量格式 | 符合断言：download counts are formatted correctly (numbers or K/M) | 功能/UI | search-card-interaction.spec.ts / 卡片内容一致性 |

### 4.3 卡片导航

- 模块：搜索卡片
- 自动化来源：`web/e2e/search-card-interaction.spec.ts`
- 场景说明：卡片导航
- 公共前置条件：搜索结果中至少存在一张可点击技能卡片。
- 公共步骤：
  1. 点击技能卡片或使用 Ctrl+Click
  2. 进入详情页
  3. 比对详情与原卡片信息
- 公共预期：卡片应正确跳转到详情页，新标签打开也应有效，详情内容与原卡片一致。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_SEARCH_INTERACT_007 | clicking a skill card navigates to the skill detail page | P0 | 搜索结果中至少存在一张可点击技能卡片。 | 1. 点击技能卡片或使用 Ctrl+Click<br>2. 进入详情页<br>3. 比对详情与原卡片信息 | 符合断言：clicking a skill card navigates to the skill detail page | 功能/导航 | search-card-interaction.spec.ts / 卡片导航 |
| TC_SEARCH_INTERACT_008 | skill detail page matches the card that was clicked | P0 | 搜索结果中至少存在一张可点击技能卡片。 | 1. 点击技能卡片或使用 Ctrl+Click<br>2. 进入详情页<br>3. 比对详情与原卡片信息 | 符合断言：skill detail page matches the card that was clicked | 功能/导航 | search-card-interaction.spec.ts / 卡片导航 |
| TC_SEARCH_INTERACT_009 | Ctrl+click on card opens skill detail in new tab | P1 | 搜索结果中至少存在一张可点击技能卡片。 | 1. 点击技能卡片或使用 Ctrl+Click<br>2. 进入详情页<br>3. 比对详情与原卡片信息 | 符合断言：Ctrl+click on card opens skill detail in new tab | 功能/导航 | search-card-interaction.spec.ts / 卡片导航 |

### 4.4 卡片列表与排序联动

- 模块：搜索卡片
- 自动化来源：`web/e2e/search-card-interaction.spec.ts`
- 场景说明：卡片列表与排序联动
- 公共前置条件：搜索结果页支持排序切换和重新搜索。
- 公共步骤：
  1. 切换排序标签
  2. 使用新关键词重新搜索
  3. 观察卡片列表和页码变化
- 公共预期：卡片列表会随排序或关键词变化而更新，重新搜索后页码重置。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_SEARCH_INTERACT_021 | switching sort tab re-renders card list | P0 | 搜索结果页支持排序切换和重新搜索。 | 1. 切换排序标签<br>2. 使用新关键词重新搜索<br>3. 观察卡片列表和页码变化 | 符合断言：switching sort tab re-renders card list | 功能/筛选 | search-card-interaction.spec.ts / 卡片列表与排序联动 |
| TC_SEARCH_INTERACT_026 | re-searching with new keyword replaces card list | P0 | 搜索结果页支持排序切换和重新搜索。 | 1. 切换排序标签<br>2. 使用新关键词重新搜索<br>3. 观察卡片列表和页码变化 | 符合断言：re-searching with new keyword replaces card list | 功能/筛选 | search-card-interaction.spec.ts / 卡片列表与排序联动 |
| TC_SEARCH_INTERACT_027 | re-searching resets page number to 0 | P0 | 搜索结果页支持排序切换和重新搜索。 | 1. 切换排序标签<br>2. 使用新关键词重新搜索<br>3. 观察卡片列表和页码变化 | 符合断言：re-searching resets page number to 0 | 功能/筛选 | search-card-interaction.spec.ts / 卡片列表与排序联动 |

### 4.5 卡片分页交互

- 模块：搜索卡片
- 自动化来源：`web/e2e/search-card-interaction.spec.ts`
- 场景说明：卡片分页交互
- 公共前置条件：结果集足够触发分页。
- 公共步骤：
  1. 切换到下一页
  2. 比对前后页卡片内容
  3. 观察结果区域滚动位置
- 公共预期：页码切换后卡片内容更新，结果区域自动回到顶部。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_SEARCH_INTERACT_023 | switching to next page shows different cards | P0 | 结果集足够触发分页。 | 1. 切换到下一页<br>2. 比对前后页卡片内容<br>3. 观察结果区域滚动位置 | 符合断言：switching to next page shows different cards | 功能/分页 | search-card-interaction.spec.ts / 卡片分页交互 |
| TC_SEARCH_INTERACT_025 | switching page scrolls back to top of results | P1 | 结果集足够触发分页。 | 1. 切换到下一页<br>2. 比对前后页卡片内容<br>3. 观察结果区域滚动位置 | 符合断言：switching page scrolls back to top of results | 功能/分页 | search-card-interaction.spec.ts / 卡片分页交互 |

### 4.6 卡片加载态

- 模块：搜索卡片
- 自动化来源：`web/e2e/search-card-interaction.spec.ts`
- 场景说明：卡片加载态
- 公共前置条件：执行搜索操作并触发结果加载。
- 公共步骤：
  1. 发起搜索
  2. 观察骨架屏显示与消失
  3. 确认真实卡片渲染
- 公共预期：骨架屏加载态应正常消失，并被真实卡片列表替换。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_SEARCH_INTERACT_030 | skeleton screen disappears and real cards appear after load | P0 | 执行搜索操作并触发结果加载。 | 1. 发起搜索<br>2. 观察骨架屏显示与消失<br>3. 确认真实卡片渲染 | 符合断言：skeleton screen disappears and real cards appear after load | UI/加载态 | search-card-interaction.spec.ts / 卡片加载态 |

### 4.7 响应式布局

- 模块：搜索卡片
- 自动化来源：`web/e2e/search-card-interaction.spec.ts`
- 场景说明：响应式布局
- 公共前置条件：准备桌面、平板和移动端视口。
- 公共步骤：
  1. 在不同视口尺寸访问搜索结果页
  2. 观察卡片列数与布局变化
  3. 在移动端执行触控跳转
- 公共预期：不同视口下卡片应按预期切换为 3 列、2 列或 1 列，并保持可用。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_SEARCH_INTERACT_042 | desktop viewport shows 3-column card grid | P0 | 准备桌面、平板和移动端视口。 | 1. 在不同视口尺寸访问搜索结果页<br>2. 观察卡片列数与布局变化<br>3. 在移动端执行触控跳转 | 符合断言：desktop viewport shows 3-column card grid | UI/兼容性 | search-card-interaction.spec.ts / 响应式布局 |
| TC_SEARCH_INTERACT_044 | mobile viewport shows single-column card layout | P0 | 准备桌面、平板和移动端视口。 | 1. 在不同视口尺寸访问搜索结果页<br>2. 观察卡片列数与布局变化<br>3. 在移动端执行触控跳转 | 符合断言：mobile viewport shows single-column card layout | UI/兼容性 | search-card-interaction.spec.ts / 响应式布局 |
| TC_SEARCH_INTERACT_043 | tablet viewport shows 2-column card layout | P0 | 准备桌面、平板和移动端视口。 | 1. 在不同视口尺寸访问搜索结果页<br>2. 观察卡片列数与布局变化<br>3. 在移动端执行触控跳转 | 符合断言：tablet viewport shows 2-column card layout | UI/兼容性 | search-card-interaction.spec.ts / 响应式布局 |
| TC_SEARCH_INTERACT_045 | card layout adjusts when browser window is resized | P1 | 准备桌面、平板和移动端视口。 | 1. 在不同视口尺寸访问搜索结果页<br>2. 观察卡片列数与布局变化<br>3. 在移动端执行触控跳转 | 符合断言：card layout adjusts when browser window is resized | UI/兼容性 | search-card-interaction.spec.ts / 响应式布局 |
| TC_SEARCH_INTERACT_046 | mobile touch on card navigates to skill detail | P0 | 准备桌面、平板和移动端视口。 | 1. 在不同视口尺寸访问搜索结果页<br>2. 观察卡片列数与布局变化<br>3. 在移动端执行触控跳转 | 符合断言：mobile touch on card navigates to skill detail | UI/兼容性 | search-card-interaction.spec.ts / 响应式布局 |

### 4.8 键盘可访问性

- 模块：搜索卡片
- 自动化来源：`web/e2e/search-card-interaction.spec.ts`
- 场景说明：键盘可访问性
- 公共前置条件：搜索结果页有可聚焦技能卡片。
- 公共步骤：
  1. 使用 Tab 在卡片间移动焦点
  2. 按 Enter 打开详情
  3. 观察焦点样式
- 公共预期：卡片支持键盘导航，Enter 可触发跳转，焦点样式清晰可见。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_SEARCH_INTERACT_049 | Tab key can navigate between skill cards | P1 | 搜索结果页有可聚焦技能卡片。 | 1. 使用 Tab 在卡片间移动焦点<br>2. 按 Enter 打开详情<br>3. 观察焦点样式 | 符合断言：Tab key can navigate between skill cards | 可访问性 | search-card-interaction.spec.ts / 键盘可访问性 |
| TC_SEARCH_INTERACT_050 | pressing Enter on a focused card opens the skill detail | P1 | 搜索结果页有可聚焦技能卡片。 | 1. 使用 Tab 在卡片间移动焦点<br>2. 按 Enter 打开详情<br>3. 观察焦点样式 | 符合断言：pressing Enter on a focused card opens the skill detail | 可访问性 | search-card-interaction.spec.ts / 键盘可访问性 |
| TC_SEARCH_INTERACT_051 | focused card has a visible focus indicator | P1 | 搜索结果页有可聚焦技能卡片。 | 1. 使用 Tab 在卡片间移动焦点<br>2. 按 Enter 打开详情<br>3. 观察焦点样式 | 符合断言：focused card has a visible focus indicator | 可访问性 | search-card-interaction.spec.ts / 键盘可访问性 |

### 4.9 单结果与返回缓存

- 模块：搜索卡片
- 自动化来源：`web/e2e/search-card-interaction.spec.ts`
- 场景说明：单结果与返回缓存
- 公共前置条件：具备单结果搜索和从详情页返回搜索页的场景。
- 公共步骤：
  1. 验证单结果卡片布局
  2. 从搜索页进入详情后返回
  3. 观察缓存结果恢复速度
- 公共预期：单结果布局正确，返回搜索页后可较快恢复缓存结果。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_SEARCH_INTERACT_033 | single search result displays card layout correctly | P1 | 具备单结果搜索和从详情页返回搜索页的场景。 | 1. 验证单结果卡片布局<br>2. 从搜索页进入详情后返回<br>3. 观察缓存结果恢复速度 | 符合断言：single search result displays card layout correctly | 功能/性能体验 | search-card-interaction.spec.ts / 单结果与返回缓存 |
| TC_SEARCH_INTERACT_060 | returning to search page shows cached results quickly | P1 | 具备单结果搜索和从详情页返回搜索页的场景。 | 1. 验证单结果卡片布局<br>2. 从搜索页进入详情后返回<br>3. 观察缓存结果恢复速度 | 符合断言：returning to search page shows cached results quickly | 功能/性能体验 | search-card-interaction.spec.ts / 单结果与返回缓存 |

### 4.10 搜索基础流程

- 模块：搜索
- 自动化来源：`web/e2e/search-flow.spec.ts`
- 场景说明：搜索基础流程
- 公共前置条件：访问搜索页，页面语言为 English。
- 公共步骤：
  1. 打开搜索页
  2. 检查搜索控件和 URL 查询参数
  3. 切换 `Starred only` 验证匿名态行为
- 公共预期：搜索控件正常渲染，URL 可保持查询状态，匿名启用收藏筛选时会跳转登录。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| SEARCH_FLOW_001 | renders search controls and keeps query state in URL | P0 | 访问搜索页，页面语言为 English。 | 1. 打开搜索页<br>2. 检查搜索控件和 URL 查询参数<br>3. 切换 `Starred only` 验证匿名态行为 | 符合断言：renders search controls and keeps query state in URL | 功能/流程 | search-flow.spec.ts / 搜索基础流程 |
| SEARCH_FLOW_002 | redirects anonymous user to login when enabling starred filter | P0 | 访问搜索页，页面语言为 English。 | 1. 打开搜索页<br>2. 检查搜索控件和 URL 查询参数<br>3. 切换 `Starred only` 验证匿名态行为 | 符合断言：redirects anonymous user to login when enabling starred filter | 功能/流程 | search-flow.spec.ts / 搜索基础流程 |

### 4.11 搜索输入与关键词处理

- 模块：搜索
- 自动化来源：`web/e2e/search-page-full.spec.ts`
- 场景说明：搜索输入与关键词处理
- 公共前置条件：访问 `/search`，搜索框可操作。
- 公共步骤：
  1. 输入关键词、空值、特殊字符或中英文数据
  2. 点击 Search 或按 Enter 提交
  3. 观察结果页和 URL
- 公共预期：搜索框应正确处理不同输入，不崩溃，并按规则更新 URL 和结果。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_SEARCH_INPUT_001 | searches with a single keyword and shows results | P0 | 访问 `/search`，搜索框可操作。 | 1. 输入关键词、空值、特殊字符或中英文数据<br>2. 点击 Search 或按 Enter 提交<br>3. 观察结果页和 URL | 符合断言：searches with a single keyword and shows results | 功能/输入 | search-page-full.spec.ts / 搜索输入与关键词处理 |
| TC_SEARCH_INPUT_003 | empty search shows default skill list | P0 | 访问 `/search`，搜索框可操作。 | 1. 输入关键词、空值、特殊字符或中英文数据<br>2. 点击 Search 或按 Enter 提交<br>3. 观察结果页和 URL | 符合断言：empty search shows default skill list | 功能/输入 | search-page-full.spec.ts / 搜索输入与关键词处理 |
| TC_SEARCH_INPUT_004 | pressing Enter in search box triggers search | P0 | 访问 `/search`，搜索框可操作。 | 1. 输入关键词、空值、特殊字符或中英文数据<br>2. 点击 Search 或按 Enter 提交<br>3. 观察结果页和 URL | 符合断言：pressing Enter in search box triggers search | 功能/输入 | search-page-full.spec.ts / 搜索输入与关键词处理 |
| TC_SEARCH_INPUT_009 | supports Chinese keyword search without error | P0 | 访问 `/search`，搜索框可操作。 | 1. 输入关键词、空值、特殊字符或中英文数据<br>2. 点击 Search 或按 Enter 提交<br>3. 观察结果页和 URL | 符合断言：supports Chinese keyword search without error | 功能/输入 | search-page-full.spec.ts / 搜索输入与关键词处理 |
| TC_SEARCH_INPUT_010 | supports English keyword search | P0 | 访问 `/search`，搜索框可操作。 | 1. 输入关键词、空值、特殊字符或中英文数据<br>2. 点击 Search 或按 Enter 提交<br>3. 观察结果页和 URL | 符合断言：supports English keyword search | 功能/输入 | search-page-full.spec.ts / 搜索输入与关键词处理 |
| TC_SEARCH_INPUT_007 | handles special characters in search without crashing | P1 | 访问 `/search`，搜索框可操作。 | 1. 输入关键词、空值、特殊字符或中英文数据<br>2. 点击 Search 或按 Enter 提交<br>3. 观察结果页和 URL | 符合断言：handles special characters in search without crashing | 功能/输入 | search-page-full.spec.ts / 搜索输入与关键词处理 |
| TC_SEARCH_INPUT_011 | trims leading and trailing spaces from search query | P1 | 访问 `/search`，搜索框可操作。 | 1. 输入关键词、空值、特殊字符或中英文数据<br>2. 点击 Search 或按 Enter 提交<br>3. 观察结果页和 URL | 符合断言：trims leading and trailing spaces from search query | 功能/输入 | search-page-full.spec.ts / 搜索输入与关键词处理 |

### 4.12 排序与筛选

- 模块：搜索
- 自动化来源：`web/e2e/search-page-full.spec.ts`
- 场景说明：排序与筛选
- 公共前置条件：访问带查询参数的搜索结果页。
- 公共步骤：
  1. 切换 `Relevance`、`Downloads`、`Newest` 标签
  2. 切换 `Starred only`
  3. 检查 URL、关键词保留和页码变化
- 公共预期：排序筛选切换应正确刷新 URL 和列表，匿名启用收藏筛选时应重定向登录。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_SEARCH_SORT_001 | relevance sort tab is selected by default | P0 | 访问带查询参数的搜索结果页。 | 1. 切换 `Relevance`、`Downloads`、`Newest` 标签<br>2. 切换 `Starred only`<br>3. 检查 URL、关键词保留和页码变化 | 符合断言：relevance sort tab is selected by default | 功能/筛选 | search-page-full.spec.ts / 排序与筛选 |
| TC_SEARCH_SORT_004 | clicking Downloads tab updates sort in URL | P0 | 访问带查询参数的搜索结果页。 | 1. 切换 `Relevance`、`Downloads`、`Newest` 标签<br>2. 切换 `Starred only`<br>3. 检查 URL、关键词保留和页码变化 | 符合断言：clicking Downloads tab updates sort in URL | 功能/筛选 | search-page-full.spec.ts / 排序与筛选 |
| TC_SEARCH_SORT_005 | clicking Newest tab updates sort in URL | P0 | 访问带查询参数的搜索结果页。 | 1. 切换 `Relevance`、`Downloads`、`Newest` 标签<br>2. 切换 `Starred only`<br>3. 检查 URL、关键词保留和页码变化 | 符合断言：clicking Newest tab updates sort in URL | 功能/筛选 | search-page-full.spec.ts / 排序与筛选 |
| TC_SEARCH_SORT_006 | switching sort tab preserves the search keyword | P0 | 访问带查询参数的搜索结果页。 | 1. 切换 `Relevance`、`Downloads`、`Newest` 标签<br>2. 切换 `Starred only`<br>3. 检查 URL、关键词保留和页码变化 | 符合断言：switching sort tab preserves the search keyword | 功能/筛选 | search-page-full.spec.ts / 排序与筛选 |
| TC_SEARCH_SORT_007 | switching sort tab resets page to 0 | P0 | 访问带查询参数的搜索结果页。 | 1. 切换 `Relevance`、`Downloads`、`Newest` 标签<br>2. 切换 `Starred only`<br>3. 检查 URL、关键词保留和页码变化 | 符合断言：switching sort tab resets page to 0 | 功能/筛选 | search-page-full.spec.ts / 排序与筛选 |
| TC_SEARCH_SORT_012 | URL contains sort parameter after switching tabs | P1 | 访问带查询参数的搜索结果页。 | 1. 切换 `Relevance`、`Downloads`、`Newest` 标签<br>2. 切换 `Starred only`<br>3. 检查 URL、关键词保留和页码变化 | 符合断言：URL contains sort parameter after switching tabs | 功能/筛选 | search-page-full.spec.ts / 排序与筛选 |
| SEARCH_SORT_FILTER_007 | starred only filter redirects anonymous user to login | P0 | 访问带查询参数的搜索结果页。 | 1. 切换 `Relevance`、`Downloads`、`Newest` 标签<br>2. 切换 `Starred only`<br>3. 检查 URL、关键词保留和页码变化 | 符合断言：starred only filter redirects anonymous user to login | 功能/筛选 | search-page-full.spec.ts / 排序与筛选 |
| SEARCH_SORT_FILTER_008 | starred only filter stays on search page for authenticated user | P0 | 访问带查询参数的搜索结果页。 | 1. 切换 `Relevance`、`Downloads`、`Newest` 标签<br>2. 切换 `Starred only`<br>3. 检查 URL、关键词保留和页码变化 | 符合断言：starred only filter stays on search page for authenticated user | 功能/筛选 | search-page-full.spec.ts / 排序与筛选 |

### 4.13 结果数量显示

- 模块：搜索
- 自动化来源：`web/e2e/search-page-full.spec.ts`
- 场景说明：结果数量显示
- 公共前置条件：搜索页存在可返回结果和无结果两类数据。
- 公共步骤：
  1. 打开默认列表或执行搜索
  2. 观察技能数量指示器
  3. 切换排序或构造无结果场景
- 公共预期：数量指示器应可见，并随着搜索结果变化准确更新。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_SEARCH_COUNT_001 | skill count indicator is visible on search page | P0 | 搜索页存在可返回结果和无结果两类数据。 | 1. 打开默认列表或执行搜索<br>2. 观察技能数量指示器<br>3. 切换排序或构造无结果场景 | 符合断言：skill count indicator is visible on search page | 功能/UI | search-page-full.spec.ts / 结果数量显示 |
| TC_SEARCH_COUNT_007 | skill count updates after performing a search | P0 | 搜索页存在可返回结果和无结果两类数据。 | 1. 打开默认列表或执行搜索<br>2. 观察技能数量指示器<br>3. 切换排序或构造无结果场景 | 符合断言：skill count updates after performing a search | 功能/UI | search-page-full.spec.ts / 结果数量显示 |
| TC_SEARCH_COUNT_009 | shows 0 skills count when search returns no results | P0 | 搜索页存在可返回结果和无结果两类数据。 | 1. 打开默认列表或执行搜索<br>2. 观察技能数量指示器<br>3. 切换排序或构造无结果场景 | 符合断言：shows 0 skills count when search returns no results | 功能/UI | search-page-full.spec.ts / 结果数量显示 |
| TC_SEARCH_COUNT_008 | skill count remains the same after switching sort tab | P0 | 搜索页存在可返回结果和无结果两类数据。 | 1. 打开默认列表或执行搜索<br>2. 观察技能数量指示器<br>3. 切换排序或构造无结果场景 | 符合断言：skill count remains the same after switching sort tab | 功能/UI | search-page-full.spec.ts / 结果数量显示 |

### 4.14 结果区展示

- 模块：搜索
- 自动化来源：`web/e2e/search-page-full.spec.ts`
- 场景说明：结果区展示
- 公共前置条件：访问搜索结果页。
- 公共步骤：
  1. 执行有结果和无结果搜索
  2. 观察卡片、空态和错误态
  3. 切换不同排序方式
- 公共预期：结果区应稳定展示卡片或空态，并与排序方式保持一致。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_SEARCH_RESULT_001 | shows skill cards when search returns results | P0 | 访问搜索结果页。 | 1. 执行有结果和无结果搜索<br>2. 观察卡片、空态和错误态<br>3. 切换不同排序方式 | 符合断言：shows skill cards when search returns results | 功能/UI | search-page-full.spec.ts / 结果区展示 |
| TC_SEARCH_RESULT_002 | shows empty state message when no results found | P0 | 访问搜索结果页。 | 1. 执行有结果和无结果搜索<br>2. 观察卡片、空态和错误态<br>3. 切换不同排序方式 | 符合断言：shows empty state message when no results found | 功能/UI | search-page-full.spec.ts / 结果区展示 |
| TC_SEARCH_RESULT_006 | page renders without error during and after search | P0 | 访问搜索结果页。 | 1. 执行有结果和无结果搜索<br>2. 观察卡片、空态和错误态<br>3. 切换不同排序方式 | 符合断言：page renders without error during and after search | 功能/UI | search-page-full.spec.ts / 结果区展示 |
| TC_SEARCH_RESULT_008 | number of displayed cards matches the count indicator | P0 | 访问搜索结果页。 | 1. 执行有结果和无结果搜索<br>2. 观察卡片、空态和错误态<br>3. 切换不同排序方式 | 符合断言：number of displayed cards matches the count indicator | 功能/UI | search-page-full.spec.ts / 结果区展示 |
| TC_SEARCH_RESULT_009 | results are sorted by downloads when Downloads tab is selected | P0 | 访问搜索结果页。 | 1. 执行有结果和无结果搜索<br>2. 观察卡片、空态和错误态<br>3. 切换不同排序方式 | 符合断言：results are sorted by downloads when Downloads tab is selected | 功能/UI | search-page-full.spec.ts / 结果区展示 |
| TC_SEARCH_RESULT_010 | results are sorted by newest when Newest tab is selected | P0 | 访问搜索结果页。 | 1. 执行有结果和无结果搜索<br>2. 观察卡片、空态和错误态<br>3. 切换不同排序方式 | 符合断言：results are sorted by newest when Newest tab is selected | 功能/UI | search-page-full.spec.ts / 结果区展示 |

### 4.15 分页行为

- 模块：搜索
- 自动化来源：`web/e2e/search-page-full.spec.ts`
- 场景说明：分页行为
- 公共前置条件：查询结果数足够触发分页。
- 公共步骤：
  1. 打开带分页的搜索结果
  2. 切换页码
  3. 检查 URL 与筛选条件保持情况
- 公共预期：分页切换应保持查询上下文，并正确更新页码状态。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_SEARCH_PAGE_011 | URL contains page parameter | P1 | 查询结果数足够触发分页。 | 1. 打开带分页的搜索结果<br>2. 切换页码<br>3. 检查 URL 与筛选条件保持情况 | 符合断言：URL contains page parameter | 功能/分页 | search-page-full.spec.ts / 分页行为 |
| TC_SEARCH_PAGE_012 | switching page preserves search keyword and sort | P0 | 查询结果数足够触发分页。 | 1. 打开带分页的搜索结果<br>2. 切换页码<br>3. 检查 URL 与筛选条件保持情况 | 符合断言：switching page preserves search keyword and sort | 功能/分页 | search-page-full.spec.ts / 分页行为 |
| TC_SEARCH_PAGE_007 | previous page button is disabled on first page | P0 | 查询结果数足够触发分页。 | 1. 打开带分页的搜索结果<br>2. 切换页码<br>3. 检查 URL 与筛选条件保持情况 | 符合断言：previous page button is disabled on first page | 功能/分页 | search-page-full.spec.ts / 分页行为 |

### 4.16 搜索安全性

- 模块：搜索
- 自动化来源：`web/e2e/search-page-full.spec.ts`
- 场景说明：搜索安全性
- 公共前置条件：访问搜索页或可直接构造搜索 URL。
- 公共步骤：
  1. 输入 XSS、SQL 注入或篡改参数
  2. 执行搜索或直接访问 URL
  3. 观察页面渲染和行为
- 公共预期：系统应安全处理恶意输入，不执行脚本、不泄露错误栈，并对篡改参数做兼容处理。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TC_SEARCH_SEC_001 | XSS payload in search box is not executed | P0 | 访问搜索页或可直接构造搜索 URL。 | 1. 输入 XSS、SQL 注入或篡改参数<br>2. 执行搜索或直接访问 URL<br>3. 观察页面渲染和行为 | 符合断言：XSS payload in search box is not executed | 安全 | search-page-full.spec.ts / 搜索安全性 |
| TC_SEARCH_SEC_002 | SQL injection payload in search box is handled safely | P0 | 访问搜索页或可直接构造搜索 URL。 | 1. 输入 XSS、SQL 注入或篡改参数<br>2. 执行搜索或直接访问 URL<br>3. 观察页面渲染和行为 | 符合断言：SQL injection payload in search box is handled safely | 安全 | search-page-full.spec.ts / 搜索安全性 |
| TC_SEARCH_SEC_003 | tampered URL parameters are handled gracefully | P1 | 访问搜索页或可直接构造搜索 URL。 | 1. 输入 XSS、SQL 注入或篡改参数<br>2. 执行搜索或直接访问 URL<br>3. 观察页面渲染和行为 | 符合断言：tampered URL parameters are handled gracefully | 安全 | search-page-full.spec.ts / 搜索安全性 |

### 4.17 详情路由异常处理

- 模块：技能详情
- 自动化来源：`web/e2e/skill-detail-browse.spec.ts`
- 场景说明：详情路由异常处理
- 公共前置条件：可直接访问技能详情路由。
- 公共步骤：
  1. 访问不存在的 namespace 路由
  2. 访问不存在的技能详情路由
  3. 观察页面反馈
- 公共预期：非法详情路径应展示 not found 或等价错误页，而不是空白页或崩溃。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| SKILL_DETAIL_001 | shows not found for unknown namespace | P1 | 可直接访问技能详情路由。 | 1. 访问不存在的 namespace 路由<br>2. 访问不存在的技能详情路由<br>3. 观察页面反馈 | 符合断言：shows not found for unknown namespace | 功能/异常 | skill-detail-browse.spec.ts / 详情路由异常处理 |
| SKILL_DETAIL_002 | shows not found for unknown skill detail route | P1 | 可直接访问技能详情路由。 | 1. 访问不存在的 namespace 路由<br>2. 访问不存在的技能详情路由<br>3. 观察页面反馈 | 符合断言：shows not found for unknown skill detail route | 功能/异常 | skill-detail-browse.spec.ts / 详情路由异常处理 |

## 工作台与设置

### 5.1 个人模块入口

- 模块：Dashboard
- 自动化来源：`web/e2e/dashboard-personal-modules.spec.ts`
- 场景说明：个人模块入口
- 公共前置条件：用户已登录 dashboard。
- 公共步骤：
  1. 打开 dashboard
  2. 进入 stars 与 notifications 页面
  3. 观察页面跳转
- 公共预期：个人模块入口可正常打开并跳转到对应页面。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| DASH_PERSONAL_001 | opens stars page | P1 | 用户已登录 dashboard。 | 1. 打开 dashboard<br>2. 进入 stars 与 notifications 页面<br>3. 观察页面跳转 | 符合断言：opens stars page | 功能/导航 | dashboard-personal-modules.spec.ts / 个人模块入口 |
| DASH_PERSONAL_002 | opens notifications page | P1 | 用户已登录 dashboard。 | 1. 打开 dashboard<br>2. 进入 stars 与 notifications 页面<br>3. 观察页面跳转 | 符合断言：opens notifications page | 功能/导航 | dashboard-personal-modules.spec.ts / 个人模块入口 |

### 5.2 工作台主路由

- 模块：Dashboard
- 自动化来源：`web/e2e/dashboard-routes.spec.ts`
- 场景说明：工作台主路由
- 公共前置条件：用户已登录。
- 公共步骤：
  1. 访问 major dashboard 页面
  2. 访问 governance 与 namespace management 页面
  3. 观察页面渲染
- 公共预期：主要工作台路由均可正常打开。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| DASH_ROUTE_001 | opens major dashboard pages | P1 | 用户已登录。 | 1. 访问 major dashboard 页面<br>2. 访问 governance 与 namespace management 页面<br>3. 观察页面渲染 | 符合断言：opens major dashboard pages | 功能/导航 | dashboard-routes.spec.ts / 工作台主路由 |
| DASH_ROUTE_002 | opens governance and namespace management pages | P1 | 用户已登录。 | 1. 访问 major dashboard 页面<br>2. 访问 governance 与 namespace management 页面<br>3. 观察页面渲染 | 符合断言：opens governance and namespace management pages | 功能/导航 | dashboard-routes.spec.ts / 工作台主路由 |

### 5.3 工作台首页壳层

- 模块：Dashboard
- 自动化来源：`web/e2e/dashboard-shell.spec.ts`
- 场景说明：工作台首页壳层
- 公共前置条件：用户已登录并可访问 dashboard 首页。
- 公共步骤：
  1. 打开 dashboard 首页
  2. 检查账户摘要和快捷入口
- 公共预期：首页应展示账户摘要和常用快捷入口。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| DASH_SHELL_001 | renders account summary and quick links | P1 | 用户已登录并可访问 dashboard 首页。 | 1. 打开 dashboard 首页<br>2. 检查账户摘要和快捷入口 | 符合断言：renders account summary and quick links | UI/功能 | dashboard-shell.spec.ts / 工作台首页壳层 |

### 5.4 命名空间列表数据展示

- 模块：我的命名空间
- 自动化来源：`web/e2e/my-namespaces-data.spec.ts`
- 场景说明：命名空间列表数据展示
- 公共前置条件：已通过辅助请求创建命名空间，用户已登录。
- 公共步骤：
  1. 打开我的命名空间页面
  2. 观察新建命名空间是否展示
- 公共预期：我的命名空间列表应展示辅助工具创建的命名空间。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| MY_NS_DATA_001 | shows namespace created by request helper | P1 | 已通过辅助请求创建命名空间，用户已登录。 | 1. 打开我的命名空间页面<br>2. 观察新建命名空间是否展示 | 符合断言：shows namespace created by request helper | 功能/数据展示 | my-namespaces-data.spec.ts / 命名空间列表数据展示 |

### 5.5 我的技能数据展示

- 模块：我的技能
- 自动化来源：`web/e2e/my-skills-data.spec.ts`
- 场景说明：我的技能数据展示
- 公共前置条件：请求辅助工具已发布测试技能，用户已登录。
- 公共步骤：
  1. 打开 dashboard 我的技能列表
  2. 观察请求发布的技能是否出现
- 公共预期：我的技能列表应展示已发布的测试技能。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| MY_SKILL_DATA_001 | shows request-published skill in dashboard list | P0 | 请求辅助工具已发布测试技能，用户已登录。 | 1. 打开 dashboard 我的技能列表<br>2. 观察请求发布的技能是否出现 | 符合断言：shows request-published skill in dashboard list | 功能/数据展示 | my-skills-data.spec.ts / 我的技能数据展示 |

### 5.6 从列表进入详情并返回

- 模块：我的技能
- 自动化来源：`web/e2e/my-skills-navigation.spec.ts`
- 场景说明：从列表进入详情并返回
- 公共前置条件：我的技能列表中存在种子技能。
- 公共步骤：
  1. 从 dashboard 技能列表进入详情
  2. 再返回原列表页
  3. 检查上下文是否保留
- 公共预期：用户可从列表顺利进入详情并返回原页面。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| MY_SKILL_NAV_001 | opens seeded skill detail from dashboard list and returns back | P1 | 我的技能列表中存在种子技能。 | 1. 从 dashboard 技能列表进入详情<br>2. 再返回原列表页<br>3. 检查上下文是否保留 | 符合断言：opens seeded skill detail from dashboard list and returns back | 功能/导航 | my-skills-navigation.spec.ts / 从列表进入详情并返回 |

### 5.7 设置页访问与表单校验

- 模块：设置
- 自动化来源：`web/e2e/settings-pages.spec.ts`
- 场景说明：设置页访问与表单校验
- 公共前置条件：用户已登录并可访问设置中心。
- 公共步骤：
  1. 打开 profile settings
  2. 尝试提交缺少 current password 的修改
  3. 打开 notification settings
- 公共预期：设置页面可访问，缺少当前密码时有校验提示。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| SETTINGS_PAGE_001 | opens profile settings page | P1 | 用户已登录并可访问设置中心。 | 1. 打开 profile settings<br>2. 尝试提交缺少 current password 的修改<br>3. 打开 notification settings | 符合断言：opens profile settings page | 功能/UI | settings-pages.spec.ts / 设置页访问与表单校验 |
| SETTINGS_PAGE_002 | shows validation when current password is missing | P1 | 用户已登录并可访问设置中心。 | 1. 打开 profile settings<br>2. 尝试提交缺少 current password 的修改<br>3. 打开 notification settings | 符合断言：shows validation when current password is missing | 功能/UI | settings-pages.spec.ts / 设置页访问与表单校验 |
| SETTINGS_PAGE_003 | opens notification settings page | P1 | 用户已登录并可访问设置中心。 | 1. 打开 profile settings<br>2. 尝试提交缺少 current password 的修改<br>3. 打开 notification settings | 符合断言：opens notification settings page | 功能/UI | settings-pages.spec.ts / 设置页访问与表单校验 |

### 5.8 设置路由重定向

- 模块：设置
- 自动化来源：`web/e2e/settings-routing.spec.ts`
- 场景说明：设置路由重定向
- 公共前置条件：用户已登录。
- 公共步骤：
  1. 访问 accounts 路由
  2. 观察页面跳转结果
- 公共预期：accounts 路由应自动重定向到 security settings。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| SETTINGS_ROUTE_001 | redirects accounts route to security settings | P1 | 用户已登录。 | 1. 访问 accounts 路由<br>2. 观察页面跳转结果 | 符合断言：redirects accounts route to security settings | 功能/路由 | settings-routing.spec.ts / 设置路由重定向 |

### 5.9 Token 页面基础展示

- 模块：Token 管理
- 自动化来源：`web/e2e/tokens.spec.ts`
- 场景说明：Token 页面基础展示
- 公共前置条件：用户已登录并可访问 token 管理页。
- 公共步骤：
  1. 打开 token 管理页
  2. 检查页面主体和 create action
- 公共预期：token 管理页可正常渲染，且创建入口可见。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| TOKEN_001 | renders token management page and create action | P1 | 用户已登录并可访问 token 管理页。 | 1. 打开 token 管理页<br>2. 检查页面主体和 create action | 符合断言：renders token management page and create action | 功能/UI | tokens.spec.ts / Token 页面基础展示 |

### 5.10 Workspace 页面访问

- 模块：Workspace
- 自动化来源：`web/e2e/workspace-pages.spec.ts`
- 场景说明：Workspace 页面访问
- 公共前置条件：用户已登录。
- 公共步骤：
  1. 进入 workspace 区域
  2. 分别打开 my skills 和 my namespaces
- 公共预期：workspace 下相关页面可正常访问。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| WORKSPACE_001 | opens my skills and my namespaces pages | P1 | 用户已登录。 | 1. 进入 workspace 区域<br>2. 分别打开 my skills 和 my namespaces | 符合断言：opens my skills and my namespaces pages | 功能/导航 | workspace-pages.spec.ts / Workspace 页面访问 |

## 命名空间与治理

### 6.1 成员管理

- 模块：命名空间
- 自动化来源：`web/e2e/namespace-members-data.spec.ts`
- 场景说明：成员管理
- 公共前置条件：用户对目标命名空间拥有写权限。
- 公共步骤：
  1. 打开 members management 页面
  2. 观察页面内容
- 公共预期：具备权限的用户可以打开命名空间成员管理页面。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| NS_MEMBER_001 | opens members management for writable namespace | P1 | 用户对目标命名空间拥有写权限。 | 1. 打开 members management 页面<br>2. 观察页面内容 | 符合断言：opens members management for writable namespace | 功能/权限 | namespace-members-data.spec.ts / 成员管理 |

### 6.2 命名空间主页数据展示

- 模块：命名空间
- 自动化来源：`web/e2e/namespace-page-data.spec.ts`
- 场景说明：命名空间主页数据展示
- 公共前置条件：命名空间下存在通过请求种子的技能数据。
- 公共步骤：
  1. 打开目标 namespace 页面
  2. 观察技能上下文和页面信息
- 公共预期：命名空间主页应展示与种子技能相关的上下文数据。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| NS_PAGE_001 | shows namespace page with request-seeded skill context | P1 | 命名空间下存在通过请求种子的技能数据。 | 1. 打开目标 namespace 页面<br>2. 观察技能上下文和页面信息 | 符合断言：shows namespace page with request-seeded skill context | 功能/数据展示 | namespace-page-data.spec.ts / 命名空间主页数据展示 |

### 6.3 评审数据展示

- 模块：命名空间
- 自动化来源：`web/e2e/namespace-reviews-data.spec.ts`
- 场景说明：评审数据展示
- 公共前置条件：命名空间下存在种子评审数据。
- 公共步骤：
  1. 打开 namespace reviews 页面
  2. 观察评审上下文和相关内容
- 公共预期：评审页面应展示与命名空间和技能相关的评审数据。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| NS_REVIEW_001 | opens namespace reviews page with seeded review data context | P1 | 命名空间下存在种子评审数据。 | 1. 打开 namespace reviews 页面<br>2. 观察评审上下文和相关内容 | 符合断言：opens namespace reviews page with seeded review data context | 功能/数据展示 | namespace-reviews-data.spec.ts / 评审数据展示 |

## 公共页面与导航

### 7.1 首页搜索与发布入口

- 模块：首页
- 自动化来源：`web/e2e/landing-navigation.spec.ts`
- 场景说明：首页搜索与发布入口
- 公共前置条件：未登录访问首页。
- 公共步骤：
  1. 在 hero 区输入关键词并提交
  2. 从首页尝试发起 publish
  3. 观察页面跳转
- 公共预期：首页搜索会跳转到搜索页，匿名发布尝试会跳转登录。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| LANDING_NAV_001 | submits the hero search to the search page | P1 | 未登录访问首页。 | 1. 在 hero 区输入关键词并提交<br>2. 从首页尝试发起 publish<br>3. 观察页面跳转 | 符合断言：submits the hero search to the search page | 功能/导航 | landing-navigation.spec.ts / 首页搜索与发布入口 |
| LANDING_NAV_002 | redirects anonymous publish attempts to login | P0 | 未登录访问首页。 | 1. 在 hero 区输入关键词并提交<br>2. 从首页尝试发起 publish<br>3. 观察页面跳转 | 符合断言：redirects anonymous publish attempts to login | 功能/导航 | landing-navigation.spec.ts / 首页搜索与发布入口 |

### 7.2 法律文档页访问

- 模块：公共页面
- 自动化来源：`web/e2e/public-pages.spec.ts`
- 场景说明：法律文档页访问
- 公共前置条件：任意用户可访问公共法律页面。
- 公共步骤：
  1. 直接访问 privacy 页面
  2. 直接访问 terms 页面
  3. 观察文档渲染
- 公共预期：法律文档页面可直接打开并完整渲染。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| PUBLIC_PAGE_001 | renders privacy and terms documents directly | P2 | 任意用户可访问公共法律页面。 | 1. 直接访问 privacy 页面<br>2. 直接访问 terms 页面<br>3. 观察文档渲染 | 符合断言：renders privacy and terms documents directly | 功能/内容展示 | public-pages.spec.ts / 法律文档页访问 |

## 发布流程

### 8.1 Dashboard 发布技能包

- 模块：发布
- 自动化来源：`web/e2e/publish-flow-ui.spec.ts`
- 场景说明：Dashboard 发布技能包
- 公共前置条件：用户已登录 dashboard，且存在可上传的生成技能包。
- 公共步骤：
  1. 从 dashboard 发起发布
  2. 上传生成的技能包
  3. 完成发布并观察结果
- 公共预期：技能包可通过 dashboard UI 成功完成发布。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| PUBLISH_UI_001 | publishes a generated skill package from dashboard page | P0 | 用户已登录 dashboard，且存在可上传的生成技能包。 | 1. 从 dashboard 发起发布<br>2. 上传生成的技能包<br>3. 完成发布并观察结果 | 符合断言：publishes a generated skill package from dashboard page | 功能/主流程 | publish-flow-ui.spec.ts / Dashboard 发布技能包 |

### 8.2 发布审核通过与可见性

- 模块：发布审核
- 自动化来源：`Image #1`
- 场景说明：发布审核通过与可见性
- 公共前置条件：用户已登录并具备技能上传权限，管理员可执行审核通过操作。
- 公共步骤：
  1. 用户按公开、仅登录可见、私有三种类型上传技能
  2. 管理员执行审核通过
  3. 检查待审核卡片、我的技能列表、技能广场与详情页版本操作
- 公共预期：不同可见性技能审核通过后，均应生成待审核卡片并在审核完成后按预期展示已发布状态和可见范围。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| PUBLISH_REVIEW_001 | 用户上传公开技能并审核通过后，我的技能展示已发布，技能广场新增技能，详情页支持版本对比与重新发布 | P0 | 用户已登录并具备技能上传权限，管理员可执行审核通过操作。 | 1. 用户按公开、仅登录可见、私有三种类型上传技能<br>2. 管理员执行审核通过<br>3. 检查待审核卡片、我的技能列表、技能广场与详情页版本操作 | 用户技能创建待审核技能卡片；我的技能列表显示已发布；技能广场新增公开技能且所有人可见；详情页提供版本对比与重新发布操作。 | 功能/主流程 | Image #1 / 发布审核通过与可见性 |
| PUBLISH_REVIEW_004 | 用户上传仅登录用户可见技能并审核通过后，我的技能展示已发布，非登录用户不可见 | P0 | 用户已登录并具备技能上传权限，管理员可执行审核通过操作。 | 1. 用户按公开、仅登录可见、私有三种类型上传技能<br>2. 管理员执行审核通过<br>3. 检查待审核卡片、我的技能列表、技能广场与详情页版本操作 | 用户技能创建待审核技能卡片；我的技能列表显示已发布；未登录用户在技能广场或详情入口中不可见该技能。 | 功能/权限 | Image #1 / 发布审核通过与可见性 |
| PUBLISH_REVIEW_006 | 用户上传私有技能并审核通过后，我的技能展示已发布，技能广场仅发布者可见 | P0 | 用户已登录并具备技能上传权限，管理员可执行审核通过操作。 | 1. 用户按公开、仅登录可见、私有三种类型上传技能<br>2. 管理员执行审核通过<br>3. 检查待审核卡片、我的技能列表、技能广场与详情页版本操作 | 用户技能创建待审核技能卡片；我的技能列表显示已发布；技能广场仅发布者本人可见该私有技能。 | 功能/权限 | Image #1 / 发布审核通过与可见性 |

### 8.3 审核不通过与撤销

- 模块：发布审核
- 自动化来源：`Image #1`
- 场景说明：审核不通过与撤销
- 公共前置条件：用户已上传技能并生成待审核卡片。
- 公共步骤：
  1. 用户上传公开、仅登录可见或私有技能
  2. 管理员执行审核不通过，或用户主动撤销技能
  3. 检查我的技能列表的状态变化
- 公共预期：技能在审核不通过或被撤销后，应保留待审核上下文并在我的技能列表中展示对应状态。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| PUBLISH_REVIEW_002 | 用户上传公开技能审核不通过后，我的技能列表展示审核不通过 | P0 | 用户已上传技能并生成待审核卡片。 | 1. 用户上传公开、仅登录可见或私有技能<br>2. 管理员执行审核不通过，或用户主动撤销技能<br>3. 检查我的技能列表的状态变化 | 用户技能创建待审核技能卡片；公开技能在审核不通过后，我的技能列表明确展示审核不通过。 | 功能/状态流转 | Image #1 / 审核不通过与撤销 |
| PUBLISH_REVIEW_003 | 用户上传技能后主动撤销，我的技能列表状态展示已撤销 | P1 | 用户已上传技能并生成待审核卡片。 | 1. 用户上传公开、仅登录可见或私有技能<br>2. 管理员执行审核不通过，或用户主动撤销技能<br>3. 检查我的技能列表的状态变化 | 用户技能创建待审核技能卡片；用户撤销后，我的技能列表状态更新为已撤销。 | 功能/状态流转 | Image #1 / 审核不通过与撤销 |
| PUBLISH_REVIEW_005 | 用户上传仅登录用户可见技能审核不通过后，我的技能列表展示审核不通过 | P0 | 用户已上传技能并生成待审核卡片。 | 1. 用户上传公开、仅登录可见或私有技能<br>2. 管理员执行审核不通过，或用户主动撤销技能<br>3. 检查我的技能列表的状态变化 | 用户技能创建待审核技能卡片；仅登录用户可见技能在审核不通过后，我的技能列表展示审核不通过。 | 功能/状态流转 | Image #1 / 审核不通过与撤销 |
| PUBLISH_REVIEW_007 | 用户上传私有技能审核不通过后，我的技能列表展示审核不通过 | P0 | 用户已上传技能并生成待审核卡片。 | 1. 用户上传公开、仅登录可见或私有技能<br>2. 管理员执行审核不通过，或用户主动撤销技能<br>3. 检查我的技能列表的状态变化 | 用户技能创建待审核技能卡片；私有技能在审核不通过后，我的技能列表展示审核不通过。 | 功能/状态流转 | Image #1 / 审核不通过与撤销 |

### 8.4 归档与版本删除

- 模块：版本管理
- 自动化来源：`Image #1`
- 场景说明：归档与版本删除
- 公共前置条件：技能已进入已发布、审核不通过或撤销发布等可操作状态。
- 公共步骤：
  1. 对已发布技能执行归档，或对指定版本执行删除
  2. 检查技能广场是否可见、是否可下载，以及列表标签和删除按钮状态
- 公共预期：归档和删除动作应受技能状态与版本数量约束，并准确反馈到技能广场和我的技能列表。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| PUBLISH_LIFECYCLE_001 | 技能归档后，技能广场不可见、不可下载，我的技能列表显示已归档标签 | P1 | 技能已进入已发布、审核不通过或撤销发布等可操作状态。 | 1. 对已发布技能执行归档，或对指定版本执行删除<br>2. 检查技能广场是否可见、是否可下载，以及列表标签和删除按钮状态 | 已归档技能在技能广场中不可见且不可下载；我的技能列表对该技能展示已归档标签。 | 功能/生命周期 | Image #1 / 归档与版本删除 |
| PUBLISH_VERSION_001 | 审核不通过版本和撤销发布版本可删除，但仅剩一个版本时不允许删除 | P1 | 技能已进入已发布、审核不通过或撤销发布等可操作状态。 | 1. 对已发布技能执行归档，或对指定版本执行删除<br>2. 检查技能广场是否可见、是否可下载，以及列表标签和删除按钮状态 | 审核不通过版本可删除；撤销发布版本可删除；当技能仅剩一个版本时，不允许删除该版本。 | 功能/版本管理 | Image #1 / 归档与版本删除 |

### 8.5 重新发布与版本可见性

- 模块：版本管理
- 自动化来源：`Image #1`
- 场景说明：重新发布与版本可见性
- 公共前置条件：技能已存在至少一个已发布版本，且支持重新发布。
- 公共步骤：
  1. 对已发布版本执行重新发布
  2. 在多版本场景下切换公开与私有可见性
  3. 检查当前版本标识和技能广场可见范围
- 公共预期：多版本技能应以当前生效版本的状态决定展示版本和外部可见性。

| 用例ID | 用例标题 | 优先级 | 前置条件 | 测试步骤 | 预期结果 | 类型 | 自动化来源 |
|---|---|---|---|---|---|---|---|
| PUBLISH_VERSION_002 | 已发布版本重新发布后，审核通过版本展示为当前版本 | P1 | 技能已存在至少一个已发布版本，且支持重新发布。 | 1. 对已发布版本执行重新发布<br>2. 在多版本场景下切换公开与私有可见性<br>3. 检查当前版本标识和技能广场可见范围 | 已发布版本重新发布并审核通过后，页面展示的新版本应被标记为当前版本。 | 功能/版本管理 | Image #1 / 重新发布与版本可见性 |
| PUBLISH_VERSION_003 | 已发布公开版本重新发布为私有版本后，多版本场景下应以当前版本状态决定技能可见性 | P1 | 技能已存在至少一个已发布版本，且支持重新发布。 | 1. 对已发布版本执行重新发布<br>2. 在多版本场景下切换公开与私有可见性<br>3. 检查当前版本标识和技能广场可见范围 | 多个版本共存时，应以当前生效版本状态决定可见性；当前版本切换为私有后，技能不再对公开用户可见，仅对具备权限的用户可见。 | 功能/权限 | Image #1 / 重新发布与版本可见性 |

## 附录

- 分类场景数：48
- 明细用例数：126
- 建议后续动作：
  1. 如需导入 Jira / 禅道，可继续转换为 CSV。
  2. 如需执行版用例，可在此基础上补充“测试数据”“执行结果”“缺陷编号”。
  3. 搜索、认证、发布三类场景建议优先纳入回归集。
