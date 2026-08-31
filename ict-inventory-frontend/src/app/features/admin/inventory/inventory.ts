import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { AssetService } from '../../../core/services/asset.service';
import { ReportService } from '../../../core/services/report.service';
import { ReferenceService } from '../../../shared/services/reference.service';
import { StaffService } from '../../../core/services/staff.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { Asset, AssetFilter, AssetRequest, AssetUpdateRequest } from '../../../core/models/asset.model';
import { Directorate, DeviceType, Office, Section, Unit, Zone } from '../../../core/models/master-data.model';
import { Staff } from '../../../core/models/staff.model';
import { DeviceStatus, OwnershipType, VerificationStatus } from '../../../core/models/enums';
import { delay, finalize, retry } from 'rxjs';
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

@Component({
  selector: 'app-inventory',
  imports: [PageHeader, ReactiveFormsModule, StatusBadge],
  templateUrl: './inventory.html',
})
export class Inventory implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly assetService = inject(AssetService);
  private readonly reportService = inject(ReportService);
  private readonly reference = inject(ReferenceService);
  private readonly staffService = inject(StaffService);

  readonly pageSize = 10;
  page = 0;
  readonly assets = signal<Asset[]>([]);
  readonly totalElements = signal(0);
  readonly totalPages = signal(0);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly success = signal('');
  editing: Asset | null = null;
  showAssetForm = false;

  readonly deviceStatusLabels = DEVICE_STATUS_LABELS;
  readonly ownershipLabels = OWNERSHIP_TYPE_LABELS;
  readonly verificationLabels = VERIFICATION_STATUS_LABELS;
  readonly deviceStatusOptions = DEVICE_STATUS_OPTIONS;
  readonly ownershipOptions = OWNERSHIP_TYPE_OPTIONS;
  readonly verificationOptions = VERIFICATION_STATUS_OPTIONS;

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

  readonly assetForm = this.fb.nonNullable.group({
    assetNumber: [''],
    serialNumber: [''],
    deviceName: ['', Validators.required],
    deviceTypeId: [0, Validators.required],
    userId: [0, Validators.required],
    ownershipType: ['', Validators.required],
    deviceStatus: ['', Validators.required],
  });

  readonly deviceTypes = signal<DeviceType[]>([]);
  readonly directorates = signal<Directorate[]>([]);
  readonly sections = signal<Section[]>([]);
  readonly units = signal<Unit[]>([]);
  readonly zones = signal<Zone[]>([]);
  readonly offices = signal<Office[]>([]);
  readonly staff = signal<Staff[]>([]);

  ngOnInit(): void {
    this.reference.getDeviceTypes().subscribe((items) => this.deviceTypes.set(items));
    this.reference.getDirectorates().subscribe((items) => this.directorates.set(items));
    this.reference.getSections().subscribe((items) => this.sections.set(items));
    this.reference.getUnits().subscribe((items) => this.units.set(items));
    this.reference.getZones().subscribe((items) => this.zones.set(items));
    this.reference.getOffices().subscribe((items) => this.offices.set(items));
    this.staffService.getStaff().subscribe((items) => this.staff.set(items));
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

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.assetService
      .getAssets(this.buildFilter())
      .pipe(
        retry({ count: 1, delay: 400 }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (paged) => {
          this.assets.set(paged.content);
          this.totalElements.set(paged.totalElements);
          this.totalPages.set(paged.totalPages);
          this.page = paged.page;
        },
        error: (err) => {
          this.error.set(httpErrorMessage(err, 'Failed to load assets.'));
        },
      });
  }

  applyFilters(): void {
    this.page = 0;
    this.load();
  }

  resetFilters(): void {
    this.page = 0;
    this.filters.reset();
    this.load();
  }

  goToPage(target: number): void {
    if (target < 0 || target >= this.totalPages() || target === this.page) {
      return;
    }
    this.page = target;
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

  openCreateForm(): void {
    this.editing = null;
    this.success.set('');
    this.showAssetForm = true;
    this.assetForm.reset({ deviceTypeId: 0, userId: 0 });
  }

  openEditForm(item: Asset): void {
    this.editing = item;
    this.success.set('');
    this.showAssetForm = true;
    this.assetForm.reset({
      assetNumber: item.assetNumber ?? '',
      serialNumber: item.serialNumber ?? '',
      deviceName: item.deviceName,
      deviceTypeId: item.deviceTypeId,
      userId: item.userId,
      ownershipType: item.ownershipType,
      deviceStatus: item.deviceStatus,
    });
  }

  cancelAssetForm(): void {
    this.showAssetForm = false;
    this.editing = null;
  }

  submitAssetForm(): void {
    if (this.assetForm.invalid) {
      this.assetForm.markAllAsTouched();
      return;
    }
    const raw = this.assetForm.getRawValue();
    const editing = this.editing;
    const op = editing
      ? this.assetService.update(
          editing.id,
          this.updateRequest(raw) as AssetUpdateRequest,
        )
      : this.assetService.create(this.createRequest(raw) as AssetRequest);
    op.subscribe({
      next: () => {
        this.showAssetForm = false;
        this.editing = null;
        this.success.set(editing ? 'Asset updated.' : 'Asset registered.');
        this.load();
      },
      error: () => {
        this.error.set('Failed to save asset. Check the device details.');
      },
    });
  }

  remove(item: Asset): void {
    if (!window.confirm(`Delete asset "${item.deviceName}" (${item.assetNumber ?? item.serialNumber ?? item.id})?`)) {
      return;
    }
    this.assetService.delete(item.id).subscribe({
      next: () => {
        this.success.set('Asset deleted.');
        this.load();
      },
      error: () => {
        this.error.set('Failed to delete asset.');
      },
    });
  }

  protected tone(status: VerificationStatus): 'warning' | 'success' | 'danger' {
    return verificationTone(status);
  }

  protected deviceTone(status: DeviceStatus): 'success' | 'danger' {
    return deviceStatusTone(status);
  }

  private updateRequest(raw: {
    assetNumber: string;
    serialNumber: string;
    deviceName: string;
    deviceTypeId: number;
    ownershipType: string;
    deviceStatus: string;
  }): AssetUpdateRequest {
    return {
      assetNumber: raw.assetNumber || undefined,
      serialNumber: raw.serialNumber || undefined,
      deviceName: raw.deviceName,
      deviceTypeId: Number(raw.deviceTypeId),
      ownershipType: raw.ownershipType as OwnershipType,
      deviceStatus: raw.deviceStatus as DeviceStatus,
    };
  }

  private createRequest(raw: {
    assetNumber: string;
    serialNumber: string;
    deviceName: string;
    deviceTypeId: number;
    userId: number;
    ownershipType: string;
    deviceStatus: string;
  }): AssetRequest {
    return {
      assetNumber: raw.assetNumber || undefined,
      serialNumber: raw.serialNumber || undefined,
      deviceName: raw.deviceName,
      deviceTypeId: Number(raw.deviceTypeId),
      userId: Number(raw.userId),
      ownershipType: raw.ownershipType as OwnershipType,
      deviceStatus: raw.deviceStatus as DeviceStatus,
    };
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
      page: this.page,
      size: this.pageSize,
    };
  }

  private num(value: string): number | undefined {
    return value ? Number(value) : undefined;
  }
}