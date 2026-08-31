import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { ReferenceService } from '../../../shared/services/reference.service';
import { ProfileService } from '../../../core/services/profile.service';
import { StaffAssetService } from '../../../core/services/staff-asset.service';
import { Asset } from '../../../core/models/asset.model';
import { DeviceType } from '../../../core/models/master-data.model';
import { Profile } from '../../../core/models/profile.model';
import { DeviceStatus, OwnershipType } from '../../../core/models/enums';
import {
  DEVICE_STATUS_OPTIONS,
  OWNERSHIP_TYPE_OPTIONS,
  VERIFICATION_STATUS_LABELS,
  deviceStatusTone,
  verificationTone,
} from '../../../shared/utils/enum-labels';

@Component({
  selector: 'app-asset-form',
  imports: [PageHeader, ReactiveFormsModule, RouterLink],
  templateUrl: './asset-form.html',
})
export class AssetForm implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly reference = inject(ReferenceService);
  private readonly profileService = inject(ProfileService);
  private readonly staffAssetService = inject(StaffAssetService);

  readonly verificationLabels = VERIFICATION_STATUS_LABELS;
  readonly ownershipOptions = OWNERSHIP_TYPE_OPTIONS;
  readonly deviceStatusOptions = DEVICE_STATUS_OPTIONS;

  readonly form = this.fb.nonNullable.group({
    assetNumber: [''],
    serialNumber: [''],
    deviceName: ['', Validators.required],
    deviceTypeId: [0, Validators.required],
    ownershipType: ['', Validators.required],
    deviceStatus: ['', Validators.required],
  });

  readonly deviceTypes = signal<DeviceType[]>([]);
  readonly registrar = signal<Profile | undefined>(undefined);
  assetId: number | null = null;
  readonly existing = signal<Asset | undefined>(undefined);
  readonly error = signal('');
  readonly success = signal('');
  readonly saving = signal(false);

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.assetId = idParam ? Number(idParam) : null;
    this.reference.getDeviceTypes().subscribe((items) => this.deviceTypes.set(items));
    this.profileService.getMe().subscribe({
      next: (profile) => this.registrar.set(profile),
    });
    if (this.assetId) {
      this.loadExisting();
    }
  }

  loadExisting(): void {
    this.staffAssetService.getMyAsset(this.assetId!).subscribe({
      next: (asset) => {
        this.existing.set(asset);
        this.form.patchValue({
          assetNumber: asset.assetNumber ?? '',
          serialNumber: asset.serialNumber ?? '',
          deviceName: asset.deviceName,
          deviceTypeId: asset.deviceTypeId,
          ownershipType: asset.ownershipType,
          deviceStatus: asset.deviceStatus,
        });
      },
      error: () => {
        this.error.set('Failed to load this asset. You can only edit your own assets.');
      },
    });
  }

  get isEdit(): boolean {
    return this.assetId !== null;
  }

  get locked(): boolean {
    return this.existing()?.verificationStatus === 'VERIFIED';
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (this.locked) {
      this.error.set('Verified assets cannot be edited.');
      return;
    }
    this.saving.set(true);
    this.error.set('');
    const raw = this.form.getRawValue();
    const payload = {
      assetNumber: raw.assetNumber || undefined,
      serialNumber: raw.serialNumber || undefined,
      deviceName: raw.deviceName,
      deviceTypeId: Number(raw.deviceTypeId),
      ownershipType: raw.ownershipType as OwnershipType,
      deviceStatus: raw.deviceStatus as DeviceStatus,
    };
    const op = this.assetId
      ? this.staffAssetService.updateMyAsset(this.assetId, payload)
      : this.staffAssetService.create(payload);
    op.subscribe({
      next: (asset) => {
        this.saving.set(false);
        this.success.set(
          this.assetId ? 'Asset updated.' : 'Asset registered and submitted for verification.',
        );
        if (this.assetId) {
          this.router.navigate(['/staff/assets', this.assetId]);
        } else {
          this.router.navigate(['/staff/assets', asset.id]);
        }
      },
      error: () => {
        this.saving.set(false);
        this.error.set('Failed to save the asset. Please check the details and try again.');
      },
    });
  }

  protected tone(status: 'PENDING' | 'VERIFIED' | 'REJECTED'): 'warning' | 'success' | 'danger' {
    return verificationTone(status);
  }

  protected deviceTone(status: DeviceStatus): 'success' | 'danger' {
    return deviceStatusTone(status);
  }
}