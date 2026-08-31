import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { ReportService } from '../../../core/services/report.service';
import { ReferenceService } from '../../../shared/services/reference.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { ReportResponse } from '../../../core/models/report.model';
import { AssetFilter } from '../../../core/models/asset.model';
import { Directorate, DeviceType, Office, Section, Unit, Zone } from '../../../core/models/master-data.model';
import { DeviceStatus, OwnershipType, VerificationStatus } from '../../../core/models/enums';
import {
  DEVICE_STATUS_LABELS,
  DEVICE_STATUS_OPTIONS,
  OWNERSHIP_TYPE_LABELS,
  OWNERSHIP_TYPE_OPTIONS,
  VERIFICATION_STATUS_LABELS,
  VERIFICATION_STATUS_OPTIONS,
  deviceStatusTone,
  verificationTone,
} from '../../../shared/utils/enum-labels';
import { delay, finalize, retry } from 'rxjs';

export type ReportType =
  | 'inventory'
  | 'by-directorate'
  | 'by-section'
  | 'by-unit'
  | 'by-zone'
  | 'by-office'
  | 'by-device-type'
  | 'by-status';

@Component({
  selector: 'app-reports',
  imports: [PageHeader, ReactiveFormsModule, FormsModule, StatusBadge],
  templateUrl: './reports.html',
})
export class Reports implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly reportService = inject(ReportService);
  private readonly reference = inject(ReferenceService);

  readonly deviceStatusLabels = DEVICE_STATUS_LABELS;
  readonly ownershipLabels = OWNERSHIP_TYPE_LABELS;
  readonly verificationLabels = VERIFICATION_STATUS_LABELS;
  readonly deviceStatusOptions = DEVICE_STATUS_OPTIONS;
  readonly ownershipOptions = OWNERSHIP_TYPE_OPTIONS;
  readonly verificationOptions = VERIFICATION_STATUS_OPTIONS;

  reportType: ReportType = 'inventory';
  readonly report = signal<ReportResponse | null>(null);
  expandedIndex: number | null = null;
  readonly loading = signal(true);
  readonly error = signal('');

  readonly deviceTypes = signal<DeviceType[]>([]);
  readonly directorates = signal<Directorate[]>([]);
  readonly sections = signal<Section[]>([]);
  readonly units = signal<Unit[]>([]);
  readonly zones = signal<Zone[]>([]);
  readonly offices = signal<Office[]>([]);

  readonly filters = this.fb.nonNullable.group({
    assetNumber: [''],
    serialNumber: [''],
    deviceName: [''],
    employeeId: [''],
    deviceTypeId: [''],
    directorateId: [''],
    sectionId: [''],
    unitId: [''],
    zoneId: [''],
    officeId: [''],
    ownershipType: [''],
    deviceStatus: [''],
    verificationStatus: [''],
  });

  ngOnInit(): void {
    this.reference.getDeviceTypes().subscribe((items) => this.deviceTypes.set(items));
    this.reference.getDirectorates().subscribe((items) => this.directorates.set(items));
    this.reference.getSections().subscribe((items) => this.sections.set(items));
    this.reference.getUnits().subscribe((items) => this.units.set(items));
    this.reference.getZones().subscribe((items) => this.zones.set(items));
    this.reference.getOffices().subscribe((items) => this.offices.set(items));
    this.filters.controls.zoneId.valueChanges.subscribe((zoneId) => {
      this.filters.controls.officeId.setValue('', { emitEvent: false });
      const zone = this.num(zoneId);
      if (zone) {
        this.reference.getOffices(zone).subscribe((items) => this.offices.set(items));
      } else {
        this.offices.set([]);
      }
    });
    this.load();
  }

  setType(type: ReportType): void {
    this.reportType = type;
    this.expandedIndex = null;
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    const filter = this.buildFilter();
    let op;
    switch (this.reportType) {
      case 'by-directorate':
        op = this.reportService.getByDirectorate(filter);
        break;
      case 'by-section':
        op = this.reportService.getBySection(filter);
        break;
      case 'by-unit':
        op = this.reportService.getByUnit(filter);
        break;
      case 'by-zone':
        op = this.reportService.getByZone(filter);
        break;
      case 'by-office':
        op = this.reportService.getByOffice(filter);
        break;
      case 'by-device-type':
        op = this.reportService.getByDeviceType(filter);
        break;
      case 'by-status':
        op = this.reportService.getByStatus(filter);
        break;
      default:
        op = this.reportService.getInventory(filter);
    }
    op.pipe(
      retry({ count: 1, delay: 400 }),
      finalize(() => this.loading.set(false)),
    ).subscribe({
      next: (report) => {
        this.report.set(report);
      },
      error: (err) => {
        this.error.set(httpErrorMessage(err, 'Failed to load the report.'));
      },
    });
  }

  applyFilters(): void {
    this.load();
  }

  resetFilters(): void {
    this.filters.reset();
    this.load();
  }

  downloadCsv(): void {
    this.reportService.exportCsv(this.buildFilter()).subscribe((blob) => {
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = 'inventory.csv';
      anchor.click();
      URL.revokeObjectURL(url);
    });
  }

  toggle(index: number): void {
    this.expandedIndex = this.expandedIndex === index ? null : index;
  }

  protected tone(status: VerificationStatus): 'warning' | 'success' | 'danger' {
    return verificationTone(status);
  }

  protected deviceTone(status: DeviceStatus): 'success' | 'danger' {
    return deviceStatusTone(status);
  }

  private buildFilter(): Partial<AssetFilter> {
    const f = this.filters.getRawValue();
    return {
      assetNumber: f.assetNumber || undefined,
      serialNumber: f.serialNumber || undefined,
      deviceName: f.deviceName || undefined,
      employeeId: f.employeeId || undefined,
      deviceTypeId: this.num(f.deviceTypeId),
      directorateId: this.num(f.directorateId),
      sectionId: this.num(f.sectionId),
      unitId: this.num(f.unitId),
      zoneId: this.num(f.zoneId),
      officeId: this.num(f.officeId),
      ownershipType: (f.ownershipType || undefined) as OwnershipType | undefined,
      deviceStatus: (f.deviceStatus || undefined) as DeviceStatus | undefined,
      verificationStatus: (f.verificationStatus || undefined) as VerificationStatus | undefined,
    };
  }

  private num(value: string): number | undefined {
    return value ? Number(value) : undefined;
  }
}