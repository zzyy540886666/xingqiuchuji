export type RepairRole = 'USER' | 'TECHNICIAN' | 'ADMIN'

export type WorkOrderStatus =
  | 'NEW'
  | 'ASSIGNED'
  | 'IN_PROGRESS'
  | 'PENDING_ACCEPT'
  | 'DONE'
  | 'REJECTED'
  | 'CLOSED'

export interface RepairCreatePayload {
  faultType: string
  address: string
  description: string
  imageUrls: string[]
  deviceCode?: string
}

export interface WorkOrderItem {
  id: string
  title: string
  status: WorkOrderStatus
  faultType: string
  location: string
  createdAt: string
}

export interface InspectionTaskItem {
  id: string
  location: string
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'OVERDUE'
  deadline: string
}

const baseUrl = '/api/v1'

function request<T>(url: string, method: 'GET' | 'POST' = 'GET', data?: unknown): Promise<T> {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${baseUrl}${url}`,
      method,
      data,
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve((res.data ?? {}) as T)
          return
        }
        reject(new Error(`Request failed: ${res.statusCode}`))
      },
      fail: (err) => reject(err)
    })
  })
}

export function createRepairOrder(payload: RepairCreatePayload) {
  return request<{ id: string }>('/work-orders', 'POST', payload)
}

export function listMyRepairOrders() {
  return request<WorkOrderItem[]>('/work-orders?scope=mine')
}

export function listTechnicianWorkOrders() {
  return request<WorkOrderItem[]>('/work-orders?scope=technician')
}

export function listInspectionTasks() {
  return request<InspectionTaskItem[]>('/inspection/tasks?scope=mine')
}

export function getAdminOverview() {
  return request<{
    pendingWorkOrders: number
    overdueInspections: number
    completionRate: number
    alerts: string[]
  }>('/dashboard/overview')
}
