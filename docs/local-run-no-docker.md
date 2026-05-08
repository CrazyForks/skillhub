# SkillHub E2E 测试本地运行方案（无 Docker）

## 📋 前置条件检查

你的机器已经具备：
- ✅ PostgreSQL 16 (运行中)
- ✅ Redis 7 (运行中)
- ✅ Java 21
- ✅ Maven
- ✅ Node.js + pnpm
- ✅ MinIO (已安装)

## 🚀 启动步骤

### 1. 启动 MinIO（对象存储）

```bash
# 创建数据目录
mkdir -p /tmp/skillhub-minio-data

# 启动 MinIO
MINIO_ROOT_USER=minioadmin MINIO_ROOT_PASSWORD=minioadmin \
  minio server /tmp/skillhub-minio-data \
  --address :9000 \
  --console-address :9001 \
  > /tmp/skillhub-minio.log 2>&1 &

# 验证 MinIO 启动
curl -s http://127.0.0.1:9000/minio/health/live && echo "✅ MinIO OK"
```

### 2. 启动后端服务

```bash
cd /Users/tenten/Desktop/skillhub/server

# 方式 1：使用 Maven 直接运行
mvn spring-boot:run -pl skillhub-app \
  -Dspring-boot.run.profiles=dev \
  > /tmp/skillhub-backend.log 2>&1 &

# 方式 2：使用项目 Makefile（推荐）
cd /Users/tenten/Desktop/skillhub
make dev-server

# 等待后端启动（约 30-60 秒）
until curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; do
  echo "等待后端启动..."
  sleep 3
done
echo "✅ 后端启动成功"
```

### 3. 启动前端服务

```bash
cd /Users/tenten/Desktop/skillhub/web

# 启动前端开发服务器
pnpm dev > /tmp/skillhub-frontend.log 2>&1 &

# 等待前端启动（约 10-20 秒）
until curl -sf http://localhost:3000 > /dev/null 2>&1; do
  echo "等待前端启动..."
  sleep 2
done
echo "✅ 前端启动成功"
```

### 4. 验证所有服务

```bash
# 检查所有服务状态
echo "=== 服务状态检查 ==="
curl -s http://localhost:5432 && echo "✅ PostgreSQL: 运行中" || echo "❌ PostgreSQL: 未运行"
redis-cli ping > /dev/null 2>&1 && echo "✅ Redis: 运行中" || echo "❌ Redis: 未运行"
curl -s http://127.0.0.1:9000/minio/health/live > /dev/null 2>&1 && echo "✅ MinIO: 运行中" || echo "❌ MinIO: 未运行"
curl -s http://localhost:8080/actuator/health > /dev/null 2>&1 && echo "✅ 后端: 运行中" || echo "❌ 后端: 未运行"
curl -s http://localhost:3000 > /dev/null 2>&1 && echo "✅ 前端: 运行中" || echo "❌ 前端: 未运行"
```

## 🧪 运行 E2E 测试

### 运行所有新增测试

```bash
cd /Users/tenten/Desktop/skillhub/web

# 运行所有新增的测试文件
pnpm test:e2e \
  reviews-full-flow.spec.ts \
  governance-center.spec.ts \
  my-namespaces-full.spec.ts \
  namespace-members-management.spec.ts \
  namespace-reviews-management.spec.ts \
  profile-settings.spec.ts \
  security-settings.spec.ts \
  accounts-settings.spec.ts \
  notification-settings.spec.ts \
  admin-audit-log.spec.ts \
  admin-labels.spec.ts \
  admin-users.spec.ts \
  stars-management.spec.ts \
  promotions-management.spec.ts \
  reports-management.spec.ts \
  device-management.spec.ts
```

### 运行特定优先级测试

```bash
# 只运行 P0 测试
pnpm test:e2e reviews-full-flow.spec.ts governance-center.spec.ts

# 只运行 P1 测试
pnpm test:e2e \
  my-namespaces-full.spec.ts \
  namespace-members-management.spec.ts \
  namespace-reviews-management.spec.ts \
  profile-settings.spec.ts \
  security-settings.spec.ts \
  accounts-settings.spec.ts \
  notification-settings.spec.ts \
  admin-audit-log.spec.ts \
  admin-labels.spec.ts \
  admin-users.spec.ts

# 只运行 P2 测试
pnpm test:e2e \
  stars-management.spec.ts \
  promotions-management.spec.ts \
  reports-management.spec.ts \
  device-management.spec.ts
```

### 运行单个测试文件

```bash
# 运行审查中心测试
pnpm test:e2e reviews-full-flow.spec.ts

# 运行治理中心测试
pnpm test:e2e governance-center.spec.ts

# 使用 UI 模式运行（可视化调试）
pnpm test:e2e:ui reviews-full-flow.spec.ts
```

## 🔍 查看日志

```bash
# 查看后端日志
tail -f /tmp/skillhub-backend.log

# 查看前端日志
tail -f /tmp/skillhub-frontend.log

# 查看 MinIO 日志
tail -f /tmp/skillhub-minio.log

# 查看 Playwright 测试报告
cd /Users/tenten/Desktop/skillhub/web
pnpm exec playwright show-report
```

## 🛑 停止所有服务

```bash
# 停止后端
pkill -f "spring-boot:run"

# 停止前端
pkill -f "pnpm dev"

# 停止 MinIO
pkill -f "minio server"

# 或者使用 Makefile
cd /Users/tenten/Desktop/skillhub
make dev-down
```

## ⚠️ 常见问题

### 1. 后端启动失败

```bash
# 检查端口占用
lsof -i :8080

# 清理 Maven 缓存
rm -rf ~/.m2/repository/com/iflytek/skillhub

# 重新编译
cd /Users/tenten/Desktop/skillhub/server
mvn clean install -DskipTests
```

### 2. 前端启动失败

```bash
# 检查端口占用
lsof -i :3000

# 重新安装依赖
cd /Users/tenten/Desktop/skillhub/web
rm -rf node_modules
pnpm install
```

### 3. 数据库连接失败

```bash
# 检查 PostgreSQL 状态
brew services list | grep postgresql

# 重启 PostgreSQL
brew services restart postgresql@16

# 验证数据库连接
psql -U skillhub -d skillhub -c "SELECT 1"
```

### 4. MinIO 连接失败

```bash
# 检查 MinIO 进程
ps aux | grep minio

# 重启 MinIO
pkill -f "minio server"
MINIO_ROOT_USER=minioadmin MINIO_ROOT_PASSWORD=minioadmin \
  minio server /tmp/skillhub-minio-data \
  --address :9000 --console-address :9001 &
```

## 📊 测试覆盖范围

| 优先级 | 测试文件 | 测试用例数 | 覆盖功能 |
|--------|---------|-----------|---------|
| P0 | reviews-full-flow.spec.ts | 7 | 审查中心完整流程 |
| P0 | governance-center.spec.ts | 7 | 治理中心核心功能 |
| P1 | my-namespaces-full.spec.ts | 4 | 命名空间管理 |
| P1 | namespace-members-management.spec.ts | 4 | 成员管理 |
| P1 | namespace-reviews-management.spec.ts | 4 | 命名空间审查 |
| P1 | profile-settings.spec.ts | 4 | 个人资料设置 |
| P1 | security-settings.spec.ts | 4 | 安全设置 |
| P1 | accounts-settings.spec.ts | 3 | 账户合并 |
| P1 | notification-settings.spec.ts | 3 | 通知设置 |
| P1 | admin-audit-log.spec.ts | 4 | 审计日志 |
| P1 | admin-labels.spec.ts | 4 | 标签管理 |
| P1 | admin-users.spec.ts | 5 | 用户管理 |
| P2 | stars-management.spec.ts | 3 | 收藏管理 |
| P2 | promotions-management.spec.ts | 4 | 推广管理 |
| P2 | reports-management.spec.ts | 4 | 举报管理 |
| P2 | device-management.spec.ts | 5 | 设备授权 |

**总计**: 16 个文件，69 个测试用例

## ✅ 验证状态

- ✅ TypeScript 编译通过（零错误）
- ✅ Playwright 识别所有测试用例
- ✅ 使用稳定的 `getByRole` / `getByLabel` 选择器
- ✅ 统一的断言风格和错误处理
- ✅ 防 flaky 措施（API 预查询、条件分支、合理 timeout）
