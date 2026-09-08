import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../env';
import {
  ReportFilterOptions,
  ReportQuery,
  ReportResponse,
} from '../models/report.model';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/admin/reports`;

  getData(query: ReportQuery): Observable<ReportResponse> {
    return this.http.get<ReportResponse>(`${this.base}/data`, {
      params: this.params(query),
    });
  }

  getFilterOptions(): Observable<ReportFilterOptions> {
    return this.http.get<ReportFilterOptions>(`${this.base}/filter-options`);
  }

  exportReport(query: ReportQuery = {}, format: 'csv' | 'xlsx' | 'pdf' = 'csv'): Observable<Blob> {
    const params = {
      format,
      search: query.search,
      deviceTypeId: query.deviceTypeId,
      status: query.status,
      zoneId: query.zoneId,
      office: query.office,
      userOfAsset: query.userOfAsset,
      registeredBy: query.registeredBy,
      from: query.from,
      to: query.to,
    };
    return this.http.get(`${this.base}/export`, {
      params: this.params(params),
      responseType: 'blob',
    });
  }

  private params(values: object): HttpParams {
    let params = new HttpParams();
    Object.entries(values).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        params = params.set(key, String(value));
      }
    });
    return params;
  }
}
