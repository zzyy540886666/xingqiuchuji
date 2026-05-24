# CI 集成与结果解读

## 本地执行

### Maven

```bash
# 运行全部测试
mvn test

# 运行指定模块测试
mvn test -pl module-name

# 运行指定测试类
mvn test -Dtest=ClassNameTest

# 运行指定测试方法
mvn test -Dtest=ClassNameTest#methodName
```

### Gradle

```bash
./gradlew test
./gradlew test --tests "com.example.ClassNameTest"
```

## CI 输出解读

CI 必须输出以下三个可程序化读取的字段：

| 字段 | 含义 | 通过条件 |
|------|------|---------|
| `status` | CI 整体状态 | 必须为 `SUCCESS` |
| `tests` | 测试总数 | 必须 `> 0` |
| `passed` | 通过数量 | 必须 `= total`（即 tests） |

### 通过示例

```
status: SUCCESS
tests: 42
passed: 42
failed: 0
```

### 失败示例（tests=0）

```
status: FAILURE
tests: 0
passed: 0
failed: 0
```

→ 回退到单测编写阶段

### 失败示例（有测试未通过）

```
status: FAILURE
tests: 42
passed: 38
failed: 4
```

→ 回退到编码实现阶段修复

## 产出 `05-CI结果.md`

CI 执行后必须写入 `changes/<需求ID>/05-CI结果.md`，包含：

```markdown
# CI 执行结果

| 字段 | 值 |
|------|-----|
| status | SUCCESS/FAILURE |
| tests | 数量 |
| passed | 数量 |
| failed | 数量 |

## 失败用例明细（如有）

### 1. TestClassName.methodName

- 失败原因：...
- 复现步骤：...
- 最短修复路径：...
```

## 门禁规则

- CI 非 SUCCESS 禁止继续进入部署阶段
- tests=0 回退到单测编写阶段
- 有测试未通过回退到编码实现阶段
- 任何条件不满足不允许带病进入部署
