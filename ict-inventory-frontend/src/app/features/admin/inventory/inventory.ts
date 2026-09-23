import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { AssetService } from '../../../core/services/asset.service';
import { ReferenceService } from '../../../shared/services/reference.service';
import { TranslationService } from '../../../core/services/translation.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { Asset, AssetRequest, AssetUpdateRequest, CsvImportResult } from '../../../core/models/asset.model';
import { DeviceType, Directorate, Zone } from '../../../core/models/master-data.model';
import { DeviceStatus } from '../../../core/models/enums';
import { delay, finalize, retry } from 'rxjs';
import {
  deviceStatusTone,
} from '../../../shared/utils/enum-labels';

@Component({
  selector: 'app-inventory',
  imports: [PageHeader, ReactiveFormsModule, StatusBadge],
  templateUrl: './inventory.html',
  styleUrl: './inventory.css',
})
export class Inventory implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly assetService = inject(AssetService);
  private readonly reference = inject(ReferenceService);
  private readonly router = inject(Router);
  readonly translation = inject(TranslationService);

  t = (key: string, params?: Record<string, string | number>) => this.translation.t(key, params);

  readonly assets = signal<Asset[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly success = signal('');
  editing: Asset | null = null;
  showAssetForm = false;
  pendingDelete = signal<Asset | null>(null);
  importsResult = signal<CsvImportResult | null>(null);
  importing = signal(false);
  importOpen = signal(false);
  importFormat = signal<'csv' | 'excel'>('csv');
  importFileName = signal('');
  importError = signal('');
  selectedImportFile: File | null = null;

  readonly deviceStatusOptions = computed(() =>
    (['WORKING', 'NOT_WORKING'] as DeviceStatus[]).map((status) => ({
      value: status,
      label: status === 'WORKING' ? this.t('deviceStatus.working') : this.t('deviceStatus.notWorking'),
    })),
  );

  readonly search = new FormControl('');

  readonly assetForm = this.fb.nonNullable.group({
    assetNumber: [''],
    serialNumber: [''],
    zoneId: [0, Validators.required],
    directorateId: [0],
    office: ['', Validators.maxLength(100)],
    userOfAsset: ['', Validators.maxLength(255)],
    deviceTypeId: [0, Validators.required],
    deviceModel: ['', Validators.required],
    deviceStatus: ['', Validators.required],
  });

  readonly deviceTypes = signal<DeviceType[]>([]);
  readonly zones = signal<Zone[]>([]);
  readonly directorates = signal<Directorate[]>([]);

  ngOnInit(): void {
    this.reference.getDeviceTypes().subscribe((items) => this.deviceTypes.set(items));
    this.reference.getZones().subscribe((items) => this.zones.set(items));
    this.reference.getDirectorates().subscribe((items) => this.directorates.set(items));
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
          this.error.set(httpErrorMessage(err, this.t('inventory.loadError'), this.t));
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

  openImportForm(): void {
    this.importFormat.set('csv');
    this.selectedImportFile = null;
    this.importFileName.set('');
    this.importError.set('');
    this.importsResult.set(null);
    this.importOpen.set(true);
  }

  cancelImportForm(): void {
    if (!this.importing()) {
      this.importOpen.set(false);
    }
  }

  selectImportFormat(format: 'csv' | 'excel'): void {
    this.importFormat.set(format);
    this.selectedImportFile = null;
    this.importFileName.set('');
    this.importError.set('');
  }

  protected importAccept(): string {
    return '.csv,.xls,.xlsx,text/csv,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
  }

  onImportFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    this.importError.set('');
    if (!file) {
      this.selectedImportFile = null;
      this.importFileName.set('');
      return;
    }
    const name = file.name.toLowerCase();
    if (name.endsWith('.csv')) {
      this.importFormat.set('csv');
    } else if (name.endsWith('.xls') || name.endsWith('.xlsx')) {
      this.importFormat.set('excel');
    } else {
      this.importError.set(this.t('inventory.unsupportedFile'));
      this.selectedImportFile = null;
      this.importFileName.set('');
      input.value = '';
      return;
    }
    this.selectedImportFile = file;
    this.importFileName.set(file.name);
  }

  submitImport(): void {
    const file = this.selectedImportFile;
    if (!file || this.importing()) {
      return;
    }
    this.importing.set(true);
    this.importError.set('');
    this.error.set('');
    this.success.set('');
    this.importsResult.set(null);
    this.assetService.importFile(file).subscribe({
      next: (result) => {
        this.importing.set(false);
        this.importOpen.set(false);
        this.selectedImportFile = null;
        this.importFileName.set('');
        this.importsResult.set(result);
        if (result.imported > 0) {
          this.success.set(this.t('inventory.imported', { count: result.imported }));
          this.load();
        }
      },
      error: () => {
        this.importing.set(false);
        this.importError.set(this.t('inventory.importFailed'));
      },
    });
  }

  openCreateForm(): void {
    this.editing = null;
    this.error.set('');
    this.assetForm.reset({
      assetNumber: '',
      serialNumber: '',
      zoneId: 0,
      directorateId: 0,
      office: '',
      userOfAsset: '',
      deviceTypeId: 0,
      deviceModel: '',
      deviceStatus: '',
    });
    this.showAssetForm = true;
  }

  openEditForm(item: Asset): void {
    this.editing = item;
    this.success.set('');
    this.showAssetForm = true;
    this.assetForm.reset({
      assetNumber: item.assetNumber ?? '',
      serialNumber: item.serialNumber ?? '',
      zoneId: item.zoneId ?? 0,
      directorateId: item.directorateId ?? 0,
      office: item.office ?? '',
      userOfAsset: item.userOfAsset ?? '',
      deviceTypeId: item.deviceTypeId,
      deviceModel: item.deviceModel,
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
        this.success.set(editing ? this.t('inventory.updated') : this.t('inventory.registered'));
        this.load();
      },
      error: () => {
        this.error.set(this.t('inventory.saveError'));
      },
    });
  }

  confirmDelete(item: Asset): void {
    this.pendingDelete.set(item);
  }

  cancelDelete(): void {
    this.pendingDelete.set(null);
  }

  remove(item: Asset): void {
    this.pendingDelete.set(null);
    this.assetService.delete(item.id).subscribe({
      next: () => {
        this.success.set(this.t('inventory.movedToTrash'));
        this.load();
      },
      error: () => {
        this.error.set(this.t('inventory.deleteFailed'));
      },
    });
  }

  protected deviceTone(status: DeviceStatus): 'success' | 'danger' {
    return deviceStatusTone(status);
  }

  protected deviceStatusLabel(status: DeviceStatus): string {
    return status === 'WORKING' ? this.t('deviceStatus.working') : this.t('deviceStatus.notWorking');
  }

  private updateRequest(raw: {
    assetNumber: string;
    serialNumber: string;
    zoneId: number;
    directorateId: number;
    office: string;
    userOfAsset: string;
    deviceTypeId: number;
    deviceModel: string;
    deviceStatus: string;
  }): AssetUpdateRequest {
    return {
      assetNumber: raw.assetNumber || undefined,
      serialNumber: raw.serialNumber || undefined,
      zoneId: Number(raw.zoneId),
      directorateId: raw.directorateId ? Number(raw.directorateId) : null,
      office: raw.office?.trim() || undefined,
      userOfAsset: raw.userOfAsset?.trim() || undefined,
      deviceTypeId: Number(raw.deviceTypeId),
      deviceModel: raw.deviceModel,
      deviceStatus: raw.deviceStatus as DeviceStatus,
    };
  }

  private createRequest(raw: {
    assetNumber: string;
    serialNumber: string;
    zoneId: number;
    directorateId: number;
    office: string;
    userOfAsset: string;
    deviceTypeId: number;
    deviceModel: string;
    deviceStatus: string;
  }): AssetRequest {
    return {
      assetNumber: raw.assetNumber || undefined,
      serialNumber: raw.serialNumber || undefined,
      zoneId: Number(raw.zoneId),
      directorateId: raw.directorateId ? Number(raw.directorateId) : null,
      office: raw.office?.trim() || undefined,
      userOfAsset: raw.userOfAsset?.trim() || undefined,
      deviceTypeId: Number(raw.deviceTypeId),
      deviceModel: raw.deviceModel,
      deviceStatus: raw.deviceStatus as DeviceStatus,
    };
  }
}
