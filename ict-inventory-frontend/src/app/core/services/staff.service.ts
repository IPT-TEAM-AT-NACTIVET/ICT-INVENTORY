import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../env';
import { Staff, StaffCreateRequest, StaffUpdateRequest } from '../models/staff.model';

@Injectable({ providedIn: 'root' })
export class StaffService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/admin/staff`;

  getStaff(search?: string): Observable<Staff[]> {
    const params = search ? { params: { search } } : {};
    return this.http.get<Staff[]>(this.base, params);
  }

  create(request: StaffCreateRequest): Observable<Staff> {
    return this.http.post<Staff>(this.base, request);
  }

  update(id: number, request: StaffUpdateRequest): Observable<Staff> {
    return this.http.put<Staff>(`${this.base}/${id}`, request);
  }

  toggleEnabled(id: number): Observable<void> {
    return this.http.patch<void>(`${this.base}/${id}/toggle-enabled`, {});
  }

  activate(id: number): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/activate`, {});
  }

  deactivate(id: number): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/deactivate`, {});
  }

  resetPassword(id: number): Observable<Staff> {
    return this.http.post<Staff>(`${this.base}/${id}/reset-password`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}