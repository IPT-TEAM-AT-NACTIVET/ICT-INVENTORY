import { DeviceStatus, OwnershipType, VerificationStatus } from './enums';

export interface Asset {
  id: number;
  assetNumber: string | null;
  serialNumber: string | null;
  deviceName: string;
  deviceTypeId: number;
  deviceTypeName: string;
  userId: number;
  userFullName: string;
  userEmployeeId: string;
  userEmail: string | null;
  userPhoneNumber: string | null;
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
  ownershipType: OwnershipType;
  deviceStatus: DeviceStatus;
  verificationStatus: VerificationStatus;
  rejectionReason: string | null;
  createdAt: string;
  updatedAt: string;
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
  userId: number;
  ownershipType: OwnershipType;
  deviceStatus: DeviceStatus;
  zoneId: number;
  officeId: number;
}

export interface StaffAssetRequest {
  assetNumber?: string;
  serialNumber?: string;
  deviceName: string;
  deviceTypeId: number;
  ownershipType: OwnershipType;
  deviceStatus: DeviceStatus;
  zoneId: number;
  officeId: number;
}

export interface AssetUpdateRequest {
  assetNumber?: string;
  serialNumber?: string;
  deviceName?: string;
  deviceTypeId?: number;
  deviceStatus?: DeviceStatus;
  ownershipType?: OwnershipType;
  zoneId?: number;
  officeId?: number;
}

export interface RejectRequest {
  rejectionReason: string;
}

export interface AssetFilter {
  assetNumber?: string;
  serialNumber?: string;
  deviceName?: string;
  deviceTypeId?: number | null;
  employeeId?: string;
  userName?: string;
  userId?: number | null;
  directorateId?: number | null;
  sectionId?: number | null;
  unitId?: number | null;
  zoneId?: number | null;
  officeId?: number | null;
  ownershipType?: OwnershipType | '';
  deviceStatus?: DeviceStatus | '';
  verificationStatus?: VerificationStatus | '';
  page?: number;
  size?: number;
}