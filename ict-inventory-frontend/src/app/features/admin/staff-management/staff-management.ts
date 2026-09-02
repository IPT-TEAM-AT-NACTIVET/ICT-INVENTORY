import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { StaffService } from '../../../core/services/staff.service';
import { ReferenceService } from '../../../shared/services/reference.service';
import { TranslationService } from '../../../core/services/translation.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { Staff, StaffCreateRequest, StaffUpdateRequest } from '../../../core/models/staff.model';
import { Directorate, Section, Unit } from '../../../core/models/master-data.model';
import { delay, finalize, retry } from 'rxjs';

@Component({
  selector: 'app-staff-management',
  imports: [PageHeader, ReactiveFormsModule, StatusBadge, DatePipe],
  templateUrl: './staff-management.html',
})
export class StaffManagement implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly staffService = inject(StaffService);
  private readonly reference = inject(ReferenceService);
  private readonly translation = inject(TranslationService);

  readonly items = signal<Staff[]>([]);
  readonly searchTerm = signal('');
  readonly directorates = signal<Directorate[]>([]);
  readonly sections = signal<Section[]>([]);
  readonly units = signal<Unit[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly success = signal('');
  readonly credentialsPanel = signal<{ title: string; staff: Staff } | null>(null);
  readonly viewPanel = signal<Staff | null>(null);
  showForm = false;
  editing: Staff | null = null;

  readonly form = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    email: [''],
    phoneNumber: [''],
    directorateId: [0],
    sectionId: [0],
    unitId: [0],
  });

  t(key: string): string {
    return this.translation.t(key);
  }

  ngOnInit(): void {
    this.reference.getDirectorates().subscribe((directorates) => this.directorates.set(directorates));
    this.reference.getUnits().subscribe((units) => this.units.set(units));

    this.form.controls.directorateId.valueChanges.subscribe((value) => {
      const directorateId = Number(value);
      this.sections.set([]);
      this.form.controls.sectionId.setValue(0, { emitEvent: false });
      if (directorateId) {
        this.reference.getSections(directorateId).subscribe((sections) => this.sections.set(sections));
      }
    });

    this.load();
  }

  load(search = this.searchTerm()): void {
    this.loading.set(true);
    this.error.set('');
    this.staffService
      .getStaff(search.trim() || undefined)
      .pipe(
        retry({ count: 1, delay: 400 }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (items) => {
          this.items.set(items);
        },
        error: (err) => {
          this.error.set(httpErrorMessage(err, 'Failed to load staff.'));
        },
      });
  }

  onSearch(value: string): void {
    this.searchTerm.set(value);
    this.load(value);
  }

  openCreate(): void {
    this.editing = null;
    this.credentialsPanel.set(null);
    this.success.set('');
    this.showForm = true;
    this.form.reset({ directorateId: 0, sectionId: 0, unitId: 0 });
  }

  openEdit(item: Staff): void {
    this.editing = item;
    this.credentialsPanel.set(null);
    this.success.set('');
    this.showForm = true;
    this.form.patchValue({
      fullName: item.fullName,
      email: item.email ?? '',
      phoneNumber: item.phoneNumber ?? '',
      directorateId: item.directorateId ?? 0,
      sectionId: item.sectionId ?? 0,
      unitId: item.unitId ?? 0,
    });
    if (item.directorateId) {
      this.reference.getSections(item.directorateId).subscribe((sections) => this.sections.set(sections));
    }
  }

  cancelForm(): void {
    this.showForm = false;
    this.editing = null;
    this.credentialsPanel.set(null);
  }

  view(item: Staff): void {
    this.viewPanel.set(item);
  }

  closeView(): void {
    this.viewPanel.set(null);
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
    const op = editing
      ? this.staffService.update(editing.id, this.buildUpdateRequest(raw))
      : this.staffService.create(this.buildCreateRequest(raw));
    op.subscribe({
      next: (item) => {
        this.saving.set(false);
        this.showForm = false;
        this.editing = null;
        if (editing) {
          this.credentialsPanel.set(null);
          this.success.set('Staff member updated.');
        } else {
          this.credentialsPanel.set({ title: 'has been created', staff: item });
          this.success.set('Share the generated credentials with them so they can log in.');
        }
        this.load();
      },
      error: () => {
        this.saving.set(false);
        this.error.set('Failed to save staff member. Check for a duplicate email.');
      },
    });
  }

  private buildUpdateRequest(raw: {
    fullName: string;
    email: string;
    phoneNumber: string;
    directorateId: number;
    sectionId: number;
    unitId: number | null;
  }): StaffUpdateRequest {
    return {
      fullName: raw.fullName,
      email: raw.email || undefined,
      phoneNumber: raw.phoneNumber || undefined,
      directorateId: raw.directorateId ? Number(raw.directorateId) : null,
      sectionId: raw.sectionId ? Number(raw.sectionId) : null,
      unitId: raw.unitId ? Number(raw.unitId) : null,
    };
  }

  private buildCreateRequest(raw: { fullName: string }): StaffCreateRequest {
    return { fullName: raw.fullName };
  }

  toggleEnabled(item: Staff): void {
    const action = item.enabled ? 'Deactivate' : 'Activate';
    if (!window.confirm(`${action} ${item.fullName}?`)) {
      return;
    }
    const op = item.enabled
      ? this.staffService.deactivate(item.id)
      : this.staffService.activate(item.id);
    op.subscribe({
      next: () => {
        this.success.set(`${item.fullName} ${action.toLowerCase()}d.`);
        this.load();
      },
      error: () => {
        this.error.set('Failed to update staff status.');
      },
    });
  }

  resetPassword(item: Staff): void {
    if (
      !window.confirm(
        `Generate a new initial password for ${item.fullName}? Their current password will stop working.`,
      )
    ) {
      return;
    }
    this.staffService.resetPassword(item.id).subscribe({
      next: (updated) => {
        this.credentialsPanel.set({ title: 'password has been reset', staff: updated });
        this.success.set('New initial password generated. Share it with the staff member.');
        this.load();
      },
      error: () => {
        this.error.set('Failed to reset the password.');
      },
    });
  }

  deleteStaff(item: Staff): void {
    if (!window.confirm(`Delete ${item.fullName} (${item.employeeId})? This cannot be undone.`)) {
      return;
    }
    this.staffService.delete(item.id).subscribe({
      next: () => {
        this.success.set(`${item.fullName} deleted.`);
        this.load();
      },
      error: (err) => {
        this.error.set(
          err.status === 409
            ? err.error?.message ?? 'Cannot delete staff: assets are registered to them.'
            : 'Failed to delete staff member.',
        );
      },
    });
  }

  async copy(text: string, field: string): Promise<void> {
    try {
      await navigator.clipboard.writeText(text);
      this.success.set(`${field} copied to clipboard.`);
    } catch {
      this.error.set('Could not copy to clipboard.');
    }
  }
}