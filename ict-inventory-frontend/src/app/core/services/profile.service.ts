import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../env';
import { Profile, ProfileUpdateRequest, PasswordChangeRequest } from '../models/profile.model';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/profile`;

  getMe(): Observable<Profile> {
    return this.http.get<Profile>(`${this.base}/me`);
  }

  updateMe(request: ProfileUpdateRequest): Observable<Profile> {
    return this.http.put<Profile>(`${this.base}/me`, request);
  }

  changePassword(request: PasswordChangeRequest): Observable<void> {
    return this.http.put<void>(`${this.base}/me/password`, request);
  }
}