export interface Notification {
  id: string;
  type: string;
  title: string;
  content: string;
  refId?: string;
  isRead: boolean;
  createdAt: string;
}
