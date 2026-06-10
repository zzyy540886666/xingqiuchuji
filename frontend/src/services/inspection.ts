import { request } from "../utils/request";
import { buildQuery } from "../utils/query";
import type { InspectionTask } from "../types/inspection";

export type { InspectionTask };

export function getInspectionTasks(params: { status?: string; page?: number }): Promise<{ items: InspectionTask[]; total: number }> {
  const query = buildQuery(params);
  return request(`/inspection/tasks${query ? `?${query}` : ""}`);
}

export function startInspection(taskId: string): Promise<void> {
  return request(`/inspection/tasks/${taskId}/start`, { method: "POST" });
}

export function submitInspection(taskId: string, data: { items: { checkPoint: string; result: string; remark?: string; images?: string[] }[] }): Promise<void> {
  return request(`/inspection/tasks/${taskId}/submit`, { method: "POST", data });
}
