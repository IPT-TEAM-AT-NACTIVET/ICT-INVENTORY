import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ProfileService } from '../../core/services/profile.service';
import { ReferenceService } from '../../shared/services/reference.service';
import { httpErrorMessage } from '../../shared/utils/http-errors';
import { Profile } from '../../core/models/profile.model';
import { Directorate, Office, Section, Unit } from '../../core/models/master-data.model';
import { PageHeader } from '../../shared/components/page-header/page-header';
import { TranslationService } from '../../core/services/translation.service';
import { delay, finalize, retry } from 'rxjs';

@Component({
  selector: 'app-profile',
  imports: [PageHeader, ReactiveFormsModule, RouterLink],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class ProfilePage implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly profileService = inject(ProfileService);
  private readonly reference = inject(ReferenceService);
  private readonly auth = inject(AuthService);
  readonly translation = inject(TranslationService);

  readonly profile = signal<Profile | null>(null);
  readonly form = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    email: [''],
    phoneNumber: [''],
    directorateId: [0],
    sectionId: [0],
    unitId: [0],
  });
  readonly directorates = signal<Directorate[]>([]);
  readonly sections = signal<Section[]>([]);
  readonly units = signal<Unit[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly success = signal('');

  get needsSetup(): boolean {
    const p = this.profile();
    return p?.role === 'STAFF' && !p.setupCompleted;
  }

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

    this.profileService
      .getMe()
      .pipe(
        retry({ count: 1, delay: 400 }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (profile) => {
          this.profile.set(profile);
          this.form.patchValue({
            fullName: profile.fullName,
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
    this.success.set('');
    this.error.set('');
    const raw = this.form.getRawValue();
    this.profileService
      .updateMe({
        fullName: raw.fullName,
        email: raw.email || undefined,
        phoneNumber: raw.phoneNumber || undefined,
        directorateId: raw.directorateId ? Number(raw.directorateId) : null,
        sectionId: raw.sectionId ? Number(raw.sectionId) : null,
        unitId: raw.unitId ? Number(raw.unitId) : null,
      })
      .subscribe({
        next: (profile) => {
          this.saving.set(false);
          this.profile.set(profile);
          this.success.set('Profile updated successfully.');
          this.auth.updateStoredUser({
            id: profile.id,
            employeeId: profile.employeeId,
            fullName: profile.fullName,
            email: profile.email,
            phoneNumber: profile.phoneNumber,
            setupCompleted: profile.setupCompleted,
            role: this.auth.user()?.role ?? 'STAFF',
          });
        },
        error: () => {
          this.saving.set(false);
          this.error.set('Failed to update your profile.');
        },
      });
  }
}