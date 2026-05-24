export interface WorkOrder {
  id: string;
  deviceName: string;
  faultType: string;
  description: string;
  images: string[];
  status: WorkOrderStatus;
  priority: WorkOrderPriority;
  createdAt: string;
  assigneeName?: string;
  location?: string;
}

export type WorkOrderStatus =
  | "NEW"
  | "ASSIGNED"
  | "IN_PROGRESS"
  | "PENDING_ACCEPT"
  | "DONE"
  | "REJECTED"
  | "CLOSED";

export type WorkOrderPriority = "LOW" | "MEDIUM" | "HIGH" | "URGENT";
