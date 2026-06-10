# Task Plan

## Goal
学习 `D:\Pro\screenshot-to-code` 中可复用的连接层和状态管理做法，在不大改当前小程序/网站业务设计的前提下，对本项目做小范围优化。

## Assumptions
- 当前项目保持 UniApp 小程序、Vue 管理后台、Spring Boot 后端三端结构不变。
- 不迁移框架，不新增生产依赖，不改业务接口语义。
- 优先优化小程序和管理后台共用后端数据时的可观测性和联调一致性。

## Likely Affected Files
- `frontend/src/utils/request.ts`
- `backend/src/main/java/com/xingqiu/server/common/filter/TraceIdFilter.java`
- `backend/src/main/java/com/xingqiu/server/common/util/TraceIdUtil.java`

## Milestones
1. 读取当前项目说明、脚本和请求层 - completed
2. 读取 `screenshot-to-code` 前后端连接层与状态处理 - completed
3. 选定低风险优化点 - completed
4. 实现小程序到后端 traceId 贯通 - completed
5. 运行聚焦验证 - completed

## Validation Plan
- Frontend: `npm.cmd run build:mp-weixin` in `frontend`.
- Backend: run focused Maven test/compile command in `backend`.
- Static check: inspect modified files and verify no unrelated behavior change.

## Risks
- 工作区已有大量未提交改动；只修改本任务必要文件。
- `rg` 当前不可用，搜索使用 PowerShell `Get-ChildItem` + `Select-String`。

## Validation Results
- Backend focused: `mvn.cmd -Dtest=TraceIdFilterTest test` passed, 2 tests.
- Backend full: `mvn.cmd test` passed, 11 tests, 0 failures/errors.
- Frontend: sandboxed `npm.cmd run build:mp-weixin` failed with Vite temp config `EPERM`; reran outside sandbox with approval and it passed.
