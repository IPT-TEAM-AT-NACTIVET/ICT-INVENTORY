import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { TranslationService } from '../../../core/services/translation.service';
import { LanguageSwitcherComponent } from '../../../shared/components/language-switcher/language-switcher.component';
import { ThemeToggleComponent } from '../../../shared/components/theme-toggle/theme-toggle.component';
import { PasswordInputComponent } from '../../../shared/components/password-input/password-input.component';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, LanguageSwitcherComponent, ThemeToggleComponent, PasswordInputComponent],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly translation = inject(TranslationService);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  readonly errorMessage = signal('');
  readonly loading = signal(false);

  t(key: string): string {
    return this.translation.t(key);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.errorMessage.set('');
    const { email, password } = this.form.getRawValue();
    this.auth.login({ email, password }).subscribe({
      next: () => {
        if (this.auth.isAdmin()) {
          this.router.navigate(['/admin/dashboard']);
          return;
        }
        const user = this.auth.user();
        if (user?.setupCompleted === false) {
          this.router.navigate(['/staff/setup']);
          return;
        }
        this.router.navigate(['/staff/dashboard']);
      },
      error: (err: { status?: number }) => {
        this.loading.set(false);
        this.errorMessage.set(
          err.status === 401 ? this.t('login.invalidCredentials') : 'Login failed. Please try again.',
        );
      },
    });
  }
}
