import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../env';
import {
  Directorate,
  DirectorateRequest,
  DeviceType,
  DeviceTypeRequest,
  Office,
  OfficeRequest,
  Section,
  SectionRequest,
  Unit,
  UnitRequest,
  Zone,
  ZoneRequest,
} from '../../core/models/master-data.model';

@Injectable({ providedIn: 'root' })
export class MasterDataService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/admin`;

  getDirectorates(): Observable<Directorate[]> {
    return this.http.get<Directorate[]>(`${this.base}/directorates`);
  }

  createDirectorate(request: DirectorateRequest): Observable<Directorate> {
    return this.http.post<Directorate>(`${this.base}/directorates`, request);
  }

  updateDirectorate(id: number, request: DirectorateRequest): Observable<Directorate> {
    return this.http.put<Directorate>(`${this.base}/directorates/${id}`, request);
  }

  deleteDirectorate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/directorates/${id}`);
  }

  getSections(): Observable<Section[]> {
    return this.http.get<Section[]>(`${this.base}/sections`);
  }

  createSection(request: SectionRequest): Observable<Section> {
    return this.http.post<Section>(`${this.base}/sections`, request);
  }

  updateSection(id: number, request: SectionRequest): Observable<Section> {
    return this.http.put<Section>(`${this.base}/sections/${id}`, request);
  }

  deleteSection(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/sections/${id}`);
  }

  getUnits(): Observable<Unit[]> {
    return this.http.get<Unit[]>(`${this.base}/units`);
  }

  createUnit(request: UnitRequest): Observable<Unit> {
    return this.http.post<Unit>(`${this.base}/units`, request);
  }

  updateUnit(id: number, request: UnitRequest): Observable<Unit> {
    return this.http.put<Unit>(`${this.base}/units/${id}`, request);
  }

  deleteUnit(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/units/${id}`);
  }

  getZones(): Observable<Zone[]> {
    return this.http.get<Zone[]>(`${this.base}/zones`);
  }

  createZone(request: ZoneRequest): Observable<Zone> {
    return this.http.post<Zone>(`${this.base}/zones`, request);
  }

  updateZone(id: number, request: ZoneRequest): Observable<Zone> {
    return this.http.put<Zone>(`${this.base}/zones/${id}`, request);
  }

  deleteZone(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/zones/${id}`);
  }

  getOffices(): Observable<Office[]> {
    return this.http.get<Office[]>(`${this.base}/offices`);
  }

  createOffice(request: OfficeRequest): Observable<Office> {
    return this.http.post<Office>(`${this.base}/offices`, request);
  }

  updateOffice(id: number, request: OfficeRequest): Observable<Office> {
    return this.http.put<Office>(`${this.base}/offices/${id}`, request);
  }

  deleteOffice(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/offices/${id}`);
  }

  getDeviceTypes(): Observable<DeviceType[]> {
    return this.http.get<DeviceType[]>(`${this.base}/device-types`);
  }

  createDeviceType(request: DeviceTypeRequest): Observable<DeviceType> {
    return this.http.post<DeviceType>(`${this.base}/device-types`, request);
  }

  updateDeviceType(id: number, request: DeviceTypeRequest): Observable<DeviceType> {
    return this.http.put<DeviceType>(`${this.base}/device-types/${id}`, request);
  }

  deleteDeviceType(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/device-types/${id}`);
  }
}