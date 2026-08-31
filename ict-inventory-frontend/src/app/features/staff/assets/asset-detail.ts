import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { StaffAssetService } from '../../../core/services/staff-asset.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { Asset } from '../../../core/models/asset.model';
import { VerificationStatus } from '../../../core/models/enums';
import {
  DEVICE_STATUS_LABELS,
  OWNERSHIP_TYPE_LABELS,
  VERIFICATION_STATUS_LABELS,
  deviceStatusTone,
  verificationTone,
} from '../../../shared/utils/enum-labels';
import { delay, finalize, retry } from 'rxjs';

@Component({
  selector: 'app-asset-detail',
  imports: [PageHeader, RouterLink, StatusBadge],
  templateUrl: './asset-detail.html',
})
export class AssetDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly staffAssetService = inject(StaffAssetService);

  readonly asset = signal<Asset | undefined>(undefined);
  readonly loading = signal(true);
  readonly error = signal('');

  readonly deviceStatusLabels = DEVICE_STATUS_LABELS;
  readonly ownershipLabels = OWNERSHIP_TYPE_LABELS;
  readonly verificationLabels = VERIFICATION_STATUS_LABELS;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.staffAssetService
      .getMyAsset(id)
      .pipe(
        retry({ count: 1, delay: 400 }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (asset) => {
          this.asset.set(asset);
        },
        error: (err) => {
          this.error.set(httpErrorMessage(err, 'Failed to load this asset. It may not belong to you.'));
        },
      });
  }

  get canEdit(): boolean {
    const asset = this.asset();
    return asset?.verificationStatus === 'PENDING' || asset?.verificationStatus === 'REJECTED';
  }

  protected tone(status: VerificationStatus): 'warning' | 'success' | 'danger' {
    return verificationTone(status);
  }

  protected deviceTone(status: 'ACTIVE' | 'DEFECTIVE'): 'success' | 'danger' {
    return deviceStatusTone(status);
  }
}