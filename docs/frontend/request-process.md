# 小程序请求进程观测设计

## 目标

参考 `D:\Pro\screenshot-to-code` 的事件摘要思路：记录“过程”，但不把大内容塞进状态。当前项目的 API 请求统一经过 `frontend/src/utils/request.ts`，因此可以在这里建立请求进程观测层。

## 核心文件

- `frontend/src/stores/request.ts`：保存进行中的请求和最近请求历史。
- `frontend/src/utils/request.ts`：在请求开始、成功、HTTP 失败、业务失败、网络失败时更新进程状态。

## 记录内容

- 请求路径：`path`
- 请求方法：`method`
- 状态：`pending / success / failed`
- 耗时：`durationMs`
- HTTP 状态：`httpStatus`
- 后端链路：`traceId`
- 错误摘要：`error`

## 不记录内容

- 不记录请求体。
- 不记录响应体。
- 不记录 token、用户资料、订单详情等敏感或大体积数据。

## 使用方式

业务页面不需要直接操作 request store。需要排查问题时，可在调试页面或开发工具里读取 `useRequestStore().history` 查看最近请求。

## 可观察收益

- 判断页面 loading 卡住是哪个接口未返回。
- 判断错误来自 HTTP 状态、业务包装错误，还是网络失败。
- 关联后端 `traceId` 做端到端排查。
- 统计本地请求耗时，定位慢接口或弱网体验问题。
