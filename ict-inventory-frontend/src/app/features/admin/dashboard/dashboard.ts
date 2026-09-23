import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { DashboardService } from '../../../core/services/dashboard.service';
import { TranslationService } from '../../../core/services/translation.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { DashboardResponse } from '../../../core/models/dashboard.model';
import { RouterLink } from '@angular/router';
import { PieChart, PieSlice } from '../../../shared/components/pie-chart/pie-chart';
import { delay, finalize, retry } from 'rxjs';

@Component({
  selector: 'app-admin-dashboard',
  imports: [PageHeader, RouterLink, DatePipe, PieChart],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class AdminDashboard implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  private readonly translation = inject(TranslationService);

  t = (k: string) => this.translation.t(k);

  readonly stats = signal<DashboardResponse | null>(null);
  readonly error = signal('');
  readonly loading = signal(true);

  readonly deviceTypeSlices = computed(() => this.toSlices(this.stats()?.assetsByDeviceType ?? {}));
  readonly zoneSlices = computed(() => this.toSlices(this.stats()?.assetsByZone ?? {}));
  readonly statusSlices = computed(() => this.toStatusSlices(this.stats()?.assetsByDeviceStatus ?? {}));

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.dashboardService
      .getDashboard()
      .pipe(
        retry({ count: 1, delay: 400 }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (stats) => {
          this.stats.set(stats);
        },
        error: (err) => {
          this.error.set(httpErrorMessage(err, this.t('dashboard.loadError'), this.t));
        },
      });
  }

  protected statusLabel(key: string): string {
    return key === 'WORKING' ? this.t('deviceStatus.working') : key === 'NOT_WORKING' ? this.t('deviceStatus.notWorking') : key;
  }

  private toSlices(record: Record<string, number>): PieSlice[] {
    return Object.entries(record).map(([key, value]) => ({ label: key, value }));
  }

  private toStatusSlices(record: Record<string, number>): PieSlice[] {
    return Object.entries(record).map(([key, value]) => ({
      label: this.statusLabel(key),
      value,
      color: key === 'WORKING' ? 'var(--success)' : key === 'NOT_WORKING' ? 'var(--danger)' : undefined,
    }));
  }
}