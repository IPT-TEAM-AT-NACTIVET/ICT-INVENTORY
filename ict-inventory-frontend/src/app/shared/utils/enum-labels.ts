import { DeviceStatus } from '../../core/models/enums';

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

export function isWorkingStatus(status: string): boolean {
  return status === 'WORKING';
}