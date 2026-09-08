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
import { DeviceStatus } from '../../../core/models/enums';
import { DEVICE_STATUS_OPTIONS } from '../../../shared/utils/enum-labels';
import { finalize, retry } from 'rxjs';

interface FilterOption {
  value: string;
  label: string;
}

interface AppliedFilter {
  criterion: ReportFilter;
  value: string;
  label: string;
}

const EMPTY_CRITERIA: Record<ReportFilter, boolean> = {} as Record<ReportFilter, boolean>;
const EMPTY_VALUES: Record<ReportFilter, string> = {} as Record<ReportFilter, string>;

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
    'by-user',
    'by-registered-by',
  ];

  readonly filterOpen = signal(false);
  readonly draftCriteria = signal<Record<ReportFilter, boolean>>(EMPTY_CRITERIA);
  readonly draftValues = signal<Record<ReportFilter, string>>(EMPTY_VALUES);
  readonly activeFilters = signal<AppliedFilter[]>([]);

  readonly zones = signal<Zone[]>([]);
  readonly deviceTypes = signal<DeviceType[]>([]);
  readonly filterOptions = signal<ReportFilterOptions | null>(null);

  readonly report = signal<ReportResponse | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');

  readonly enabledDrafts = computed(() => this.criteria.filter((c) => this.draftCriteria()[c]));

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

  optionsFor(c: ReportFilter): FilterOption[] {
    switch (c) {
      case 'by-zone':
        return this.zones().map((z) => ({ value: String(z.id), label: z.name }));
      case 'by-device-type':
        return this.deviceTypes().map((d) => ({ value: String(d.id), label: d.name }));
      case 'by-status':
        return DEVICE_STATUS_OPTIONS;
      case 'by-office':
        return (this.filterOptions()?.offices ?? []).map((o) => ({ value: o, label: o }));
      case 'by-user':
        return (this.filterOptions()?.usersOfAsset ?? []).map((u) => ({ value: u, label: u }));
      case 'by-registered-by':
        return (this.filterOptions()?.registeredBy ?? []).map((r) => ({ value: r.name, label: r.name }));
      default:
        return [];
    }
  }

  draftEnabled(c: ReportFilter): boolean {
    return !!this.draftCriteria()[c];
  }

  draftValue(c: ReportFilter): string {
    return this.draftValues()[c] ?? '';
  }

  toggleFilter(): void {
    this.filterOpen.set(!this.filterOpen());
  }

  toggleDraft(c: ReportFilter): void {
    this.draftCriteria.update((m) => ({ ...m, [c]: !m[c] }));
  }

  setDraftValue(c: ReportFilter, value: string): void {
    this.draftValues.update((m) => ({ ...m, [c]: value }));
  }

  applyFilters(): void {
    const added: AppliedFilter[] = [];
    for (const c of this.criteria) {
      if (this.draftCriteria()[c]) {
        const value = (this.draftValues()[c] ?? '').trim();
        if (value) {
          added.push({ criterion: c, value, label: this.labelFor(c, value) });
        }
      }
    }
    if (added.length > 0) {
      this.activeFilters.update((current) => {
        const next = current.filter((f) => !added.some((a) => a.criterion === f.criterion));
        return [...next, ...added];
      });
    }
    this.draftCriteria.set(EMPTY_CRITERIA);
    this.draftValues.set(EMPTY_VALUES);
    this.filterOpen.set(false);
    if (added.length > 0) {
      this.loadData();
    }
  }

  removeFilter(criterion: ReportFilter): void {
    this.activeFilters.update((current) => current.filter((f) => f.criterion !== criterion));
    this.loadData();
  }

  clearFilters(): void {
    this.activeFilters.set([]);
    this.draftCriteria.set(EMPTY_CRITERIA);
    this.draftValues.set(EMPTY_VALUES);
    this.filterOpen.set(false);
    this.loadData();
  }

  summary(): ReportSummary | null {
    return this.report()?.summary ?? null;
  }

  readonly exportOpen = signal(false);

  readonly exportFormats: { value: 'csv' | 'xlsx' | 'pdf'; label: string; file: string }[] = [
    { value: 'xlsx', label: 'Excel (.xlsx)', file: 'ict-inventory-report.xlsx' },
    { value: 'pdf', label: 'PDF (.pdf)', file: 'ict-inventory-report.pdf' },
    { value: 'csv', label: 'CSV (.csv)', file: 'ict-inventory-report.csv' },
  ];

  toggleExport(): void {
    this.exportOpen.set(!this.exportOpen());
  }

  download(format: 'csv' | 'xlsx' | 'pdf', filename: string): void {
    this.exportOpen.set(false);
    this.error.set('');
    this.reportService.exportReport(this.buildQuery(), format).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = filename;
        anchor.click();
        URL.revokeObjectURL(url);
      },
      error: (err) => {
        this.error.set(httpErrorMessage(err, this.t('reports.exportError')));
      },
    });
  }

  private labelFor(c: ReportFilter, value: string): string {
    return this.optionsFor(c).find((o) => o.value === value)?.label ?? value;
  }

  private buildQuery(): ReportQuery {
    const query: ReportQuery = {};
    for (const f of this.activeFilters()) {
      switch (f.criterion) {
        case 'by-zone':
          query.zoneId = Number(f.value);
          break;
        case 'by-device-type':
          query.deviceTypeId = Number(f.value);
          break;
        case 'by-status':
          query.status = f.value as DeviceStatus;
          break;
        case 'by-office':
          query.office = f.value;
          break;
        case 'by-user':
          query.userOfAsset = f.value;
          break;
        case 'by-registered-by':
          query.registeredBy = f.value;
          break;
      }
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