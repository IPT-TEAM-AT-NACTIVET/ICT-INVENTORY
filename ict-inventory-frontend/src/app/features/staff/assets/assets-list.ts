import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
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
  selector: 'app-assets-list',
  imports: [PageHeader, RouterLink, StatusBadge],
  templateUrl: './assets-list.html',
})
export class AssetsList implements OnInit {
  private readonly staffAssetService = inject(StaffAssetService);

  readonly items = signal<Asset[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');

  readonly deviceStatusLabels = DEVICE_STATUS_LABELS;
  readonly ownershipLabels = OWNERSHIP_TYPE_LABELS;
  readonly verificationLabels = VERIFICATION_STATUS_LABELS;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.staffAssetService
      .getMyAssets()
      .pipe(
        retry({ count: 1, delay: 400 }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (items) => {
          this.items.set(items);
        },
        error: (err) => {
          this.error.set(httpErrorMessage(err, 'Failed to load your assets.'));
        },
      });
  }

  protected tone(status: VerificationStatus): 'warning' | 'success' | 'danger' {
    return verificationTone(status);
  }

  protected deviceTone(status: 'ACTIVE' | 'DEFECTIVE'): 'success' | 'danger' {
    return deviceStatusTone(status);
  }
}