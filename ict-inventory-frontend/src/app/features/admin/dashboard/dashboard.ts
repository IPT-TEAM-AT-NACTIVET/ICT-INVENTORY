import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { DashboardService } from '../../../core/services/dashboard.service';
import { recordEntries } from '../../../shared/utils/enum-labels';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { DashboardResponse } from '../../../core/models/dashboard.model';
import { RouterLink } from '@angular/router';
import { delay, finalize, retry } from 'rxjs';

@Component({
  selector: 'app-admin-dashboard',
  imports: [PageHeader, RouterLink, DatePipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class AdminDashboard implements OnInit {
  private readonly dashboardService = inject(DashboardService);

  readonly stats = signal<DashboardResponse | null>(null);
  readonly error = signal('');
  readonly loading = signal(true);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.dashboardService
      .getAdminDashboard()
      .pipe(
        retry({ count: 1, delay: 400 }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (stats) => {
          this.stats.set(stats);
        },
        error: (err) => {
          this.error.set(httpErrorMessage(err, 'Failed to load dashboard data.'));
        },
      });
  }

  entries = recordEntries;

  maxBar(values: Record<string, number>): number {
    const max = Math.max(1, ...Object.values(values));
    return max;
  }
}