import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { MasterDataService } from '../../../shared/services/master-data.service';
import { TranslationService } from '../../../core/services/translation.service';
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
  private readonly translation = inject(TranslationService);

  t = (key: string, params?: Record<string, string | number>) => this.translation.t(key, params);

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
          this.error.set(httpErrorMessage(err, this.t('master.deviceTypes.loadError'), this.t));
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
    if (!this.saving()) {
      this.showForm = false;
      this.editing = null;
    }
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
        this.success.set(editing ? this.t('master.deviceTypes.updated') : this.t('master.deviceTypes.created'));
        this.load();
      },
      error: () => {
        this.saving.set(false);
        this.error.set(this.t('master.deviceTypes.saveError'));
      },
    });
  }

  remove(item: DeviceType): void {
    if (!window.confirm(this.t('master.deviceTypes.confirmDelete', { name: item.name }))) {
      return;
    }
    this.service.deleteDeviceType(item.id).subscribe({
      next: () => {
        this.success.set(this.t('master.deviceTypes.deleted', { name: item.name }));
        this.load();
      },
      error: () => {
        this.error.set(this.t('master.deviceTypes.deleteError'));
      },
    });
  }
}