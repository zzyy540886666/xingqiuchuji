# Parent Agent Dispatch

## Objective
Coordinate implementation-ready tasks after auditing `docs/星球出机小程序需求文档 V1.docx` against the current project.

## Parent Agent Decisions
- Do not delegate tasks requiring external credentials or production/service-provider decisions.
- Delegate only disjoint code-write scopes.
- Avoid repeating old gap-list facts without verifying current code; several old P0 items are now fixed.

## Spawned Workers

| Worker | Agent ID | Scope | Result |
| --- | --- | --- | --- |
| A / Feynman | `019eaf74-6e25-7053-8092-d5253ec3357e` | Frontend order status and fulfillment display contract | Notification reported 429, but workspace contained partial task changes; main thread reviewed, added `pay-result.vue` cleanup, and validated with frontend static check |
| B / Heisenberg | `019eaf76-08c9-74f1-aa09-cd0fbfaadb45` | Backend membership default rules and focused tests | Completed; focused backend test passed |
| C / Socrates | `019eaf77-bc5f-7f51-ab5f-c6d38dbd43e8` | Real analytics persistence/admin aggregation and Product.vue integration | Failed with 429 Too Many Requests before producing code |

## Implementable Task Backlog

### TASK-A: Frontend Order Contract Cleanup
- Files: `frontend/src/pages/order/detail.vue`, `frontend/src/pages/order/list.vue`, `frontend/src/services/order.ts`, `frontend/src/types/order.ts`.
- Fix `IN_SERVICE` vs backend `FULFILLING`.
- Avoid buttons that navigate to missing backend-backed flows.
- Preserve contract download.
- Status: implemented in workspace and verified by `npm.cmd run type-check`.

### TASK-B: Membership Default Rules
- Files: `backend/src/main/java/com/xingqiu/server/appconfig/service/ConfigService.java`, `backend/src/main/java/com/xingqiu/server/member/service/MemberService.java`, focused tests.
- Align defaults with Lv1 first paid order / 99 yuan card, Lv2 5000 yuan, Lv3 20000 yuan.
- Preserve backend config override behavior.
- Status: implemented in workspace and verified by `mvn -q -Dtest=MemberServiceTest test`.

### TASK-C: Product Analytics Real Data
- Files: `backend/src/main/java/com/xingqiu/server/analytics/**`, admin analytics controller if needed, `admin-web/src/modules/analytics/Product.vue`, SQL only if needed.
- Persist accepted analytics events.
- Add admin exposure/funnel aggregation endpoints.
- Remove Product.vue mock constants.
- Status: still pending; worker failed with 429 and no code output was observed.

### Deferred / Not Locally Implementable
- Real WeChat Pay configuration and callback acceptance.
- WeChat transfer-to-balance settlement.
- IM provider integration without selected provider credentials.
- Final operations rules for commissions, points, and benefits.
