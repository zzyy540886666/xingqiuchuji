import { request } from "../utils/request";
import type { WorkOrder } from "../types/workorder";
import { apiBaseUrl, apiPrefix, resolveUploadUrl } from "../utils/request";
import { buildQuery } from "../utils/query";

export type { WorkOrder };

export function uploadRepairImage(filePath: string): Promise<string> {
  const token = uni.getStorageSync("xq_access_token");
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${apiBaseUrl}${apiPrefix}/repair/attachments/images`,
      filePath,
      name: "file",
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success(result) {
        const body = JSON.parse(result.data) as { success: boolean; data?: { url: string }; error?: { message: string } };
        if (body.success && body.data?.url) resolve(resolveUploadUrl(body.data.url));
        else reject(new Error(body.error?.message || "图片上传失败"));
      },
      fail(error) { reject(new Error(error.errMsg || "图片上传失败")); },
    });
  });
}

export function createWorkOrder(data: { deviceId?: string; faultType: string; description: string; images: string[]; location?: string }): Promise<{ id: string }> {
  return request("/work-orders", { method: "POST", data: { deviceId: data.deviceId, faultType: data.faultType, faultDescription: data.description, images: JSON.stringify(data.images) } });
}

export async function getWorkOrders(params: { status?: string; page?: number; pageSize?: number }): Promise<{ items: WorkOrder[]; total: number }> {
  const query = buildQuery({ ...params, pageSize: params.pageSize || 20 });
  const page = await request<{ items: BackendWorkOrder[]; total: number }>(`/work-orders${query ? `?${query}` : ""}`);
  return { ...page, items: page.items.map(mapWorkOrder) };
}

export async function getWorkOrderDetail(id: string): Promise<WorkOrder> {
  const detail = await request<{ workOrder: BackendWorkOrder }>(`/work-orders/${id}`);
  return mapWorkOrder(detail.workOrder);
}

export function acceptWorkOrder(id: string): Promise<void> {
  return request(`/work-orders/${id}/accept`, { method: "POST" });
}

export function rejectWorkOrder(id: string, reason: string): Promise<void> {
  return request(`/work-orders/${id}/reject`, { method: "POST", data: { reason } });
}

export function completeWorkOrder(id: string, data: { solution: string; images: string[]; materials?: string[] }): Promise<void> {
  return request(`/work-orders/${id}/complete`, { method: "POST", data });
}

export function resolveQrCode(code: string): Promise<{ type: string; deviceId?: string; locationId?: string; name: string }> {
  return request("/qrcodes/resolve", { method: "POST", data: { code } });
}

interface BackendWorkOrder {
  id: string;
  deviceName?: string;
  faultType: string;
  faultDescription?: string;
  images?: string;
  status: WorkOrder["status"];
  priority: WorkOrder["priority"];
  createdAt: string;
}

function mapWorkOrder(item: BackendWorkOrder): WorkOrder {
  let images: string[] = [];
  try { images = item.images ? JSON.parse(item.images) : []; } catch { images = []; }
  images = images.map(resolveUploadUrl);
  return {
    id: item.id,
    deviceName: item.deviceName || "",
    faultType: item.faultType,
    description: item.faultDescription || "",
    images,
    status: item.status,
    priority: item.priority,
    createdAt: item.createdAt,
  };
}
