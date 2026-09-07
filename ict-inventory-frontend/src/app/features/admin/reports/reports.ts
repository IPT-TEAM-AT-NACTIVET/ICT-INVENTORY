import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { ReportService } from '../../../core/services/report.service';
import { TranslationService } from '../../../core/services/translation.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { ReportResponse, ReportItem, ReportSummary } from '../../../core/models/report.model';
import { Asset } from '../../../core/models/asset.model';
import { DeviceStatus } from '../../../core/models/enums';
import {
  DEVICE_STATUS_LABELS,
  OWNERSHIP_TYPE_LABELS,
  deviceStatusTone,
} from '../../../shared/utils/enum-labels';
import { delay, finalize, retry } from 'rxjs';

export type ReportType =
  | 'inventory'
  | 'by-zone'
  | 'by-device-type'
  | 'by-status'
  | 'by-ownership';

@Component({
  selector: 'app-reports',
  imports: [PageHeader, ReactiveFormsModule, StatusBadge, RouterLink, DatePipe],
  templateUrl: './reports.html',
  styleUrl: './reports.css',
})
export class Reports implements OnInit {
  private readonly reportService = inject(ReportService);
  private readonly translation = inject(TranslationService);

  t = (k: string) => this.translation.t(k);

  readonly deviceStatusLabels = DEVICE_STATUS_LABELS;
  readonly ownershipLabels = OWNERSHIP_TYPE_LABELS;

  readonly reportTypes: ReportType[] = [
    'inventory',
    'by-zone',
    'by-device-type',
    'by-status',
    'by-ownership',
  ];

  reportType: ReportType = 'inventory';
  readonly search = new FormControl('');
  readonly summary = signal<ReportSummary | null>(null);
  readonly report = signal<ReportResponse | null>(null);
  readonly assets = signal<Asset[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    this.loadSummary();
    this.loadReport();
  }

  setType(type: ReportType): void {
    this.reportType = type;
    this.error.set('');
    this.loadReport();
  }

  applySearch(): void {
    this.error.set('');
    this.loadSummary();
    this.loadReport();
  }

  resetSearch(): void {
    this.search.setValue('');
    this.error.set('');
    this.loadSummary();
    this.loadReport();
  }

  downloadCsv(): void {
    this.error.set('');
    this.reportService.exportCsv(this.searchTerm()).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = 'inventory.csv';
        anchor.click();
        URL.revokeObjectURL(url);
      },
      error: (err) => {
        this.error.set(httpErrorMessage(err, 'Failed to export the report.'));
      },
    });
  }

  maxBar(report: ReportResponse): number {
    return Math.max(1, ...report.items.map((i) => i.count));
  }

  percentage(item: ReportItem, report: ReportResponse): number {
    if (!report.totalAssets) {
      return 0;
    }
    return Math.round((item.count / report.totalAssets) * 100);
  }

  protected deviceTone(status: DeviceStatus): 'success' | 'danger' {
    return deviceStatusTone(status);
  }

  protected statusLabel(name: string): string {
    return (DEVICE_STATUS_LABELS as Record<string, string>)[name] ?? name;
  }

  protected ownershipLabel(name: string): string {
    return (OWNERSHIP_TYPE_LABELS as Record<string, string>)[name] ?? name;
  }

  private searchTerm(): string | undefined {
    const term = (this.search.value ?? '').trim();
    return term || undefined;
  }

  private loadSummary(): void {
    this.reportService.getSummary(this.searchTerm()).subscribe({
      next: (summary) => this.summary.set(summary),
      error: (err) => this.error.set(httpErrorMessage(err, 'Failed to load the report summary.')),
    });
  }

  private loadReport(): void {
    this.loading.set(true);
    this.error.set('');
    const term = this.searchTerm();
    let op;
    switch (this.reportType) {
      case 'by-zone':
        op = this.reportService.getByZone(term);
        break;
      case 'by-device-type':
        op = this.reportService.getByDeviceType(term);
        break;
      case 'by-status':
        op = this.reportService.getByStatus(term);
        break;
      case 'by-ownership':
        op = this.reportService.getByOwnership(term);
        break;
      default:
        this.loadInventory(term);
        return;
    }
    op.pipe(
      retry({ count: 1, delay: 400 }),
      finalize(() => this.loading.set(false)),
    ).subscribe({
      next: (report) => {
        this.report.set(report);
        this.assets.set([]);
        this.totalElements.set(report.totalAssets);
      },
      error: (err) => {
        this.error.set(httpErrorMessage(err, 'Failed to load the report.'));
      },
    });
  }

  private loadInventory(term: string | undefined): void {
    this.reportService
      .getInventory(term)
      .pipe(
        retry({ count: 1, delay: 400 }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (list) => {
          this.assets.set(list);
          this.totalElements.set(list.length);
          this.report.set(null);
        },
        error: (err) => {
          this.error.set(httpErrorMessage(err, 'Failed to load the report.'));
        },
      });
  }
}
