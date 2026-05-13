# API-07 - 报修工单巡检与运维后台

> 来源：`docs/报修工单小程序系统功能清单.xlsx`  
> 说明：本文件是业务补充说明，路径与 Schema 以 `openapi/openapi.yaml` 为准。

## 1. 认证与角色

- 角色：`USER`、`TECHNICIAN`、`ADMIN`、`BACKOFFICE_OPERATOR`
- 角色决定可访问资源范围（例如维修员只可处理被指派工单）。

## 2. 设备与点位

- `GET /api/v1/devices`：设备列表（分页/筛选）
- `POST /api/v1/devices`：新增设备
- `PATCH /api/v1/devices/{id}`：编辑设备
- `POST /api/v1/devices/{id}/qrcode`：生成二维码
- `POST /api/v1/qrcodes/resolve`：解析扫码目标并返回点位/设备信息

## 3. 工单接口

- `POST /api/v1/work-orders`：用户提交报修工单
- `GET /api/v1/work-orders`：工单列表（按角色过滤）
- `GET /api/v1/work-orders/{id}`：工单详情
- `POST /api/v1/work-orders/{id}/assign`：管理员派单/改派
- `POST /api/v1/work-orders/{id}/accept`：维修员接单
- `POST /api/v1/work-orders/{id}/reject`：维修员拒单（需原因）
- `POST /api/v1/work-orders/{id}/progress`：提交处理过程（图片/记录）
- `POST /api/v1/work-orders/{id}/complete`：提交完工
- `POST /api/v1/work-orders/{id}/close`：关闭工单
- `GET /api/v1/work-orders/{id}/logs`：工单操作日志

## 4. 巡检接口

- `GET /api/v1/inspection/templates`：模板列表
- `POST /api/v1/inspection/templates`：新增模板
- `POST /api/v1/inspection/plans`：创建巡检计划
- `PATCH /api/v1/inspection/plans/{id}`：编辑/停用巡检计划
- `GET /api/v1/inspection/tasks`：巡检任务列表
- `POST /api/v1/inspection/tasks/{id}/start`：开始巡检
- `POST /api/v1/inspection/tasks/{id}/submit`：提交巡检记录
- `GET /api/v1/inspection/reports/summary`：巡检报表汇总

## 5. 反馈与知识库

- `POST /api/v1/feedbacks`：提交意见反馈
- `GET /api/v1/feedbacks`：反馈列表（后台）
- `PATCH /api/v1/feedbacks/{id}`：反馈处理状态更新
- `GET /api/v1/knowledge/articles`：经验文章列表
- `POST /api/v1/knowledge/articles`：发布经验文章
- `PATCH /api/v1/knowledge/articles/{id}`：文章编辑/下线

## 6. 统计与驾驶舱

- `GET /api/v1/reports/work-orders`：工单统计
- `GET /api/v1/reports/inspections`：巡检统计
- `GET /api/v1/reports/efficiency`：维修时效/完成率/逾期率
- `GET /api/v1/dashboard/overview`：驾驶舱核心指标
- `GET /api/v1/dashboard/charts`：图表数据（趋势/排行/区域对比）

## 7. 消息提醒

- `GET /api/v1/notifications`：通知列表
- `POST /api/v1/notifications/read`：批量已读
- 消息类型：新工单、新巡检任务、逾期预警、改派通知。

## 8. 字段与约束建议

- 工单状态：`NEW/ASSIGNED/IN_PROGRESS/PENDING_ACCEPT/DONE/REJECTED/CLOSED`
- 巡检状态：`PENDING/IN_PROGRESS/COMPLETED/OVERDUE`
- 工单优先级：`LOW/MEDIUM/HIGH/URGENT`
- 所有分页接口遵循 `00-接口通则.md` 的分页与错误体规范。
