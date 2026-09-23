import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { AuditLogService } from '../../../core/services/audit-log.service';
import { TranslationService } from '../../../core/services/translation.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { AuditLog } from '../../../core/models/audit-log.model';
import { finalize, retry } from 'rxjs';

@Component({
  selector: 'app-audit-logs',
  imports: [PageHeader, ReactiveFormsModule],
  templateUrl: './audit-logs.html',
  styleUrl: './audit-logs.css',
})
export class AuditLogs implements OnInit {
  private readonly auditLogService = inject(AuditLogService);
  private readonly translation = inject(TranslationService);

  readonly loading = signal(true);
  readonly error = signal('');
  readonly allLogs = signal<AuditLog[]>([]);
  readonly search = new FormControl('');

  ngOnInit(): void {
    this.load();
  }

  t(key: string): string {
    return this.translation.t(key);
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.auditLogService
      .findAll()
      .pipe(
        retry({ count: 1, delay: 400 }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (logs) => this.allLogs.set(logs),
        error: (err) => this.error.set(httpErrorMessage(err, this.t('auditLogs.loadError'), this.t)),
      });
  }

  get filteredLogs(): AuditLog[] {
    const q = (this.search.value ?? '').trim().toLowerCase();
    if (!q) {
      return this.allLogs();
    }
    return this.allLogs().filter(
      (l) =>
        l.action.toLowerCase().includes(q) ||
        l.entityType.toLowerCase().includes(q) ||
        (l.performedBy ?? '').toLowerCase().includes(q) ||
        (l.description ?? '').toLowerCase().includes(q),
    );
  }

  formatDateTime(iso: string): string {
    return this.translation.formatDateTime(iso);
  }
}
