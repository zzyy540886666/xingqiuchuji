import { request } from "../utils/request";
import type { Notification } from "../types/notification";

export type { Notification };

export function getNotifications(params: { page?: number }): Promise<{ items: Notification[]; total: number }> {
  const query = params.page ? `?page=${params.page}` : "";
  return request(`/notifications${query}`);
}

export function markNotificationsRead(ids: string[]): Promise<void> {
  return request("/notifications/read", { method: "POST", data: { ids } });
}

export function getDashboardOverview(): Promise<{ totalOrders: number; pendingOrders: number; completionRate: number; overdueInspections: number }> {
  return request("/dashboard/overview");
}
