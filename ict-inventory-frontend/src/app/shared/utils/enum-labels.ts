import { DeviceStatus, OwnershipType } from '../../core/models/enums';

export const DEVICE_STATUS_LABELS: Record<DeviceStatus, string> = {
  ACTIVE: 'Active',
  DEFECTIVE: 'Defective',
};

export const OWNERSHIP_TYPE_LABELS: Record<OwnershipType, string> = {
  OFFICE: 'Office',
  PERSONAL: 'Personal',
};

export interface SelectOption {
  value: string;
  label: string;
}

export const DEVICE_STATUS_OPTIONS: SelectOption[] = Object.entries(DEVICE_STATUS_LABELS).map(
  ([value, label]) => ({ value, label }),
);

export const OWNERSHIP_TYPE_OPTIONS: SelectOption[] = Object.entries(OWNERSHIP_TYPE_LABELS).map(
  ([value, label]) => ({ value, label }),
);

export interface KeyValue {
  key: string;
  value: number;
}

export function recordEntries(record: Record<string, number>): KeyValue[] {
  return Object.entries(record).map(([key, value]) => ({ key, value }));
}

export function deviceStatusTone(status: DeviceStatus): 'success' | 'danger' {
  return status === 'ACTIVE' ? 'success' : 'danger';
}