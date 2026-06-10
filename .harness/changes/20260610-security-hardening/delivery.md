# 安全输入与接口加固交付记录

## 目标与范围

- 目标：在不破坏现有正常业务功能的前提下，从输入校验、SQL 注入、XSS、鉴权绕过、接口滥用、金额/订单状态篡改和资源越权角度，对小程序、管理网站和后端做第一轮生产级加固。
- 范围：`backend/`、`frontend/`、`admin-web/` 中可通过静态审计明确定位且低破坏风险的安全问题。
- 不做：不接入新的外部服务，不改生产部署拓扑，不重写大模块，不引入未经必要性证明的新生产依赖。

## 风险触点与关键决策

- 仓库存在大量既有未提交修改，本轮只修改经确认的目标文件，不回滚或格式化无关文件。
- 后端是安全真源；前端和管理端只做输入收敛与展示层防护，不能替代服务端鉴权、金额计算或状态机。
- 接口契约以 `docs/api/openapi/openapi.yaml` 和实际 Controller/DTO 为准；若本轮改变公开字段或状态语义，必须同步文档和调用端。
- 对不确定是否会影响业务兼容的高风险收紧项，先记录风险，不直接破坏性修改。
- 订单预览接口从“接受客户端自报优惠但封顶”收紧为“正数客户端自报优惠直接拒绝”。当前小程序确认页已不再提交这些字段，主流程不受影响；旧客户端或外部调用方若继续提交正数抵扣会失败，这是有意的安全收紧，需要发布说明。
- `backend/src/main/resources/application-dev.yml`、`START.md`、`start-dev.ps1` 和旧部署记录中曾存在本地开发 JWT/微信配置明文值；本轮已改为环境变量或示例占位。由于值已经在工作区暴露过，不能视为仅改文件即完成补救，仍必须轮换相关密钥/凭证。
- 生产 profile 不再为数据库、Redis、JWT 提供弱默认值，缺少环境变量时应 fail-fast；开发 profile 保留本地默认值以维持本地启动便利。

## 审计记录

- 2026-06-10：`rg` 在当前环境无法运行（拒绝访问），改用 PowerShell `Get-ChildItem -Recurse | Select-String` 扫描。
- 后端 `SecurityConfig` 原先放开 `/api/v1/internal/**` 和 `/api/v1/analytics/**`，实际公开需求仅有微信支付回调和小程序事件上报。
- 社区发帖/评论 DTO 已有 Bean Validation 注解，但 Controller 未加 `@Valid`，导致长度/必填校验不会执行。
- 二维码解析接口使用裸 `Map<String,String>`，缺少长度和格式边界。
- 埋点接口使用裸 `Map<String,Object>`，只按 List size 计数，缺少事件数量、名称长度、参数数量边界。
- 订单预览/创建接口接收 `couponDiscountMinor`、`lightYearDiscountMinor`，前端确认页用本地常量提交抵扣金额；服务端仅做非负和封顶，属于可篡改支付金额入口。
- 前端/管理端未发现 `v-html`、`rich-text`、`innerHTML`、`eval`、`new Function` 命中；令牌存储命中属于现有登录态机制，本轮未改变。
- SQL 注入静态扫描未发现 MyBatis `${}` 动态拼接；命中项均为 Spring `@Value("${...}")` 配置占位符。
- 上传接口已有扩展名和 MIME 基础校验，但缺少文件头魔数校验；公开 `/uploads/**` 目录会放大伪装文件的 XSS/内容嗅探风险。
- `harness:ci` 首次失败在首页刷新竞态门禁：实现已具备 `createLatestTask()`，但变量名不符合门禁要求的 `productRequestId` 契约。
- 独立核对 Agent `019ead6e-d9a4-7e01-9775-0336e6cd4ac8` 复核发现 5 个问题：公开社区详情/评论可能泄露未审核内容、`CircleService.last("LIMIT ...")` 拼接 SQL 片段、关闭 `nosniff`、未知字段拒绝未落地、订单分页缺少控制器级范围校验。后续均已按本记录修复或补充验证。
- 独立核对 Agent 二次复核指出：自定义 `JacksonConfig` 可能绕过 YAML 未知字段拒绝、生产配置仍有弱默认值。已显式启用 `FAIL_ON_UNKNOWN_PROPERTIES`，补测试，并收紧 `application-prod.yml`。

## 实际改动

- 收窄公开安全白名单：`/api/v1/internal/**` 改为仅允许 `POST /api/v1/internal/pay/wechat/notify`；`/api/v1/analytics/**` 改为仅允许 `POST /api/v1/analytics/events`。
- 新增 `AnalyticsEventRequest`，对单次事件数量、事件名、页面路径和参数项数加 Bean Validation。
- 新增 `ResolveQrCodeRequest`，对二维码内容加非空、长度和字符白名单校验。
- 社区发帖和评论入口启用 `@Valid`。
- 订单预览/创建 DTO 补充 `skuId`、`orderType`、`address`、`idempotencyKey` 等基础校验。
- `PreviewService` 拒绝客户端提交的正数优惠/光年币抵扣金额；前端确认页不再发送本地抵扣字段，并把本地可用抵扣金额置 0。
- 更新未跟踪的 `PreviewServiceTest` 中优惠抵扣用例，验证正数客户端抵扣被拒绝。
- 新增 `FileSignatureValidator`，对 JPG/PNG/GIF/WebP、MP4/MOV/M4V/WebM 做文件头魔数校验。
- 管理端图片/视频上传、用户头像上传、维修图片上传接入魔数校验，拒绝 MIME/扩展名伪装文件。
- 首页商品请求竞态保护变量从 `productTask` 改名为 `productRequestId`，保持原行为不变并满足门禁契约。
- 社区帖子详情和评论增加资源级可见性保护：未审核/非通过帖子仅作者可访问，匿名或其他用户不能枚举读取。
- `CircleService`、`AdminCommunityController`、`AdminSkuController` 移除 `.last("LIMIT ...")`，改用 MyBatis-Plus `Page`。
- 恢复 Spring Security 默认 `X-Content-Type-Options: nosniff`，避免公开上传目录放大内容嗅探风险。
- 全局启用 Jackson 未知字段拒绝，并补充请求体不可读/未知字段的 400 响应处理。
- 订单列表分页参数增加 `@Min/@Max` 边界。
- `application-dev.yml` 中本地 JWT/微信配置改为从 `JWT_SECRET`、`WX_APP_ID`、`WX_APP_SECRET` 读取。
- `START.md`、`start-dev.ps1`、`.harness/changes/20260607-admin-web-server-deploy/deployment.md` 中旧 JWT/微信示例明文改为开发占位或填写提示。
- `JacksonConfig` 显式启用 `DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES`，避免自定义 `ObjectMapper` 绕过全局未知字段拒绝。
- `application-prod.yml` 对 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`REDIS_PASSWORD`、`JWT_SECRET` 改为必须由环境变量提供。
- 新增 `JacksonConfigTest` 验证未知字段会触发 `UnrecognizedPropertyException`。

## 验证证据与结果

- 后端聚焦测试：`mvn "-Dtest=PreviewServiceTest,PaymentServiceTest" test` 通过，7 tests, 0 failures/errors。
- 后端全量测试：`mvn test` 通过，12 tests, 0 failures/errors；配置脱敏、未知字段拒绝和 SQL 拼接收尾后已再次运行通过。
- 小程序构建：`npm.cmd run build:mp-weixin` 通过；沙箱内曾因 Vite 临时配置文件 `EPERM` 失败，已按权限规则在沙箱外重跑通过。
- 管理端构建：`npm.cmd run build` 通过；沙箱内曾因清理 `admin-web/dist` 旧文件 `EPERM` 失败，已按权限规则在沙箱外重跑通过。
- 受控交付门禁：`npm.cmd run harness:ci` 通过，`status=SUCCESS`，`tests=7`，`passed=7`，`total=7`；收尾后已再次运行通过。
- XSS 危险渲染扫描：`frontend/src` 与 `admin-web/src` 未发现 `v-html`、`rich-text`、`innerHTML`、`eval(`、`new Function`、`document.write`、`srcdoc` 命中。
- SQL 拼接扫描：`backend/src/main/java` 与 `backend/src/main/resources` 未发现 MyBatis `${}` 动态 SQL 拼接命中；扫描结果仅剩 `@Value("${...}")` 配置占位符。
- 开发配置明文扫描：`backend/src/main/resources` 未再命中本轮发现的 JWT/微信明文值。
- 已知文档/脚本残留扫描：`START.md`、`start-dev.ps1`、`.harness/changes/20260607-admin-web-server-deploy/deployment.md`、`application-dev.yml` 未再命中本轮发现的 JWT/微信明文值。
- 未知字段拒绝测试：`JacksonConfigTest.objectMapperRejectsUnknownFields` 通过。

## 未验证事项、回退与后续动作

- 本轮没有连接真实微信支付、COS、TMS、生产数据库或生产服务器；支付回调验签、第三方内容安全和对象存储权限仍需在集成环境验证。
- 管理端分析页仍存在业务 mock 注释/占位数据，属于数据链路补齐问题，不是本轮安全输入加固范围。
- 需要轮换已经暴露过的本地 JWT/微信相关密钥或凭证，并检查 Git 历史/其他环境文件是否存在同类泄露；本地 `.env` 仍可能包含旧值，本轮没有擅自改动本地凭证文件。
- 建议后续补控制器/集成测试：未知字段拒绝、匿名访问未审核帖子失败、公开上传资源带 `nosniff`、旧客户端提交正数抵扣失败。
- `backend/src/main/java/com/xingqiu/server/common/controller/UploadController.java` 和 `backend/src/test/java/com/xingqiu/server/order/service/PreviewServiceTest.java` 在本轮前已是未跟踪文件；本轮只做补丁式加固/测试更新，没有删除重建。
- 若需要回退本轮安全加固，优先按本交付记录列出的文件逐项反向补丁；不要使用全仓库 reset，因为工作区存在大量无关未提交改动。
