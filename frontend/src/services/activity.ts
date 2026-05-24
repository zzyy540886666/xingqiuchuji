import { request } from "../utils/request";

export interface ActivityDetail {
  id: number;
  title: string;
  subtitle?: string;
  tag?: string;
  coverUrl?: string;
  videoUrl?: string;
  description?: string;
  contentJson?: string;
  linkUrl?: string;
  startAt: string;
  endAt?: string;
}

export function getActivities(): Promise<ActivityDetail[]> {
  return request<ActivityDetail[]>("/config/activities");
}

export function getActivityDetail(id: number): Promise<ActivityDetail> {
  return request<ActivityDetail>(`/config/activities/${id}`);
}
