# Requirements Audit Findings

## Requirement Source Notes
- Source: `docs/星球出机小程序需求文档 V1.docx`; extracted title says `星球出机小程序需求文档 V1.1（更新）`.
- Scope: user-side robot rental / purchase / software trading platform, with "星球分身经济" and membership/community. Merchant management, operation config, and content moderation back office are explicitly out of the requirement scope, but user app must read dynamic configuration from backend.
- Core transaction requirements:
  - Home/global navigation with three product entries: rental, purchase, software.
  - Rental/buying categories: humanoid, quadruped robot dog, bionic humanoid, filterable by brand/price.
  - Software categories: dance packages, interactive scripts, voice packs, sport algorithm packages, filterable by compatible robot model.
  - Scenario entry: annual meeting, expo, store, concert, variety show; aggregate service bundles.
  - Multi-dimensional search by product name, scenario, brand, price range, and compatible robot model.
  - Unified product detail template: media, specs, compatibility, tiered rental price, sale price/installment notes, software license/subscription, QA, smart action buttons.
  - Unified order flow: select product, fill rental period/address/license, calculate fees with coupon/light-year coin, WeChat Pay deposit/full payment, generate order and electronic contract.
  - Order statuses: pending payment, paid/pending fulfillment, fulfilling/shipped, completed, canceled.
  - Order center tabs: rental/buy/software; status filter; renewal/cancel/logistics/confirm receipt/after-sales/activation code/download link by type.
- Membership/avatar economy:
  - First paid rental/purchase/software makes user Lv.1 "原住民"; lifetime, no downgrade.
  - Purchased robots enter "我的分身资产"; can manage hosted rental.
  - Hosted rental board: availability window, platform pricing, status, cumulative income, rental records.
  - Wallet: light-year wallet, withdraw to WeChat balance, traceable statements.
  - Membership levels Lv.0-Lv.3, discounts and rights; paid upgrade by WeChat Pay; rules/rights are backend-configurable.
- Community/distribution:
  - Community posts with images/video, preset topics, circles, private messages/group chat, profile, badges/certification, points rewards.
  - Distribution center with invitation poster/link/code, team board, tree graph, commission bills, T+7 withdrawal, freeze hints, leader rewards.
- Non-functional/business requirements: WeChat Pay, enterprise payment withdrawal, electronic contract preview/download, dynamic operation config, anti-fraud distribution detection, hosting conflict lock, order auto-cancel, reserved analytics.

## Implementation Findings
- Current project structure: UniApp mini program in `frontend/`, Spring Boot backend in `backend/`, admin web in `admin-web/`.
- Root `package.json` exposes `harness:ci`, `type-check`, and `test:unit`; frontend/admin/backend each also have their own scripts/build files.
- Existing 2026-06-06 gap doc is useful but partly stale. Verified stale items:
  - SOFTWARE order mapping is fixed in `frontend/src/pages/product-detail/index.vue`.
  - TMS failure now routes to `MANUAL_REVIEW` in `TmsModerationClient.java`.
  - Order detail has contract download in `frontend/src/pages/order/detail.vue`.
  - Trusteeship pricing now uses platform config in frontend and backend.
  - Trusteeship scheduled inspection now calls `trusteeshipService.inspectTrusteeships()` and generates revenue ledgers.
  - Community like/collect/follow/comment now have backend services and frontend calls.
  - Distribution invite poster page exists and draws a canvas poster.
- Still-valid current gaps:
  - WeChat Pay env defaults are empty and notify URL defaults to localhost in `backend/src/main/resources/application.yml`.
  - Planet card purchase still uses wallet debit in `MemberService.purchasePlanetCard`, not WeChat Pay.
  - Frontend order detail/list still need backend status alignment (`FULFILLING` vs `IN_SERVICE`) and should not imply unavailable fulfillment APIs are complete.
  - `OrderResponse` lacks real logistics/software delivery fields even though frontend service type declares them.
  - Membership default rules still use low thresholds (100/500/2000 yuan equivalents) instead of doc defaults (first paid order, 5000, 20000).
  - IM pages use notification APIs rather than real conversations/messages.
  - Product analytics admin page still uses mock constants; backend analytics accepts events but does not persist or aggregate.
  - Automated tests are limited: backend currently has 5 test classes; frontend/admin no obvious tests.

## Gaps
See `docs/需求实现核对与子agent分发-20260610.md`.

## Delegation Notes
- Spawned worker A for frontend order contract cleanup. It reported 429, but partial changes appeared in the workspace; main thread reviewed and completed the remaining `pay-result.vue` state cleanup.
- Spawned worker B for membership default rules and tests. It completed; focused backend test passed.
- Spawned worker C for real product analytics. It failed with 429 before producing code.
