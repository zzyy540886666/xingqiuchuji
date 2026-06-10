# Findings

## Current Findings
- Frontend and backend are separate workspace folders: `frontend` is a Uni/Vite app, `backend` is Spring Boot 3.2.5 with MyBatis-Plus.
- `rg` cannot run in this environment due `Access is denied`; use PowerShell `Get-ChildItem` plus `Select-String`.
- Existing backend domains already cover many “我的” page targets: auth/user, order, wallet, member, distribution, asset, repair/workorder, community.
- Frontend mock/fallback hits found so far:
  - `frontend/src/pages/profile/index.vue`: unauthenticated clone stats fallback, hard-coded invite fallback, hard-coded membership expiry/progress/privilege values.
  - `frontend/src/pages/asset/earnings.vue`: explicit mock data block.
  - `frontend/src/pages/repair/mine.vue`: explicit mock data block.
  - `frontend/src/pages/post/create.vue`: comment and UI media list mock wording.
  - `frontend/src/pages/search/index.vue`: fallback design mock branches.
  - `frontend/src/services/order.ts`: comments indicate UI fields were mock support.

## Open Questions Resolved By Inspection
- Membership endpoint already returns `level`, `isNative`, `lifetimeSpendMinor`, `planetCardExpiresAt`, and `benefits`; profile page can derive expiry, progress, and benefit counts from real data.
- Work-order backend and frontend service already exist; `repair/mine.vue` can use `getWorkOrders()` and derive status counts locally.
- Asset revenue has `asset_revenue_ledger` and per-asset dashboard, but no user-level aggregate endpoint. Add a focused `/api/v1/assets/earnings` endpoint instead of making the page call every asset dashboard.
- `asset/list.vue` currently calls `getAssetDashboard()` per asset; it can keep working, but a user-level summary endpoint will simplify `asset/earnings.vue` and `profile/index.vue`.
- Current working tree already contains a backend `/api/v1/assets/earnings` implementation and `AssetEarningsResponse` DTO; added OpenAPI schema and frontend page wiring instead of replacing those files.
- `TrusteeshipService#createTrusteeship` inserted an active trusteeship slot but did not update the asset status to `TRUSTEED`; this made profile clone stats and asset-list tabs inaccurate after托管.
- `frontend/src/pages/order/detail.vue` generated a deterministic software activation code from the order ID. Removed this local generation; software delivery now displays only backend-provided fields.
- `frontend/src/pages/asset/earnings.vue` was referenced in `pages.json` but missing from disk, so profile/asset entry navigation could land on a non-existent page.
