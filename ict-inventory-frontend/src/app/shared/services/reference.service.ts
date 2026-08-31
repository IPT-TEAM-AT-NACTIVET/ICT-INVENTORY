import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../env';
import { Directorate, DeviceType, Office, Section, Unit, Zone } from '../../core/models/master-data.model';

@Injectable({ providedIn: 'root' })
export class ReferenceService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/reference`;

  getDirectorates(): Observable<Directorate[]> {
    return this.http.get<Directorate[]>(`${this.base}/directorates`);
  }

  getSections(directorateId?: number): Observable<Section[]> {
    return this.http.get<Section[]>(`${this.base}/sections`, {
      params: directorateId ? { directorateId } : {},
    });
  }

  getUnits(): Observable<Unit[]> {
    return this.http.get<Unit[]>(`${this.base}/units`);
  }

  getZones(): Observable<Zone[]> {
    return this.http.get<Zone[]>(`${this.base}/zones`);
  }

  getOffices(zoneId?: number): Observable<Office[]> {
    return this.http.get<Office[]>(`${this.base}/offices`, {
      params: zoneId ? { zoneId } : {},
    });
  }

  getDeviceTypes(): Observable<DeviceType[]> {
    return this.http.get<DeviceType[]>(`${this.base}/device-types`);
  }
}