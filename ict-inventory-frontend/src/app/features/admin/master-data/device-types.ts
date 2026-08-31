import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { MasterDataService } from '../../../shared/services/master-data.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { DeviceType } from '../../../core/models/master-data.model';
import { delay, finalize, retry } from 'rxjs';

@Component({
  selector: 'app-device-types',
  imports: [PageHeader, ReactiveFormsModule],
  templateUrl: './device-types.html',
})
export class DeviceTypesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(MasterDataService);

  readonly items = signal<DeviceType[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly success = signal('');
  showForm = false;
  editing: DeviceType | null = null;

  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    description: [''],
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.service
      .getDeviceTypes()
      .pipe(
        retry({ count: 1, delay: 400 }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (items) => {
          this.items.set(items);
        },
        error: (err) => {
          this.error.set(httpErrorMessage(err, 'Failed to load device types.'));
        },
      });
  }

  openCreate(): void {
    this.editing = null;
    this.success.set('');
    this.form.reset({ name: '', description: '' });
    this.showForm = true;
  }

  openEdit(item: DeviceType): void {
    this.editing = item;
    this.success.set('');
    this.form.reset({ name: item.name, description: item.description ?? '' });
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
      name: raw.name,
      description: raw.description || undefined,
    };
    const op = editing
      ? this.service.updateDeviceType(editing.id, request)
      : this.service.createDeviceType(request);
    op.subscribe({
      next: () => {
        this.saving.set(false);
        this.showForm = false;
        this.editing = null;
        this.success.set(editing ? 'Device type updated.' : 'Device type created.');
        this.load();
      },
      error: () => {
        this.saving.set(false);
        this.error.set('Failed to save device type.');
      },
    });
  }

  remove(item: DeviceType): void {
    if (!window.confirm(`Delete device type "${item.name}"?`)) {
      return;
    }
    this.service.deleteDeviceType(item.id).subscribe({
      next: () => {
        this.success.set(`Deleted "${item.name}".`);
        this.load();
      },
      error: () => {
        this.error.set('Failed to delete device type. It may be in use.');
      },
    });
  }
}