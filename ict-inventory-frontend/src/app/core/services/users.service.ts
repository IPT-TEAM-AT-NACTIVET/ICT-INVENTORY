import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../env';
import { UserAccount } from '../models/users.model';

@Injectable({ providedIn: 'root' })
export class UsersService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/users`;

  findAll(search?: string): Observable<UserAccount[]> {
    let params = new HttpParams();
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<UserAccount[]>(this.base, { params });
  }

  findById(id: number): Observable<UserAccount> {
    return this.http.get<UserAccount>(`${this.base}/${id}`);
  }

  approve(id: number): Observable<UserAccount> {
    return this.http.post<UserAccount>(`${this.base}/${id}/approve`, {});
  }

  activate(id: number): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/activate`, {});
  }

  deactivate(id: number): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/deactivate`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
