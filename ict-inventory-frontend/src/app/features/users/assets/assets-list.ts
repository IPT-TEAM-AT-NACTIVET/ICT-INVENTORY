import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { AssetService } from '../../../core/services/asset.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { Asset } from '../../../core/models/asset.model';
import {
  DEVICE_STATUS_LABELS,
  OWNERSHIP_TYPE_LABELS,
  deviceStatusTone,
} from '../../../shared/utils/enum-labels';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-assets-list',
  imports: [PageHeader, RouterLink, StatusBadge],
  templateUrl: './assets-list.html',
})
export class AssetsList implements OnInit {
  private readonly assetService = inject(AssetService);

  readonly items = signal<Asset[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');

  readonly deviceStatusLabels = DEVICE_STATUS_LABELS;
  readonly ownershipLabels = OWNERSHIP_TYPE_LABELS;

  ngOnInit(): void {
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

  protected deviceTone(status: 'ACTIVE' | 'DEFECTIVE'): 'success' | 'danger' {
    return deviceStatusTone(status);
  }
}
