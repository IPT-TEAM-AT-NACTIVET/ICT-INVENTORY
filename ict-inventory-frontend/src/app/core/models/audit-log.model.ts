export interface AuditLog {
  id: number;
  action: string;
  entityType: string;
  entityId: number | null;
  performedBy: string | null;
  performedByUserId: number | null;
  description: string | null;
  createdAt: string;
}
