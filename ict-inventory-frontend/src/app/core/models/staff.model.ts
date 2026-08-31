import { Role } from './enums';

export interface Staff {
  id: number;
  employeeId: string;
  fullName: string;
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
  enabled?: boolean;
}