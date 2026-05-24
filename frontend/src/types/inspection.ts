export interface InspectionTask {
  id: string;
  planName: string;
  locationName: string;
  status: InspectionStatus;
  dueAt: string;
  startedAt?: string;
  completedAt?: string;
}

export type InspectionStatus = "PENDING" | "IN_PROGRESS" | "COMPLETED" | "OVERDUE";

export interface InspectionCheckItem {
  checkPoint: string;
  result: string;
  remark?: string;
  images?: string[];
}
