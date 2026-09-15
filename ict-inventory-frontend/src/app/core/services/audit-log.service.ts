import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../env';
import { AuditLog } from '../models/audit-log.model';

@Injectable({ providedIn: 'root' })
export class AuditLogService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/admin/audit-logs`;

  findAll(): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(this.base);
  }
}
