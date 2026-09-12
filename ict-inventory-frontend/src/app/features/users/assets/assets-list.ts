import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { AssetService } from '../../../core/services/asset.service';
import { ReferenceService } from '../../../shared/services/reference.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { Asset, AssetRequest } from '../../../core/models/asset.model';
import { DeviceType, Directorate, Zone } from '../../../core/models/master-data.model';
import { DeviceStatus } from '../../../core/models/enums';
import {
  DEVICE_STATUS_LABELS,
  DEVICE_STATUS_OPTIONS,
  deviceStatusTone,
} from '../../../shared/utils/enum-labels';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-assets-list',
  imports: [PageHeader, RouterLink, ReactiveFormsModule, StatusBadge],
  templateUrl: './assets-list.html',
  styleUrl: './assets-list.css',
})
export class AssetsList implements OnInit {
  private readonly assetService = inject(AssetService);
  private readonly reference = inject(ReferenceService);
  private readonly fb = inject(FormBuilder);

  readonly items = signal<Asset[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly saving = signal(false);
  readonly modalError = signal('');
  showModal = false;

  readonly deviceStatusLabels = DEVICE_STATUS_LABELS;
  readonly deviceStatusOptions = DEVICE_STATUS_OPTIONS;

  readonly form = this.fb.nonNullable.group({
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
      .getAssets(undefined, 0, 200)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (paged) => {
          this.items.set(paged.content);
        },
        error: (err) => {
          this.error.set(httpErrorMessage(err, 'Failed to load the inventory.'));
        },
      });
  }

  openCreateForm(): void {
    this.modalError.set('');
    this.form.reset({
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
    this.showModal = true;
  }

  cancelCreateForm(): void {
    this.showModal = false;
  }

  submitCreateForm(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.modalError.set('');
    const raw = this.form.getRawValue();
    const payload: AssetRequest = {
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
    this.assetService.create(payload).subscribe({
      next: () => {
        this.saving.set(false);
        this.showModal = false;
        this.load();
      },
      error: () => {
        this.saving.set(false);
        this.modalError.set('Failed to save the asset. Check the device details and try again.');
      },
    });
  }

  protected deviceTone(status: DeviceStatus): 'success' | 'danger' {
    return deviceStatusTone(status);
  }
}