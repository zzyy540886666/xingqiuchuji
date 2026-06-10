# Task Plan

## Goal
补全前端用户侧数据链路，重点是“我的”页面及其入口功能，不再依赖 mock 或设计兜底数据。

## Assumptions
- 以后端现有 Spring Boot/MyBatis-Plus 领域模型和 `docs/api/openapi/openapi.yaml` 为接口真源。
- 低风险处优先复用已有接口；只有确实缺少接口或字段时才补后端。
- 未登录状态可以显示空值或登录入口，但不能显示假资产、假邀请码、假订单或假收益。

## Likely Affected Files
- `frontend/src/pages/profile/index.vue`
- `frontend/src/pages/asset/earnings.vue`
- `frontend/src/pages/repair/mine.vue`
- `frontend/src/pages/search/index.vue`
- `frontend/src/pages/post/create.vue`
- `frontend/src/services/*`
- `backend/src/main/java/com/xingqiu/server/**`
- `backend/src/main/resources/**`
- `docs/api/openapi/openapi.yaml`

## Milestones
1. Inventory frontend mock/fallback usage and “我的” page dependencies - completed
2. Map required frontend data to existing backend endpoints - completed
3. Implement missing backend fields/endpoints with existing domain patterns - completed
4. Replace mock/fallback data in frontend with backend calls and empty/error states - completed
5. Run focused backend and frontend checks - completed

## Validation Plan
- Frontend: run the configured build/static check for the miniapp.
- Backend: run Maven tests or a focused compile/test command from `backend`.
- Search verification: scan source for remaining `mock`, `Mock`, `MOCK`, and design fallback data in runtime code.

## Risks
- Some files contain mojibake Chinese text; avoid broad text rewrites unless required for the data task.
- Existing database seed/schema may not contain every table needed for UI entries; verify mapper queries before adding code.
- Payment, external COS/TMS, and production credentials are out of scope; do not introduce real external calls for this task.

## Validation Results
- Backend: `mvn clean test` in `backend` passed, 9 tests, 0 failures/errors.
- Frontend: `npm.cmd run build:mp-weixin` in `frontend` passed after rerunning outside the sandbox because the sandboxed run hit Vite temp config `EPERM`.
- Search verification: PowerShell source scans found no remaining `mock/Mock/MOCK/模拟/假数据/本地生成/pseudo/QR-like` hits in frontend runtime source or backend main Java source.
- Build artifact check: `dist/build/mp-weixin/pages/asset/earnings.js` and `dist/build/mp-weixin/pages/post/create.js` exist after build.

## Decisions
- Use `/api/v1/assets/earnings` as the user-level asset earnings endpoint instead of calling every asset dashboard from the earnings page.
- Treat unauthenticated profile as real empty state/login entry; do not show fake wallet, invite, asset, or revenue values.
- Software order delivery must come from backend response fields; frontend must not derive activation codes locally.
