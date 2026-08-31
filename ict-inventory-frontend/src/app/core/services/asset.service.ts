import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../env';
import { Asset, AssetFilter, AssetRequest, AssetUpdateRequest, Paged, RejectRequest } from '../models/asset.model';

@Injectable({ providedIn: 'root' })
export class AssetService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/admin/assets`;

  getAssets(filter: Partial<AssetFilter>): Observable<Paged<Asset>> {
    return this.http.get<Paged<Asset>>(this.base, { params: this.params(filter) });
  }

  getPending(page = 0, size = 20): Observable<Paged<Asset>> {
    return this.http.get<Paged<Asset>>(`${this.base}/pending`, { params: { page, size } });
  }

  getVerified(page = 0, size = 20): Observable<Paged<Asset>> {
    return this.http.get<Paged<Asset>>(`${this.base}/verified`, { params: { page, size } });
  }

  getRejected(page = 0, size = 20): Observable<Paged<Asset>> {
    return this.http.get<Paged<Asset>>(`${this.base}/rejected`, { params: { page, size } });
  }

  getAsset(id: number): Observable<Asset> {
    return this.http.get<Asset>(`${this.base}/${id}`);
  }

  create(request: AssetRequest): Observable<Asset> {
    return this.http.post<Asset>(this.base, request);
  }

  update(id: number, request: AssetUpdateRequest): Observable<Asset> {
    return this.http.put<Asset>(`${this.base}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  verify(id: number): Observable<Asset> {
    return this.http.patch<Asset>(`${this.base}/${id}/verify`, {});
  }

  reject(id: number, rejectionReason: string): Observable<Asset> {
    return this.http.patch<Asset>(`${this.base}/${id}/reject`, { rejectionReason } satisfies RejectRequest);
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