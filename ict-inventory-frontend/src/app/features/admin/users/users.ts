import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { UsersService } from '../../../core/services/users.service';
import { AuthService } from '../../../core/services/auth.service';
import { TranslationService } from '../../../core/services/translation.service';
import { httpErrorMessage } from '../../../shared/utils/http-errors';
import { UserAccount } from '../../../core/models/users.model';

@Component({
  selector: 'app-admin-users',
  imports: [PageHeader, StatusBadge, ReactiveFormsModule],
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class Users implements OnInit {
  private readonly usersService = inject(UsersService);
  private readonly auth = inject(AuthService);
  readonly translation = inject(TranslationService);

  readonly search = new FormControl('');
  readonly all = signal<UserAccount[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly notice = signal('');

  t(key: string): string {
    return this.translation.t(key);
  }

  ngOnInit(): void {
    this.load();
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

  approve(user: UserAccount): void {
    this.usersService.approve(user.id).subscribe({
      next: () => {
        this.notice.set(this.t('usersAdmin.approved'));
        this.load();
      },
      error: (err) => this.error.set(httpErrorMessage(err, 'Failed to approve user.')),
    });
  }

  activate(user: UserAccount): void {
    this.usersService.activate(user.id).subscribe({
      next: () => {
        this.notice.set('User activated successfully.');
        this.load();
      },
      error: (err) => this.error.set(httpErrorMessage(err, 'Failed to activate user.')),
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

  protected pending(): UserAccount[] {
    return this.all().filter((u) => !u.enabled);
  }

  protected activeUsers(): UserAccount[] {
    return this.all().filter((u) => u.enabled);
  }
}