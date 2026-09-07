import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { UsersService } from '../../../core/services/users.service';
import { AuthService } from '../../../core/services/auth.service';
import { TranslationService } from '../../../core/services/translation.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { UserAccount, UserCreateRequest } from '../../../core/models/users.model';
import { ReferenceService } from '../../../shared/services/reference.service';
import { Directorate, Section, Unit } from '../../../core/models/master-data.model';

@Component({
  selector: 'app-admin-users',
  imports: [PageHeader, StatusBadge, ReactiveFormsModule],
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class Users implements OnInit {
  private readonly usersService = inject(UsersService);
  private readonly reference = inject(ReferenceService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  readonly translation = inject(TranslationService);

  readonly search = new FormControl('');
  readonly all = signal<UserAccount[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly notice = signal('');

  readonly showNewUser = signal(false);
  readonly savingNewUser = signal(false);
  readonly newUserError = signal('');
  readonly directorates = signal<Directorate[]>([]);
  readonly sections = signal<Section[]>([]);
  readonly units = signal<Unit[]>([]);

  readonly newUserForm = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    phoneNumber: ['', [Validators.required, Validators.pattern(/^[0-9+\-\s]{9,}$/)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required]],
    directorateId: [0 as number],
    sectionId: [0 as number],
    unitId: [0 as number],
  });

  t(key: string): string {
    return this.translation.t(key);
  }

  ngOnInit(): void {
    this.load();
    this.reference.getDirectorates().subscribe((directorates) => this.directorates.set(directorates));
    this.reference.getUnits().subscribe((units) => this.units.set(units));

    this.newUserForm.controls.directorateId.valueChanges.subscribe((value) => {
      const directorateId = Number(value);
      this.sections.set([]);
      this.newUserForm.controls.sectionId.setValue(0, { emitEvent: false });
      if (directorateId) {
        this.reference.getSections(directorateId).subscribe((sections) => this.sections.set(sections));
      }
    });
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.notice.set('');
    this.usersService
      .findAll(this.search.value ?? undefined)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (users) => this.all.set(users),
        error: (err) => this.error.set(httpErrorMessage(err, 'Failed to load users.')),
      });
  }

  applyFilters(): void {
    this.load();
  }

  resetFilters(): void {
    this.search.setValue('');
    this.load();
  }

  openNewUser(): void {
    this.newUserForm.reset();
    this.sections.set([]);
    this.newUserError.set('');
    this.showNewUser.set(true);
  }

  closeNewUser(): void {
    if (!this.savingNewUser()) {
      this.showNewUser.set(false);
    }
  }

  saveNewUser(): void {
    if (this.newUserForm.invalid) {
      this.newUserForm.markAllAsTouched();
      return;
    }
    const { password, confirmPassword, directorateId, sectionId, unitId, ...rest } = this.newUserForm.getRawValue();
    if (password !== confirmPassword) {
      this.newUserError.set('Passwords do not match.');
      return;
    }
    this.savingNewUser.set(true);
    this.newUserError.set('');
    const request: UserCreateRequest = {
      ...rest,
      password,
      directorateId: directorateId ? Number(directorateId) : null,
      sectionId: sectionId ? Number(sectionId) : null,
      unitId: unitId ? Number(unitId) : null,
    };
    this.usersService.create(request).subscribe({
      next: () => {
        this.savingNewUser.set(false);
        this.showNewUser.set(false);
        this.notice.set(this.t('usersAdmin.userCreated'));
        this.load();
      },
      error: (err) => {
        this.savingNewUser.set(false);
        this.newUserError.set(err.error?.message ?? 'Failed to create user. Please try again.');
      },
    });
  }

  deactivate(user: UserAccount): void {
    this.usersService.deactivate(user.id).subscribe({
      next: () => {
        this.notice.set(this.t('common.deactivate'));
        this.load();
      },
      error: (err) => this.error.set(httpErrorMessage(err, 'Failed to deactivate user.')),
    });
  }

  activateUser(user: UserAccount): void {
    this.usersService.activate(user.id).subscribe({
      next: () => {
        this.notice.set('User activated successfully.');
        this.load();
      },
      error: (err) => this.error.set(httpErrorMessage(err, 'Failed to activate user.')),
    });
  }

  remove(user: UserAccount): void {
    if (!window.confirm(this.t('usersAdmin.confirmDelete'))) {
      return;
    }
    this.usersService.delete(user.id).subscribe({
      next: () => this.load(),
      error: (err) => {
        this.error.set(
          err.error?.message ?? httpErrorMessage(err, 'Failed to delete user.'),
        );
      },
    });
  }

  isCurrentUser(user: UserAccount): boolean {
    return this.auth.user()?.id === user.id;
  }

  protected allUsers(): UserAccount[] {
    return this.all();
  }
}
