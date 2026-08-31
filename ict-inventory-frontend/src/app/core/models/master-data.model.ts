export interface Directorate {
  id: number;
  name: string;
  code: string | null;
  description: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface Section {
  id: number;
  name: string;
  code: string | null;
  description: string | null;
  directorateId: number;
  directorateName: string;
  createdAt: string;
  updatedAt: string;
}

export interface Unit {
  id: number;
  name: string;
  code: string | null;
  description: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface Zone {
  id: number;
  name: string;
  code: string | null;
  description: string | null;
  status: string;
  officeCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface Office {
  id: number;
  zoneId: number;
  zoneName: string;
  officeCode: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface DeviceType {
  id: number;
  name: string;
  description: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface DirectorateRequest {
  name: string;
  code?: string;
  description?: string;
}

export interface SectionRequest {
  name: string;
  code?: string;
  description?: string;
  directorateId: number;
}

export interface UnitRequest {
  name: string;
  code?: string;
  description?: string;
}

export interface ZoneRequest {
  name: string;
  code?: string;
  description?: string;
  status?: string;
}

export interface OfficeRequest {
  zoneId: number;
  officeCode: string;
  status?: string;
}

export interface DeviceTypeRequest {
  name: string;
  description?: string;
}