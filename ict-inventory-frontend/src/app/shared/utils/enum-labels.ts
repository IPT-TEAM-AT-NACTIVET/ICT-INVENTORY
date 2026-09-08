import { DeviceStatus } from '../../core/models/enums';

export const DEVICE_STATUS_LABELS: Record<DeviceStatus, string> = {
  WORKING: 'Working',
  NOT_WORKING: 'Not Working',
};

export interface SelectOption {
  value: string;
  label: string;
}

export const DEVICE_STATUS_OPTIONS: SelectOption[] = Object.entries(DEVICE_STATUS_LABELS).map(
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
  return status === 'WORKING' ? 'success' : 'danger';
}