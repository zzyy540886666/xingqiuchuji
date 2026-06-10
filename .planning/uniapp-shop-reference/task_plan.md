# Task Plan

## Goal
分析 `Megasu/uniapp-shop-vue3-ts` 的架构、页面切换、前后端数据流和页面逻辑，持续把成熟模式落地到当前星球出机小程序。

## Assumptions
- 参考项目用于学习设计与工程模式，不直接复制业务代码。
- 当前工作区存在大量用户未提交改动，所有实现必须避开覆盖和无关重构。
- 优化优先选择可验证、低风险、跨页面收益明确的前端基础能力。

## Phases
1. 读取当前项目约束、结构和验证命令 - completed
2. 拉取并分析参考项目架构与 Git 历史 - completed
3. 对比当前项目并形成分析/优化文档 - completed
4. 实施聚焦优化并补充测试或静态检查 - completed
5. 第一轮运行验证、检查 diff、完成汇报 - completed
6. 深入恢复订单中心真实数据流 - completed
7. 增强首页刷新并更新测试 - completed
8. 第二轮构建、测试和差异检查 - completed

## Expected Outputs
- `docs/frontend/uniapp-shop-vue3-ts-study.md`
- 当前小程序的聚焦代码优化
- 可核验的静态检查或构建结果

## Risks
- 当前工作区改动范围很大，需要严格区分本次改动和既有改动。
- 参考仓库可能依赖旧版 UniApp/第三方服务，不能机械迁移。
- 页面与后端接口较多，本轮应优先优化公共基础设施而非重写所有页面。

## Errors
| Error | Resolution |
| --- | --- |
| `rg.exe` 执行被系统拒绝 | 改用 PowerShell `Get-ChildItem` / `Select-String` |
| 递归搜索前端 AGENTS 文件超时 | 已从仓库级说明获得约束，后续缩小搜索范围 |
| `npm run test:unit` 有 2 个失败 | 测试仍断言主包固定 9 页和旧品牌文案；当前任务开始前主包已为 11 页且文案已变化，本次不修改无关断言 |
| 第二轮测试仍断言“微信一键登录” | 改为验证当前“登录 / 注册”入口和 `loginByWechat` 行为 |
| PowerShell 使用 `$home` 触发只读变量冲突 | 后续避免使用与 `$HOME` 不区分大小写的变量名 |
