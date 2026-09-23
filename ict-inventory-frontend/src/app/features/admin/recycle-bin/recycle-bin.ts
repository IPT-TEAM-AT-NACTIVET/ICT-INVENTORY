import { Component, inject, OnInit, signal } from '@angular/core';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { AssetService } from '../../../core/services/asset.service';
import { TranslationService } from '../../../core/services/translation.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { Asset } from '../../../core/models/asset.model';
import { DeviceStatus } from '../../../core/models/enums';
import {
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

  t(key: string, params?: Record<string, string | number>): string {
    return this.translation.t(key, params);
  }

  readonly deletedAssets = signal<Asset[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly success = signal('');
  readonly pendingRestore = signal<Asset | null>(null);
  readonly pendingPermanentDelete = signal<Asset | null>(null);

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
        this.error.set(httpErrorMessage(err, this.t('recycleBin.loadError'), this.t));
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
        this.success.set(this.t('recycleBin.restored', { device: item.deviceModel }));
        this.load();
      },
      error: () => {
        this.error.set(this.t('recycleBin.restoreFailed'));
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
        this.success.set(this.t('recycleBin.permanentlyDeleted', { device: item.deviceModel }));
        this.load();
      },
      error: () => {
        this.error.set(this.t('recycleBin.permanentDeleteFailed'));
      },
    });
  }

  protected deviceStatusLabel(status: DeviceStatus): string {
    return status === 'WORKING' ? this.t('deviceStatus.working') : this.t('deviceStatus.notWorking');
  }

  protected deviceTone(status: DeviceStatus): 'success' | 'danger' {
    return deviceStatusTone(status);
  }

  formatDateTime(iso: string | null): string {
    return this.translation.formatDateTime(iso);
  }
}