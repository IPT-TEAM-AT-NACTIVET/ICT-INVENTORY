import { Role } from './enums';

export interface User {
  id: number;
  employeeId: string;
  fullName: string;
  email: string | null;
  phoneNumber?: string | null;
  setupCompleted?: boolean;
  role: Role;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}
