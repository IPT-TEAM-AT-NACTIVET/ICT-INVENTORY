import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../env';
import { Asset, AssetRequest, AssetUpdateRequest, CsvImportResult, Paged } from '../models/asset.model';

@Injectable({ providedIn: 'root' })
export class AssetService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/admin/assets`;

  getAssets(search?: string, page?: number, size?: number): Observable<Paged<Asset>> {
    let params = new HttpParams();
    if (search && search.trim()) {
      params = params.set('search', search.trim());
    }
    if (page !== undefined && page !== null) {
      params = params.set('page', String(page));
    }
    if (size !== undefined && size !== null) {
      params = params.set('size', String(size));
    }
    return this.http.get<Paged<Asset>>(this.base, { params });
  }
  getAsset(id: number): Observable<Asset> {
    return this.http.get<Asset>(`${this.base}/${id}`);
  }

  create(request: AssetRequest): Observable<Asset> {
    return this.http.post<Asset>(this.base, request);
  }

  importCsv(file: File): Observable<CsvImportResult> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    return this.http.post<CsvImportResult>(`${this.base}/import`, formData);
  }

  update(id: number, request: AssetUpdateRequest): Observable<Asset> {
    return this.http.put<Asset>(`${this.base}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}