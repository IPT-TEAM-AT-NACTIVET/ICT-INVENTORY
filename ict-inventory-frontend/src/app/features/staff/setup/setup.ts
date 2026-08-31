import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { ProfileService } from '../../../core/services/profile.service';
import { ReferenceService } from '../../../shared/services/reference.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { Directorate, Office, Section, Unit } from '../../../core/models/master-data.model';
import { AuthService } from '../../../core/services/auth.service';
import { delay, finalize, retry } from 'rxjs';

@Component({
  selector: 'app-setup',
  imports: [PageHeader, ReactiveFormsModule, RouterLink],
  templateUrl: './setup.html',
})
export class Setup implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly profileService = inject(ProfileService);
  private readonly reference = inject(ReferenceService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    phoneNumber: ['', Validators.required],
    directorateId: [0, Validators.required],
    sectionId: [0],
    unitId: [0],
  });

  readonly directorates = signal<Directorate[]>([]);
  readonly sections = signal<Section[]>([]);
  readonly units = signal<Unit[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');

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

    this.profileService
      .getMe()
      .pipe(
        retry({ count: 1, delay: 400 }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (profile) => {
          if (profile.setupCompleted) {
            const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
            this.router.navigate([returnUrl ?? '/staff/assets']);
            return;
          }
          this.form.patchValue({
            email: profile.email ?? '',
            phoneNumber: profile.phoneNumber ?? '',
            directorateId: profile.directorateId ?? 0,
            sectionId: profile.sectionId ?? 0,
            unitId: profile.unitId ?? 0,
          });
          if (profile.directorateId) {
            this.reference.getSections(profile.directorateId).subscribe((sections) => this.sections.set(sections));
          }
        },
        error: (err) => {
          this.error.set(httpErrorMessage(err, 'Failed to load your profile.'));
        },
      });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set('');
    const raw = this.form.getRawValue();
    this.profileService
      .updateMe({
        email: raw.email,
        phoneNumber: raw.phoneNumber,
        directorateId: Number(raw.directorateId),
        sectionId: raw.sectionId ? Number(raw.sectionId) : null,
        unitId: raw.unitId ? Number(raw.unitId) : null,
      })
      .subscribe({
        next: (profile) => {
          this.auth.updateStoredUser({
            id: profile.id,
            employeeId: profile.employeeId,
            fullName: profile.fullName,
            email: profile.email,
            phoneNumber: profile.phoneNumber,
            setupCompleted: profile.setupCompleted,
            role: this.auth.user()?.role ?? 'STAFF',
          });
          const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
          this.router.navigate([returnUrl ?? '/staff/assets']);
        },
        error: (err) => {
          this.saving.set(false);
          this.error.set(
            err.status === 409
              ? 'That email is already in use. Please use a different email.'
              : 'Failed to complete your setup. Please check the details and try again.',
          );
        },
      });
  }
}