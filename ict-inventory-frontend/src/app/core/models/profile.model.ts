export interface Profile {
  id: number;
  employeeId: string;
  fullName: string;
  email: string | null;
  phoneNumber: string | null;
  setupCompleted: boolean;
  role: string;
  directorateId: number | null;
  directorateName: string | null;
  sectionId: number | null;
  sectionName: string | null;
  unitId: number | null;
  unitName: string | null;
  enabled: boolean;
}

export interface ProfileUpdateRequest {
  fullName?: string;
  email?: string;
  phoneNumber?: string;
  directorateId?: number | null;
  sectionId?: number | null;
  unitId?: number | null;
}