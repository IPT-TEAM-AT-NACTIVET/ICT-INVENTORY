import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { AssetService } from '../../../core/services/asset.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { Asset } from '../../../core/models/asset.model';
import { DEVICE_STATUS_LABELS, OWNERSHIP_TYPE_LABELS, deviceStatusTone } from '../../../shared/utils/enum-labels';
import { delay, finalize, retry } from 'rxjs';

@Component({
  selector: 'app-asset-detail',
  imports: [PageHeader, RouterLink, StatusBadge],
  templateUrl: './asset-detail.html',
})
export class AssetDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly assetService = inject(AssetService);

  readonly asset = signal<Asset | undefined>(undefined);
  readonly loading = signal(true);
  readonly error = signal('');

  readonly deviceStatusLabels = DEVICE_STATUS_LABELS;
  readonly ownershipLabels = OWNERSHIP_TYPE_LABELS;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.assetService
      .getAsset(id)
      .pipe(
        retry({ count: 1, delay: 400 }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (asset) => {
          this.asset.set(asset);
        },
        error: (err) => {
          this.error.set(httpErrorMessage(err, 'Failed to load this asset.'));
        },
      });
  }

  protected deviceTone(status: 'ACTIVE' | 'DEFECTIVE'): 'success' | 'danger' {
    return deviceStatusTone(status);
  }
}
