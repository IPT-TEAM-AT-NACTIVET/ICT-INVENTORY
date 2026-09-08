import { Asset } from './asset.model';
import { DeviceStatus } from './enums';

export type ReportFilter =
  | 'by-zone'
  | 'by-office'
  | 'by-device-type'
  | 'by-status'
  | 'by-user'
  | 'by-registered-by';

export interface ReportQuery {
  search?: string;
  deviceTypeId?: number | null;
  status?: DeviceStatus | '';
  zoneId?: number | null;
  office?: string;
  userOfAsset?: string;
  registeredBy?: string;
  from?: string;
  to?: string;
  groupBy?: string;
}

export interface ReportSummary {
  totalAssets: number;
  activeAssets: number;
  defectiveAssets: number;
}

export interface ReportResponse {
  items: never[];
  reportType: string;
  totalAssets: number;
  summary: ReportSummary;
  assets: Asset[];
}

export interface RegistrarOption {
  id: number | null;
  name: string;
}

export interface ReportFilterOptions {
  offices: string[];
  usersOfAsset: string[];
  registeredBy: RegistrarOption[];
}
