import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { ReportTable } from '../../../shared/components/report-table/report-table';
import { ReportService } from '../../../core/services/report.service';
import { ReferenceService } from '../../../shared/services/reference.service';
import { TranslationService } from '../../../core/services/translation.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import {
  ReportFilter,
  ReportFilterOptions,
  ReportQuery,
  ReportResponse,
  ReportSummary,
} from '../../../core/models/report.model';
import { DeviceType, Zone } from '../../../core/models/master-data.model';
import { DeviceStatus, OwnershipType } from '../../../core/models/enums';
import {
  DEVICE_STATUS_OPTIONS,
  OWNERSHIP_TYPE_OPTIONS,
} from '../../../shared/utils/enum-labels';
import { finalize, retry } from 'rxjs';

@Component({
  selector: 'app-reports',
  imports: [PageHeader, ReportTable],
  templateUrl: './reports.html',
  styleUrl: './reports.css',
})
export class Reports implements OnInit {
  private readonly reportService = inject(ReportService);
  private readonly reference = inject(ReferenceService);
  private readonly translation = inject(TranslationService);

  t = (k: string) => this.translation.t(k);

  readonly criteria: ReportFilter[] = [
    'by-zone',
    'by-office',
    'by-device-type',
    'by-status',
    'by-ownership',
    'by-user',
    'by-registered-by',
  ];

  readonly filterOpen = signal(false);
  readonly criterion = signal<ReportFilter | null>(null);
  readonly criterionValue = signal('');

  readonly zones = signal<Zone[]>([]);
  readonly deviceTypes = signal<DeviceType[]>([]);
  readonly filterOptions = signal<ReportFilterOptions | null>(null);

  readonly report = signal<ReportResponse | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    this.reference.getZones().subscribe({
      next: (z) => this.zones.set(z),
    });
    this.reference.getDeviceTypes().subscribe({
      next: (d) => this.deviceTypes.set(d),
    });
    this.reportService.getFilterOptions().subscribe({
      next: (o) => this.filterOptions.set(o),
    });
    this.loadData();
  }

  readonly valueOptions = computed(() => {
    switch (this.criterion()) {
      case 'by-zone':
        return this.zones().map((z) => ({ value: String(z.id), label: z.name }));
      case 'by-device-type':
        return this.deviceTypes().map((d) => ({ value: String(d.id), label: d.name }));
      case 'by-status':
        return DEVICE_STATUS_OPTIONS;
      case 'by-ownership':
        return OWNERSHIP_TYPE_OPTIONS;
      case 'by-office':
        return (this.filterOptions()?.offices ?? []).map((o) => ({ value: o, label: o }));
      case 'by-user':
        return (this.filterOptions()?.usersOfAsset ?? []).map((u) => ({ value: u, label: u }));
      case 'by-registered-by':
        return (this.filterOptions()?.registeredBy ?? []).map((r) => ({ value: r.name, label: r.name }));
      default:
        return [];
    }
  });

  readonly activeFilter = computed(() => {
    const c = this.criterion();
    if (!c || !this.criterionValue()) {
      return null;
    }
    const label =
      this.valueOptions().find((v) => v.value === this.criterionValue())?.label ??
      this.criterionValue();
    return { criterion: c, label };
  });

  summary(): ReportSummary | null {
    return this.report()?.summary ?? null;
  }

  toggleFilter(): void {
    this.filterOpen.set(!this.filterOpen());
  }

  selectCriterion(c: ReportFilter): void {
    this.criterion.set(this.criterion() === c ? null : c);
    this.criterionValue.set('');
  }

  applyFilter(): void {
    if (!this.criterion() || !this.criterionValue()) {
      return;
    }
    this.filterOpen.set(false);
    this.loadData();
  }

  resetFilter(): void {
    this.criterion.set(null);
    this.criterionValue.set('');
    this.filterOpen.set(false);
    this.loadData();
  }

  downloadCsv(): void {
    this.error.set('');
    this.reportService.exportCsv(this.buildQuery()).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = 'ict-inventory-report.csv';
        anchor.click();
        URL.revokeObjectURL(url);
      },
      error: (err) => {
        this.error.set(httpErrorMessage(err, this.t('reports.exportError')));
      },
    });
  }

  private buildQuery(): ReportQuery {
    const query: ReportQuery = {};
    const c = this.criterion();
    const v = this.criterionValue();
    if (!c || !v) {
      return query;
    }
    switch (c) {
      case 'by-zone':
        query.zoneId = Number(v);
        break;
      case 'by-device-type':
        query.deviceTypeId = Number(v);
        break;
      case 'by-status':
        query.status = v as DeviceStatus;
        break;
      case 'by-ownership':
        query.ownershipType = v as OwnershipType;
        break;
      case 'by-office':
        query.office = v;
        break;
      case 'by-user':
        query.userOfAsset = v;
        break;
      case 'by-registered-by':
        query.registeredBy = v;
        break;
    }
    return query;
  }

  private loadData(): void {
    this.loading.set(true);
    this.error.set('');
    this.reportService
      .getData(this.buildQuery())
      .pipe(
        retry({ count: 1, delay: 400 }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (report) => this.report.set(report),
        error: (err) => {
          this.error.set(httpErrorMessage(err, this.t('reports.loadError')));
        },
      });
  }
}