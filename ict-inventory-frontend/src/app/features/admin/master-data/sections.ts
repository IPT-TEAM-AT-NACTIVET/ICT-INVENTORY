import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { MasterDataService } from '../../../shared/services/master-data.service';
import { ReferenceService } from '../../../shared/services/reference.service';
import { TranslationService } from '../../../core/services/translation.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { Directorate, Section } from '../../../core/models/master-data.model';
import { delay, finalize, retry } from 'rxjs';

@Component({
  selector: 'app-sections',
  imports: [PageHeader, ReactiveFormsModule],
  templateUrl: './sections.html',
})
export class SectionsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(MasterDataService);
  private readonly reference = inject(ReferenceService);
  private readonly translation = inject(TranslationService);

  t = (key: string, params?: Record<string, string | number>) => this.translation.t(key, params);

  readonly items = signal<Section[]>([]);
  readonly directorates = signal<Directorate[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly success = signal('');
  showForm = false;
  editing: Section | null = null;

  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    code: [''],
    description: [''],
    directorateId: [0, Validators.required],
  });

  ngOnInit(): void {
    this.reference.getDirectorates().subscribe({
      next: (directorates) => {
        this.directorates.set(directorates);
      },
    });
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.service
      .getSections()
      .pipe(
        retry({ count: 1, delay: 400 }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (items) => {
          this.items.set(items);
        },
        error: (err) => {
          this.error.set(httpErrorMessage(err, this.t('master.sections.loadError'), this.t));
        },
      });
  }

  openCreate(): void {
    this.editing = null;
    this.success.set('');
    this.form.reset({ name: '', code: '', description: '', directorateId: 0 });
    this.showForm = true;
  }

  openEdit(item: Section): void {
    this.editing = item;
    this.success.set('');
    this.form.reset({
      name: item.name,
      code: item.code ?? '',
      description: item.description ?? '',
      directorateId: item.directorateId,
    });
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
      code: raw.code || undefined,
      description: raw.description || undefined,
      directorateId: Number(raw.directorateId),
    };
    const op = editing
      ? this.service.updateSection(editing.id, request)
      : this.service.createSection(request);
    op.subscribe({
      next: () => {
        this.saving.set(false);
        this.showForm = false;
        this.editing = null;
        this.success.set(editing ? this.t('master.sections.updated') : this.t('master.sections.created'));
        this.load();
      },
      error: () => {
        this.saving.set(false);
        this.error.set(this.t('master.sections.saveError'));
      },
    });
  }

  remove(item: Section): void {
    if (!window.confirm(this.t('master.sections.confirmDelete', { name: item.name }))) {
      return;
    }
    this.service.deleteSection(item.id).subscribe({
      next: () => {
        this.success.set(this.t('master.sections.deleted', { name: item.name }));
        this.load();
      },
      error: () => {
        this.error.set(this.t('master.sections.deleteError'));
      },
    });
  }
}