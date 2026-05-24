import { request } from "../utils/request";
import type { InspectionTask } from "../types/inspection";

export type { InspectionTask };

export function getInspectionTasks(params: { status?: string; page?: number }): Promise<{ items: InspectionTask[]; total: number }> {
  const query = new URLSearchParams();
  if (params.status) query.set("status", params.status);
  if (params.page) query.set("page", String(params.page));
  return request(`/inspection/tasks?${query.toString()}`);
}

export function startInspection(taskId: string): Promise<void> {
  return request(`/inspection/tasks/${taskId}/start`, { method: "POST" });
}

export function submitInspection(taskId: string, data: { items: { checkPoint: string; result: string; remark?: string; images?: string[] }[] }): Promise<void> {
  return request(`/inspection/tasks/${taskId}/submit`, { method: "POST", data });
}
