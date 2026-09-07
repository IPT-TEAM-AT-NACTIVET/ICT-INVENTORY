import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { AssetService } from '../../../core/services/asset.service';
import { ReportService } from '../../../core/services/report.service';
import { ReferenceService } from '../../../shared/services/reference.service';
import { TranslationService } from '../../../core/services/translation.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { Asset, AssetRequest, AssetUpdateRequest, CsvImportResult } from '../../../core/models/asset.model';
import { DeviceType, Zone } from '../../../core/models/master-data.model';
import { DeviceStatus, OwnershipType } from '../../../core/models/enums';
import { delay, finalize, retry } from 'rxjs';
import {
  DEVICE_STATUS_LABELS,
  DEVICE_STATUS_OPTIONS,
  OWNERSHIP_TYPE_LABELS,
  OWNERSHIP_TYPE_OPTIONS,
  deviceStatusTone,
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
  private readonly router = inject(Router);
  readonly translation = inject(TranslationService);

  t(key: string): string {
    return this.translation.t(key);
  }

  readonly assets = signal<Asset[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly success = signal('');
  editing: Asset | null = null;
  showAssetForm = false;
  importsResult = signal<CsvImportResult | null>(null);
  importing = signal(false);

  readonly deviceStatusLabels = DEVICE_STATUS_LABELS;
  readonly ownershipLabels = OWNERSHIP_TYPE_LABELS;
  readonly deviceStatusOptions = DEVICE_STATUS_OPTIONS;
  readonly ownershipOptions = OWNERSHIP_TYPE_OPTIONS;

  readonly search = new FormControl('');

  readonly assetForm = this.fb.nonNullable.group({
    assetNumber: [''],
    serialNumber: [''],
    deviceModel: ['', Validators.required],
    deviceTypeId: [0, Validators.required],
    userOfAsset: [''],
    ownershipType: ['', Validators.required],
    deviceStatus: ['', Validators.required],
    zoneId: [0, Validators.required],
    office: ['', Validators.maxLength(100)],
  });

  readonly deviceTypes = signal<DeviceType[]>([]);
  readonly zones = signal<Zone[]>([]);

  ngOnInit(): void {
    this.reference.getDeviceTypes().subscribe((items) => this.deviceTypes.set(items));
    this.reference.getZones().subscribe((items) => this.zones.set(items));
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.assetService
      .getAllAssets(this.search.value?.trim() || undefined)
      .pipe(
        retry({ count: 1, delay: 400 }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (list) => {
          this.assets.set(list);
          this.totalElements.set(list.length);
        },
        error: (err) => {
          this.error.set(httpErrorMessage(err, 'Failed to load assets.'));
        },
      });
  }

  applyFilters(): void {
    this.load();
  }

  resetFilters(): void {
    this.search.setValue('');
    this.load();
  }

  downloadCsv(): void {
    this.reportService.exportCsv().subscribe((blob) => {
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = 'inventory.csv';
      anchor.click();
      URL.revokeObjectURL(url);
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }
    this.importing.set(true);
    this.error.set('');
    this.success.set('');
    this.importsResult.set(null);
    this.assetService.importCsv(file).subscribe({
      next: (result) => {
        this.importsResult.set(result);
        this.importing.set(false);
        if (result.imported > 0) {
          this.success.set(`Imported ${result.imported} asset(s).`);
          this.load();
        }
      },
      error: () => {
        this.importing.set(false);
        this.error.set('Failed to import CSV. Check the file and try again.');
      },
      complete: () => {
        input.value = '';
      },
    });
  }

  openCreateForm(): void {
    this.router.navigate(['/admin/register-asset']);
  }

  openEditForm(item: Asset): void {
    this.editing = item;
    this.success.set('');
    this.showAssetForm = true;
    this.assetForm.reset({
      assetNumber: item.assetNumber ?? '',
      serialNumber: item.serialNumber ?? '',
      deviceModel: item.deviceModel,
      deviceTypeId: item.deviceTypeId,
      userOfAsset: item.userOfAsset,
      ownershipType: item.ownershipType,
      deviceStatus: item.deviceStatus,
      zoneId: item.zoneId ?? 0,
      office: item.office ?? '',
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
    if (!window.confirm(`Delete asset "${item.deviceModel}" (${item.assetNumber ?? item.serialNumber ?? item.id})?`)) {
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

  protected deviceTone(status: DeviceStatus): 'success' | 'danger' {
    return deviceStatusTone(status);
  }

  private updateRequest(raw: {
    assetNumber: string;
    serialNumber: string;
    deviceModel: string;
    deviceTypeId: number;
    userOfAsset: string;
    ownershipType: string;
    deviceStatus: string;
    zoneId: number;
    office: string;
  }): AssetUpdateRequest {
    return {
      assetNumber: raw.assetNumber || undefined,
      serialNumber: raw.serialNumber || undefined,
      deviceModel: raw.deviceModel,
      deviceTypeId: Number(raw.deviceTypeId),
      userOfAsset: raw.userOfAsset?.trim() || undefined,
      ownershipType: raw.ownershipType as OwnershipType,
      deviceStatus: raw.deviceStatus as DeviceStatus,
      zoneId: Number(raw.zoneId),
      office: raw.office?.trim() || undefined,
    };
  }

  private createRequest(raw: {
    assetNumber: string;
    serialNumber: string;
    deviceModel: string;
    deviceTypeId: number;
    userOfAsset: string;
    ownershipType: string;
    deviceStatus: string;
    zoneId: number;
    office: string;
  }): AssetRequest {
    return {
      assetNumber: raw.assetNumber || undefined,
      serialNumber: raw.serialNumber || undefined,
      deviceModel: raw.deviceModel,
      deviceTypeId: Number(raw.deviceTypeId),
      userOfAsset: raw.userOfAsset?.trim() || undefined,
      ownershipType: raw.ownershipType as OwnershipType,
      deviceStatus: raw.deviceStatus as DeviceStatus,
      zoneId: Number(raw.zoneId),
      office: raw.office?.trim() || undefined,
    };
  }
}
