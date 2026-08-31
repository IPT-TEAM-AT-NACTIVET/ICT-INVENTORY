import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../env';
import { Asset, AssetFilter } from '../models/asset.model';
import { ReportResponse } from '../models/report.model';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/admin/reports`;

  getInventory(filter: Partial<AssetFilter>): Observable<ReportResponse> {
    return this.http.get<ReportResponse>(`${this.base}/inventory`, { params: this.params(filter) });
  }

  getByDirectorate(filter: Partial<AssetFilter>): Observable<ReportResponse> {
    return this.http.get<ReportResponse>(`${this.base}/by-directorate`, { params: this.params(filter) });
  }

  getBySection(filter: Partial<AssetFilter>): Observable<ReportResponse> {
    return this.http.get<ReportResponse>(`${this.base}/by-section`, { params: this.params(filter) });
  }

  getByUnit(filter: Partial<AssetFilter>): Observable<ReportResponse> {
    return this.http.get<ReportResponse>(`${this.base}/by-unit`, { params: this.params(filter) });
  }

  getByOffice(filter: Partial<AssetFilter>): Observable<ReportResponse> {
    return this.http.get<ReportResponse>(`${this.base}/by-office`, { params: this.params(filter) });
  }

  getByZone(filter: Partial<AssetFilter>): Observable<ReportResponse> {
    return this.http.get<ReportResponse>(`${this.base}/by-zone`, { params: this.params(filter) });
  }

  getByDeviceType(filter: Partial<AssetFilter>): Observable<ReportResponse> {
    return this.http.get<ReportResponse>(`${this.base}/by-device-type`, { params: this.params(filter) });
  }

  getByStatus(filter: Partial<AssetFilter>): Observable<ReportResponse> {
    return this.http.get<ReportResponse>(`${this.base}/by-status`, { params: this.params(filter) });
  }

  getFiltered(filter: Partial<AssetFilter>): Observable<Asset[]> {
    return this.http.get<Asset[]>(`${this.base}/filtered`, { params: this.params(filter) });
  }

  exportCsv(filter: Partial<AssetFilter>): Observable<Blob> {
    return this.http.get(`${this.base}/inventory/export/csv`, {
      params: this.params(filter),
      responseType: 'blob',
    });
  }

  private params(filter: Partial<AssetFilter>): HttpParams {
    let params = new HttpParams();
    Object.entries(filter).forEach(([key, value]) => {
      const v = value as string | number | null | undefined;
      if (v !== null && v !== undefined && v !== '') {
        params = params.set(key, String(v));
      }
    });
    return params;
  }
}