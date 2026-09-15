import { Component, inject, OnInit, signal } from '@angular/core';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { AssetService } from '../../../core/services/asset.service';
import { TranslationService } from '../../../core/services/translation.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { Asset } from '../../../core/models/asset.model';
import { DeviceStatus } from '../../../core/models/enums';
import {
  DEVICE_STATUS_LABELS,
  deviceStatusTone,
} from '../../../shared/utils/enum-labels';

@Component({
  selector: 'app-recycle-bin',
  imports: [PageHeader, StatusBadge],
  templateUrl: './recycle-bin.html',
  styleUrl: './recycle-bin.css',
})
export class RecycleBin implements OnInit {
  private readonly assetService = inject(AssetService);
  readonly translation = inject(TranslationService);

  t(key: string): string {
    return this.translation.t(key);
  }

  readonly deletedAssets = signal<Asset[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly success = signal('');
  pendingRestore = signal<Asset | null>(null);
  pendingPermanentDelete = signal<Asset | null>(null);

  readonly deviceStatusLabels = DEVICE_STATUS_LABELS;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.assetService.getDeletedAssets().subscribe({
      next: (list) => {
        this.deletedAssets.set(list);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(httpErrorMessage(err, 'Failed to load the recycle bin.'));
        this.loading.set(false);
      },
    });
  }

  confirmRestore(item: Asset): void {
    this.pendingRestore.set(item);
  }

  cancelRestore(): void {
    this.pendingRestore.set(null);
  }

  restore(item: Asset): void {
    this.pendingRestore.set(null);
    this.assetService.restore(item.id).subscribe({
      next: () => {
        this.success.set(`Asset "${item.deviceModel}" restored to the inventory.`);
        this.load();
      },
      error: () => {
        this.error.set('Failed to restore the asset.');
      },
    });
  }

  confirmPermanentDelete(item: Asset): void {
    this.pendingPermanentDelete.set(item);
  }

  cancelPermanentDelete(): void {
    this.pendingPermanentDelete.set(null);
  }

  permanentlyDelete(item: Asset): void {
    this.pendingPermanentDelete.set(null);
    this.assetService.deletePermanent(item.id).subscribe({
      next: () => {
        this.success.set(`Asset "${item.deviceModel}" permanently deleted.`);
        this.load();
      },
      error: () => {
        this.error.set('Failed to permanently delete the asset.');
      },
    });
  }

  protected deviceTone(status: DeviceStatus): 'success' | 'danger' {
    return deviceStatusTone(status);
  }

  formatDateTime(iso: string | null): string {
    if (!iso) {
      return '—';
    }
    const d = new Date(iso);
    return (
      d.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }) +
      ' ' +
      d.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })
    );
  }
}