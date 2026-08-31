import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { DashboardService } from '../../../core/services/dashboard.service';
import { AuthService } from '../../../core/services/auth.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { StaffDashboardResponse } from '../../../core/models/dashboard.model';
import { delay, finalize, retry } from 'rxjs';

@Component({
  selector: 'app-staff-dashboard',
  imports: [PageHeader, RouterLink],
  templateUrl: './staff-dashboard.html',
})
export class StaffDashboard implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  readonly auth = inject(AuthService);

  readonly stats = signal<StaffDashboardResponse | null>(null);
  readonly error = signal('');
  readonly loading = signal(true);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.dashboardService
      .getStaffDashboard()
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
}