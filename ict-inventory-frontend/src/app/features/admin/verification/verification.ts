import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { AssetService } from '../../../core/services/asset.service';
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
  selector: 'app-verification',
  imports: [PageHeader, FormsModule, StatusBadge],
  templateUrl: './verification.html',
})
export class Verification implements OnInit {
  private readonly assetService = inject(AssetService);

  readonly pageSize = 20;
  tab: VerificationStatus = 'PENDING';
  readonly items = signal<Asset[]>([]);
  page = 0;
  readonly totalElements = signal(0);
  readonly totalPages = signal(0);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly success = signal('');
  readonly rejectTarget = signal<Asset | null>(null);
  rejectReason = '';

  readonly deviceStatusLabels = DEVICE_STATUS_LABELS;
  readonly ownershipLabels = OWNERSHIP_TYPE_LABELS;
  readonly verificationLabels = VERIFICATION_STATUS_LABELS;

  ngOnInit(): void {
    this.load();
  }

  setTab(tab: VerificationStatus): void {
    this.tab = tab;
    this.page = 0;
    this.rejectTarget.set(null);
    this.error.set('');
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    const op =
      this.tab === 'PENDING'
        ? this.assetService.getPending(this.page, this.pageSize)
        : this.tab === 'VERIFIED'
          ? this.assetService.getVerified(this.page, this.pageSize)
          : this.assetService.getRejected(this.page, this.pageSize);
    op.pipe(
      retry({ count: 1, delay: 400 }),
      finalize(() => this.loading.set(false)),
    ).subscribe({
      next: (paged) => {
        this.items.set(paged.content);
        this.totalElements.set(paged.totalElements);
        this.totalPages.set(paged.totalPages);
        this.page = paged.page;
      },
      error: (err) => {
        this.error.set(httpErrorMessage(err, 'Failed to load assets.'));
      },
    });
  }

  goToPage(target: number): void {
    if (target < 0 || target >= this.totalPages() || target === this.page) {
      return;
    }
    this.page = target;
    this.load();
  }

  verify(item: Asset): void {
    if (!window.confirm(`Verify asset "${item.deviceName}"?`)) {
      return;
    }
    this.assetService.verify(item.id).subscribe({
      next: () => {
        this.success.set('Asset verified.');
        this.load();
      },
      error: () => {
        this.error.set('Failed to verify asset.');
      },
    });
  }

  openReject(item: Asset): void {
    this.rejectTarget.set(item);
    this.rejectReason = '';
  }

  cancelReject(): void {
    this.rejectTarget.set(null);
    this.rejectReason = '';
  }

  reject(): void {
    if (!this.rejectTarget() || !this.rejectReason.trim()) {
      return;
    }
    const target = this.rejectTarget()!;
    this.assetService.reject(target.id, this.rejectReason.trim()).subscribe({
      next: () => {
        this.rejectTarget.set(null);
        this.rejectReason = '';
        this.success.set('Asset rejected.');
        this.load();
      },
      error: () => {
        this.error.set('A rejection reason is required.');
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