import { request } from "../utils/request";

export interface Post {
  id: string;
  authorName?: string;
  authorNickname?: string;
  authorAvatar: string;
  content: string;
  images?: string[];
  mediaList?: { url: string; type: string; sortOrder: number }[];
  videoUrl?: string;
  topicId?: string;
  topicName?: string;
  likeCount: number;
  commentCount: number;
  liked: boolean;
  status: "PENDING" | "APPROVED" | "REJECTED";
  createdAt: string;
}

export interface Circle {
  id: string;
  name: string;
  avatar?: string;
  iconUrl?: string;
  memberCount: number;
  joined?: boolean;
}

export function getFeed(params: { cursor?: string; limit?: number; topicId?: string }): Promise<Post[]> {
  const query = new URLSearchParams();
  if (params.cursor) query.set("cursor", params.cursor);
  if (params.limit) query.set("limit", String(params.limit));
  if (params.topicId) query.set("topicId", params.topicId);
  return request(`/community/feed?${query.toString()}`);
}

export function createPost(data: { circleId: string; title: string; content: string; mediaUrls?: string[]; topicId?: string }): Promise<{ id: string }> {
  return request("/community/posts", { method: "POST", data });
}

export function getUploadSts(): Promise<{ tmpSecretId: string; tmpSecretKey: string; sessionToken: string; prefix: string; bucket: string; region: string }> {
  return request("/community/upload/sts", { method: "POST" });
}

export function getCircles(): Promise<Circle[]> {
  return request("/community/circles");
}

export function joinCircle(circleId: string): Promise<void> {
  return request(`/community/circles/${circleId}/join`, { method: "POST" });
}

export function getCircleFeed(circleId: string, params: { cursor?: string }): Promise<Post[]> {
  const query = params.cursor ? `?cursor=${params.cursor}` : "";
  return request(`/community/circles/${circleId}/feed${query}`);
}
