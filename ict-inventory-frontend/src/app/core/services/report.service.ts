import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../env';
import { Asset, Paged } from '../models/asset.model';
import { ReportResponse, ReportSummary } from '../models/report.model';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/admin/reports`;

  getSummary(search?: string): Observable<ReportSummary> {
    return this.http.get<ReportSummary>(`${this.base}/summary`, { params: this.params({ search }) });
  }

  getInventory(search: string | undefined, page: number, size: number): Observable<Paged<Asset>> {
    return this.http.get<Paged<Asset>>(`${this.base}/inventory`, {
      params: this.params({ search, page, size }),
    });
  }

  getByOffice(search?: string): Observable<ReportResponse> {
    return this.http.get<ReportResponse>(`${this.base}/by-office`, { params: this.params({ search }) });
  }

  getByZone(search?: string): Observable<ReportResponse> {
    return this.http.get<ReportResponse>(`${this.base}/by-zone`, { params: this.params({ search }) });
  }

  getByDeviceType(search?: string): Observable<ReportResponse> {
    return this.http.get<ReportResponse>(`${this.base}/by-device-type`, { params: this.params({ search }) });
  }

  getByStatus(search?: string): Observable<ReportResponse> {
    return this.http.get<ReportResponse>(`${this.base}/by-status`, { params: this.params({ search }) });
  }

  getByOwnership(search?: string): Observable<ReportResponse> {
    return this.http.get<ReportResponse>(`${this.base}/by-ownership`, { params: this.params({ search }) });
  }

  exportCsv(search?: string): Observable<Blob> {
    return this.http.get(`${this.base}/inventory/export/csv`, {
      params: this.params({ search }),
      responseType: 'blob',
    });
  }

  private params(values: Record<string, string | number | null | undefined>): HttpParams {
    let params = new HttpParams();
    Object.entries(values).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        params = params.set(key, String(value));
      }
    });
    return params;
  }
}
