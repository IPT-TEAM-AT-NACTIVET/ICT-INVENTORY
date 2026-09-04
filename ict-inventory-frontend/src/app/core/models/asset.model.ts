import { DeviceStatus, OwnershipType } from './enums';

export interface Asset {
  id: number;
  assetNumber: string | null;
  serialNumber: string | null;
  deviceName: string;
  deviceTypeId: number;
  deviceTypeName: string;
  userOfAsset: string;
  zoneId: number | null;
  zoneName: string | null;
  office: string | null;
  ownershipType: OwnershipType;
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
  deviceName: string;
  deviceTypeId: number;
  userOfAsset?: string;
  ownershipType: OwnershipType;
  deviceStatus: DeviceStatus;
  zoneId: number;
  office?: string;
}

export interface AssetUpdateRequest {
  assetNumber?: string;
  serialNumber?: string;
  deviceName?: string;
  deviceTypeId?: number;
  userOfAsset?: string;
  deviceStatus?: DeviceStatus;
  ownershipType?: OwnershipType;
  zoneId?: number;
  office?: string;
}

export interface AssetFilter {
  assetNumber?: string;
  serialNumber?: string;
  deviceName?: string;
  deviceTypeId?: number | null;
  userOfAsset?: string;
  zoneId?: number | null;
  office?: string;
  ownershipType?: OwnershipType | '';
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
