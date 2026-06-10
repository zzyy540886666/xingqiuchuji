import { request } from "../utils/request";
import { buildQuery } from "../utils/query";

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
  collectCount?: number;
  commentCount: number;
  liked?: boolean;
  collected?: boolean;
  comments?: Comment[];
  status: "PENDING" | "APPROVED" | "REJECTED";
  createdAt: string;
}

export interface Comment {
  id: string;
  postId: string;
  userId: string;
  parentId?: string;
  content: string;
  status: string;
  authorNickname?: string;
  authorAvatar?: string;
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
  const query = buildQuery(params);
  return request(`/community/feed${query ? `?${query}` : ""}`);
}

export function createPost(data: { circleId: string; title: string; content: string; mediaUrls?: string[]; topicId?: string }): Promise<{ id: string }> {
  return request("/community/posts", { method: "POST", data });
}

export function getPostDetail(postId: string): Promise<Post> {
  return request(`/community/posts/${postId}`);
}

export function likePost(postId: string): Promise<Post> {
  return request(`/community/posts/${postId}/like`, { method: "POST" });
}

export function collectPost(postId: string): Promise<Post> {
  return request(`/community/posts/${postId}/collect`, { method: "POST" });
}

export function followUser(userId: string): Promise<boolean> {
  return request(`/community/users/${userId}/follow`, { method: "POST" });
}

export function addComment(postId: string, data: { content: string; parentId?: string }): Promise<Comment> {
  return request(`/community/posts/${postId}/comments`, { method: "POST", data });
}

export function getComments(postId: string): Promise<Comment[]> {
  return request(`/community/posts/${postId}/comments`);
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
  const queryString = buildQuery(params);
  const query = queryString ? `?${queryString}` : "";
  return request(`/community/circles/${circleId}/feed${query}`);
}
