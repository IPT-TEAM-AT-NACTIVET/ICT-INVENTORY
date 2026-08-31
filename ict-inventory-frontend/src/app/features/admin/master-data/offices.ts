import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { MasterDataService } from '../../../shared/services/master-data.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { Office, Zone } from '../../../core/models/master-data.model';
import { delay, finalize, retry } from 'rxjs';

@Component({
  selector: 'app-offices',
  imports: [PageHeader, ReactiveFormsModule, DatePipe],
  templateUrl: './offices.html',
})
export class OfficesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(MasterDataService);

  readonly statusOptions = ['ACTIVE', 'INACTIVE'];

  readonly items = signal<Office[]>([]);
  readonly zones = signal<Zone[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly success = signal('');
  showForm = false;
  editing: Office | null = null;

  readonly form = this.fb.nonNullable.group({
    zoneId: [0, Validators.required],
    officeCode: ['', Validators.required],
    status: ['ACTIVE'],
  });

  ngOnInit(): void {
    this.service.getZones().subscribe((zones) => this.zones.set(zones));
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.service
      .getOffices()
      .pipe(
        retry({ count: 1, delay: 400 }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (items) => {
          this.items.set(items);
        },
        error: (err) => {
          this.error.set(httpErrorMessage(err, 'Failed to load offices.'));
        },
      });
  }

  openCreate(): void {
    this.editing = null;
    this.success.set('');
    this.form.reset({ zoneId: 0, officeCode: '', status: 'ACTIVE' });
    this.showForm = true;
  }

  openEdit(item: Office): void {
    this.editing = item;
    this.success.set('');
    this.form.reset({
      zoneId: item.zoneId,
      officeCode: item.officeCode,
      status: item.status || 'ACTIVE',
    });
    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.editing = null;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set('');
    const editing = this.editing;
    const raw = this.form.getRawValue();
    const request = {
      zoneId: Number(raw.zoneId),
      officeCode: raw.officeCode.trim(),
      status: raw.status,
    };
    const op = editing
      ? this.service.updateOffice(editing.id, request)
      : this.service.createOffice(request);
    op.subscribe({
      next: () => {
        this.saving.set(false);
        this.showForm = false;
        this.editing = null;
        this.success.set(editing ? 'Office updated.' : 'Office created.');
        this.load();
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(
          err.status === 409 ? 'That office code already exists in the selected zone.' : 'Failed to save office.',
        );
      },
    });
  }

  remove(item: Office): void {
    if (!window.confirm(`Delete office with code "${item.officeCode}"?`)) {
      return;
    }
    this.service.deleteOffice(item.id).subscribe({
      next: () => {
        this.success.set(`Deleted office "${item.officeCode}".`);
        this.load();
      },
      error: () => {
        this.error.set('Failed to delete office. It may be in use.');
      },
    });
  }
}