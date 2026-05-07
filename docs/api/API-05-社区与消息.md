# API-05 - 社区与 IM

## 1. Feed

**GET** `/community/feed?cursor=&limit=&topicId=`

## 2. 帖子

**POST** `/community/posts` — 创建（进入审核）  
**GET** `/community/posts/{postId}`

## 3. 媒体上传 STS

**POST** `/community/upload/sts` — 返回 COS 临时密钥与路径前缀（**最小权限**）。

## 4. 圈子

**GET** `/community/circles`  
**POST** `/community/circles/{id}/join`  
**GET** `/community/circles/{id}/feed`

## 5. IM

**GET** `/im/token` — 返回厂商所需字段（`token`、`appKey` 等，按 IM 选型定稿）。

## 6. 错误码（节选）

| code | 说明 |
|------|------|
| POST_AUDIT_REJECTED | 400 文案违规 |
| POST_RATE_LIMITED | 429 |
