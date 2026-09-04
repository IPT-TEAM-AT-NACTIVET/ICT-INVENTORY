import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { TranslationService } from '../../../core/services/translation.service';
import { LanguageSwitcherComponent } from '../../../shared/components/language-switcher/language-switcher.component';
import { ThemeToggleComponent } from '../../../shared/components/theme-toggle/theme-toggle.component';
import { PasswordInputComponent } from '../../../shared/components/password-input/password-input.component';
import { ReferenceService } from '../../../shared/services/reference.service';
import { Directorate, Section, Unit } from '../../../core/models/master-data.model';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink, LanguageSwitcherComponent, ThemeToggleComponent, PasswordInputComponent],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly reference = inject(ReferenceService);
  readonly translation = inject(TranslationService);

  readonly form = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    phoneNumber: ['', [Validators.required, Validators.pattern(/^[0-9+\-\s]{9,}$/)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required]],
    directorateId: [0],
    sectionId: [0],
    unitId: [0],
  });

  readonly directorates = signal<Directorate[]>([]);
  readonly sections = signal<Section[]>([]);
  readonly units = signal<Unit[]>([]);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly loading = signal(false);

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
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { password, confirmPassword, directorateId, sectionId, unitId, ...rest } = this.form.getRawValue();
    if (password !== confirmPassword) {
      this.errorMessage.set('Passwords do not match.');
      return;
    }
    this.loading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');
    this.auth
      .register({
        ...rest,
        password,
        directorateId: directorateId ? Number(directorateId) : null,
        sectionId: sectionId ? Number(sectionId) : null,
        unitId: unitId ? Number(unitId) : null,
      })
      .subscribe({
        next: () => {
          this.loading.set(false);
          this.successMessage.set(this.t('register.success'));
        },
        error: (err: { status?: number; error?: { message?: string } }) => {
          this.loading.set(false);
          this.errorMessage.set(err.error?.message ?? 'Registration failed. Please try again.');
        },
      });
  }
}