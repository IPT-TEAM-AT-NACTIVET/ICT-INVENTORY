import { Component, OnInit, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ReferenceService } from '../../../shared/services/reference.service';
import { Directorate, Office, Section, Unit, Zone } from '../../../core/models/master-data.model';
import { RegisterResponse } from '../../../core/models/user.model';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { TranslationService } from '../../../core/services/translation.service';
import { LanguageSwitcherComponent } from '../../../shared/components/language-switcher/language-switcher.component';
import { ThemeToggleComponent } from '../../../shared/components/theme-toggle/theme-toggle.component';
import { PasswordInputComponent } from '../../../shared/components/password-input/password-input.component';

@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    LanguageSwitcherComponent,
    ThemeToggleComponent,
    PasswordInputComponent,
  ],
  templateUrl: './register.html',
  styleUrl: '../login/login.css',
})
export class Register implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly reference = inject(ReferenceService);
  readonly translation = inject(TranslationService);

  readonly form = this.fb.nonNullable.group(
    {
      fullName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
      directorateId: [0, Validators.required],
      sectionId: [0],
      unitId: [0],
      zoneId: [0, Validators.required],
      officeId: [0],
    },
    { validators: matchPasswords },
  );

  readonly directorates = signal<Directorate[]>([]);
  readonly sections = signal<Section[]>([]);
  readonly units = signal<Unit[]>([]);
  readonly zones = signal<Zone[]>([]);
  readonly offices = signal<Office[]>([]);

  readonly errorMessage = signal('');
  readonly loading = signal(false);
  readonly registered = signal<RegisterResponse | null>(null);

  t(key: string): string {
    return this.translation.t(key);
  }

  ngOnInit(): void {
    this.reference.getDirectorates().subscribe((items) => this.directorates.set(items));
    this.reference.getUnits().subscribe((items) => this.units.set(items));
    this.reference.getZones().subscribe((items) => this.zones.set(items));

    this.form.controls.directorateId.valueChanges.subscribe((value) => {
      const directorateId = Number(value);
      this.sections.set([]);
      this.form.controls.sectionId.setValue(0, { emitEvent: false });
      if (directorateId) {
        this.reference.getSections(directorateId).subscribe((sections) => this.sections.set(sections));
      }
    });

    this.form.controls.zoneId.valueChanges.subscribe((value) => {
      const zoneId = Number(value);
      this.offices.set([]);
      this.form.controls.officeId.setValue(0, { emitEvent: false });
      if (zoneId) {
        this.reference.getOffices(zoneId).subscribe((offices) => this.offices.set(offices));
      }
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.errorMessage.set('');
    const raw = this.form.getRawValue();
    this.auth
      .register({
        fullName: raw.fullName,
        email: raw.email || undefined,
        phoneNumber: raw.phoneNumber || undefined,
        password: raw.password,
        confirmPassword: raw.confirmPassword,
        directorateId: this.num(raw.directorateId),
        sectionId: this.num(raw.sectionId),
        unitId: this.num(raw.unitId),
        zoneId: this.num(raw.zoneId),
        officeId: this.num(raw.officeId),
      })
      .subscribe({
        next: (response) => {
          this.loading.set(false);
          this.registered.set(response);
        },
        error: (err) => {
          this.loading.set(false);
          this.errorMessage.set(
            httpErrorMessage(err, 'Registration failed. The email may already be registered.'),
          );
        },
      });
  }

  private num(value: number): number | undefined {
    return value ? Number(value) : undefined;
  }
}

function matchPasswords(group: AbstractControl): { [key: string]: boolean } | null {
  const password = group.get('password')?.value as string | undefined;
  const confirmPassword = group.get('confirmPassword')?.value as string | undefined;
  return password && confirmPassword && password !== confirmPassword ? { mismatch: true } : null;
}
