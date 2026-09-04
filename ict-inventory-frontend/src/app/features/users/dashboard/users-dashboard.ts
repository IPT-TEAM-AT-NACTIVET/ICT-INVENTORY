import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { DashboardService } from '../../../core/services/dashboard.service';
import { AuthService } from '../../../core/services/auth.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { UserDashboardResponse } from '../../../core/models/dashboard.model';
import { delay, finalize, retry } from 'rxjs';

@Component({
  selector: 'app-users-dashboard',
  imports: [PageHeader, RouterLink, DatePipe],
  templateUrl: './users-dashboard.html',
})
export class UsersDashboard implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  readonly auth = inject(AuthService);

  readonly stats = signal<UserDashboardResponse | null>(null);
  readonly error = signal('');
  readonly loading = signal(true);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.dashboardService
      .getUserDashboard()
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

  protected entries(record: Record<string, number> | undefined): { key: string; value: number }[] {
    if (!record) {
      return [];
    }
    return Object.entries(record)
      .map(([key, value]) => ({ key, value }))
      .sort((a, b) => b.value - a.value);
  }

  protected maxBar(record: Record<string, number> | undefined): number {
    if (!record) {
      return 0;
    }
    return Object.values(record).reduce((max, value) => (value > max ? value : max), 0) || 1;
  }
}