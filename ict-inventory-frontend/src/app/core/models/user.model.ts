import { Role } from './enums';

export interface User {
  id: number;
  employeeId: string;
  fullName: string;
  username: string;
  email: string | null;
  phoneNumber?: string | null;
  setupCompleted?: boolean;
  role: Role;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email?: string;
  phoneNumber?: string;
  password: string;
  confirmPassword: string;
  directorateId?: number;
  sectionId?: number;
  unitId?: number;
  zoneId?: number;
  officeId?: number;
}

export interface LoginResponse {
  token: string;
  user: User;
}

export interface RegisterResponse {
  message: string;
  employeeId: string;
  email: string | null;
  username: string;
  role: Role;
  status: string;
}