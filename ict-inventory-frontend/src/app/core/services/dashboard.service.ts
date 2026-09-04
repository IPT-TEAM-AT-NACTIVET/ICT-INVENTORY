import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../env';
import { DashboardResponse, UserDashboardResponse } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  getAdminDashboard(): Observable<DashboardResponse> {
    return this.http.get<DashboardResponse>(`${this.base}/admin/dashboard`);
  }

  getUserDashboard(): Observable<UserDashboardResponse> {
    return this.http.get<UserDashboardResponse>(`${this.base}/users/dashboard`);
  }
}