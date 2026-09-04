import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
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
        this.router.navigate(['/admin/dashboard']);
      },
      error: (err: { status?: number; error?: { message?: string } }) => {
        this.loading.set(false);
        const message = err.error?.message ?? '';
        if (err.status === 401 && message.toLowerCase().includes('not yet approved')) {
          this.errorMessage.set(this.t('register.pending'));
        } else if (err.status === 401) {
          this.errorMessage.set(this.t('login.invalidCredentials'));
        } else {
          this.errorMessage.set('Login failed. Please try again.');
        }
      },
    });
  }
}
