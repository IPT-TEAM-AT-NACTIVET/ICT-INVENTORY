import { DeviceStatus } from './enums';

export interface Asset {
  id: number;
  assetNumber: string | null;
  serialNumber: string | null;
  zoneId: number | null;
  zoneName: string | null;
  directorateId: number | null;
  directorateName: string | null;
  office: string | null;
  userOfAsset: string;
  deviceTypeId: number;
  deviceTypeName: string;
  deviceModel: string;
  deviceStatus: DeviceStatus;
  createdAt: string;
  updatedAt: string;
  createdById: number | null;
  createdByName: string | null;
  updatedById: number | null;
  updatedByName: string | null;
}

export interface Paged<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface AssetRequest {
  assetNumber?: string;
  serialNumber?: string;
  zoneId: number;
  directorateId?: number | null;
  office?: string;
  userOfAsset?: string;
  deviceTypeId: number;
  deviceModel: string;
  deviceStatus: DeviceStatus;
}

export interface AssetUpdateRequest {
  assetNumber?: string;
  serialNumber?: string;
  zoneId?: number;
  directorateId?: number | null;
  office?: string;
  userOfAsset?: string;
  deviceTypeId?: number;
  deviceModel?: string;
  deviceStatus?: DeviceStatus;
}

export interface AssetFilter {
  assetNumber?: string;
  serialNumber?: string;
  zoneId?: number | null;
  directorateId?: number | null;
  office?: string;
  userOfAsset?: string;
  deviceTypeId?: number | null;
  deviceModel?: string;
  deviceStatus?: DeviceStatus | '';
  page?: number;
  size?: number;
}

export interface CsvImportError {
  row: number;
  message: string;
}

export interface CsvImportResult {
  imported: number;
  failed: number;
  errors: CsvImportError[];
}
