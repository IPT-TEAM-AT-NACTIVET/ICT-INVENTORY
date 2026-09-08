import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { ReferenceService } from '../../../shared/services/reference.service';
import { AssetService } from '../../../core/services/asset.service';
import { Asset } from '../../../core/models/asset.model';
import { DeviceType, Directorate, Zone } from '../../../core/models/master-data.model';
import { DeviceStatus } from '../../../core/models/enums';
import { DEVICE_STATUS_OPTIONS, deviceStatusTone } from '../../../shared/utils/enum-labels';

@Component({
  selector: 'app-asset-form',
  imports: [PageHeader, ReactiveFormsModule],
  templateUrl: './asset-form.html',
})
export class AssetForm implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly reference = inject(ReferenceService);
  private readonly assetService = inject(AssetService);

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
  assetId: number | null = null;
  readonly existing = signal<Asset | undefined>(undefined);
  readonly error = signal('');
  readonly success = signal('');
  readonly saving = signal(false);

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.assetId = idParam ? Number(idParam) : null;
    this.reference.getDeviceTypes().subscribe((items) => this.deviceTypes.set(items));
    this.reference.getZones().subscribe((items) => this.zones.set(items));
    this.reference.getDirectorates().subscribe((items) => this.directorates.set(items));
    if (this.assetId) {
      this.loadExisting();
    }
  }

  loadExisting(): void {
    this.assetService.getAsset(this.assetId!).subscribe({
      next: (asset) => {
        this.existing.set(asset);
        this.form.patchValue({
          assetNumber: asset.assetNumber ?? '',
          serialNumber: asset.serialNumber ?? '',
          zoneId: asset.zoneId ?? 0,
          directorateId: asset.directorateId ?? 0,
          office: asset.office ?? '',
          userOfAsset: asset.userOfAsset ?? '',
          deviceTypeId: asset.deviceTypeId,
          deviceModel: asset.deviceModel,
          deviceStatus: asset.deviceStatus,
        });
      },
      error: () => {
        this.error.set('Failed to load this asset.');
      },
    });
  }

  get isEdit(): boolean {
    return this.assetId !== null;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set('');
    const raw = this.form.getRawValue();
    const payload = {
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
    const op = this.assetId
      ? this.assetService.update(this.assetId, payload)
      : this.assetService.create(payload);
    op.subscribe({
      next: (asset) => {
        this.saving.set(false);
        this.success.set(this.assetId ? 'Asset updated.' : 'Asset registered.');
        if (this.assetId) {
          this.router.navigate(['/users/assets', asset.id]);
        } else {
          this.router.navigate(['/admin/inventory']);
        }
      },
      error: () => {
        this.saving.set(false);
        this.error.set('Failed to save the asset. Please check the details and try again.');
      },
    });
  }

  protected deviceTone(status: DeviceStatus): 'success' | 'danger' {
    return deviceStatusTone(status);
  }

  cancel(): void {
    if (this.assetId) {
      this.router.navigate(['/users/assets', this.assetId]);
    } else {
      this.router.navigate(['/admin/inventory']);
    }
  }
}
