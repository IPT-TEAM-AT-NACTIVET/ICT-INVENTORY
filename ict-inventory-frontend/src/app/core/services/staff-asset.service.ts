import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../env';
import { Asset, AssetUpdateRequest, StaffAssetRequest } from '../models/asset.model';

@Injectable({ providedIn: 'root' })
export class StaffAssetService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/assets`;

  create(request: StaffAssetRequest): Observable<Asset> {
    return this.http.post<Asset>(this.base, request);
  }

  getMyAssets(): Observable<Asset[]> {
    return this.http.get<Asset[]>(`${this.base}/my`);
  }

  getMyAsset(id: number): Observable<Asset> {
    return this.http.get<Asset>(`${this.base}/my/${id}`);
  }

  updateMyAsset(id: number, request: AssetUpdateRequest): Observable<Asset> {
    return this.http.put<Asset>(`${this.base}/my/${id}`, request);
  }
}