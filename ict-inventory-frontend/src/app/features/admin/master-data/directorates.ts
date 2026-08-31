import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { MasterDataService } from '../../../shared/services/master-data.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { Directorate } from '../../../core/models/master-data.model';
import { delay, finalize, retry } from 'rxjs';

@Component({
  selector: 'app-directorates',
  imports: [PageHeader, ReactiveFormsModule],
  templateUrl: './directorates.html',
})
export class DirectoratesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(MasterDataService);

  readonly items = signal<Directorate[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly success = signal('');
  showForm = false;
  editing: Directorate | null = null;

  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    code: [''],
    description: [''],
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.service
      .getDirectorates()
      .pipe(
        retry({ count: 1, delay: 400 }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (items) => {
          this.items.set(items);
        },
        error: (err) => {
          this.error.set(httpErrorMessage(err, 'Failed to load directorates.'));
        },
      });
  }

  openCreate(): void {
    this.editing = null;
    this.success.set('');
    this.form.reset({ name: '', code: '', description: '' });
    this.showForm = true;
  }

  openEdit(item: Directorate): void {
    this.editing = item;
    this.success.set('');
    this.form.reset({ name: item.name, code: item.code ?? '', description: item.description ?? '' });
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
      code: raw.code || undefined,
      description: raw.description || undefined,
    };
    const op = editing
      ? this.service.updateDirectorate(editing.id, request)
      : this.service.createDirectorate(request);
    op.subscribe({
      next: () => {
        this.saving.set(false);
        this.showForm = false;
        this.editing = null;
        this.success.set(editing ? 'Directorate updated.' : 'Directorate created.');
        this.load();
      },
      error: () => {
        this.saving.set(false);
        this.error.set('Failed to save directorate.');
      },
    });
  }

  remove(item: Directorate): void {
    if (!window.confirm(`Delete directorate "${item.name}"?`)) {
      return;
    }
    this.service.deleteDirectorate(item.id).subscribe({
      next: () => {
        this.success.set(`Deleted "${item.name}".`);
        this.load();
      },
      error: () => {
        this.error.set('Failed to delete directorate. It may be in use.');
      },
    });
  }
}