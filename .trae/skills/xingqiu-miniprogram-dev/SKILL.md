---
name: "xingqiu-miniprogram-dev"
description: "Guides WeChat miniprogram structure, pages, WXML/WXSS/JS, wx.request to a self-hosted backend, modular layout, and minimalist SVG icons (no emoji in UI). Use when editing miniprogram sources, add..."
---
# 星球出机小程序 - 小程序端开发

## 目标

在声明式、模块化前提下完成页面与交互；网络与状态与自建后端保持一致；界面图标使用极简 SVG，不使用 emoji。

## 目录与分层

- 按功能分目录：`pages/`、`components/`、`services/`（或 `api/`）、`utils/`、`constants/`、`behaviors/`（如需复用逻辑）。
- 单页保持「结构（WXML）- 样式（WXSS）- 逻辑（JS/TS）」同路径命名；公共样式放 `styles/`，避免在页面硬编码魔法数。
- 接口调用集中在 `services/`，页面只处理展示与用户事件，不把 URL 与鉴权细节散落在各页。

## 网络与配置

- `baseURL`、环境切换（开发/预发/生产）来自配置模块或构建注入，禁止把密钥写进仓库。
- 所有请求走 HTTPS；需要登录态时使用后端约定方式（如 `Authorization` 或会话 Cookie，按小程序能力与后端一致实现）。
- 请求失败要有可恢复路径：超时、401、5xx 与业务错误码分支清晰，避免静默吞错。

## 数据与界面

- 合并 `setData`，减少频繁小粒度更新；列表大数据考虑分页或虚拟列表策略（按微信文档与项目现状选型）。
- 展示层对外部/接口字符串做必要转义或白名单渲染，避免把未信任 HTML 当富文本直接解析（防 XSS 展示面）。
- 图标：使用内联或独立 `.svg` 资源，风格统一、可访问属性（`aria` 等按基础库支持情况处理）。

## 与后端协作

- 字段名、类型、枚举状态与后端文档或 OpenAPI 一致；新增字段先对齐再改前后端，避免「前端自创字段」。
- 业务状态以后端返回为准，前端状态机只做展示与乐观更新的回滚设计（若采用乐观更新）。

## 自检清单（改完即过）

- [ ] 新页面已在 `app.json`（或分包配置）注册路径正确。
- [ ] 无硬编码域名分散在业务文件。
- [ ] 列表/表单边界（空态、加载态、错误态）齐全。
- [ ] 未在 UI 使用 emoji 作为功能图标。