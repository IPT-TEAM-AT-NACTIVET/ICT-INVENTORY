import { Role } from './enums';

export interface Staff {
  id: number;
  employeeId: string;
  fullName: string;
  username: string;
  email: string | null;
  phoneNumber: string | null;
  setupCompleted: boolean;
  initialPassword?: string;
  role: Role;
  enabled: boolean;
  createdAt?: string | null;
  directorateId: number | null;
  directorateName: string | null;
  sectionId: number | null;
  sectionName: string | null;
  unitId: number | null;
  unitName: string | null;
  zoneId: number | null;
  zoneName: string | null;
  officeId: number | null;
  officeCode: string | null;
}

export interface StaffCreateRequest {
  fullName: string;
}

export interface StaffUpdateRequest {
  fullName?: string;
  email?: string;
  phoneNumber?: string;
  directorateId?: number | null;
  sectionId?: number | null;
  unitId?: number | null;
  zoneId?: number | null;
  officeId?: number | null;
  enabled?: boolean;
}