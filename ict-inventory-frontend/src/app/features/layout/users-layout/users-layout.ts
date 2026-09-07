import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { TranslationService } from '../../../core/services/translation.service';
import { ThemeToggleComponent } from '../../../shared/components/theme-toggle/theme-toggle.component';
import { LanguageSwitcherComponent } from '../../../shared/components/language-switcher/language-switcher.component';

interface NavItem {
  path: string;
  labelKey: string;
}

@Component({
  selector: 'app-users-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ThemeToggleComponent, LanguageSwitcherComponent],
  templateUrl: './users-layout.html',
  styleUrl: './users-layout.css',
})
export class UsersLayout {
  readonly auth = inject(AuthService);
  readonly translation = inject(TranslationService);
  private readonly router = inject(Router);

  readonly open = signal(false);
  readonly menuOpen = signal(false);

  readonly navItems: NavItem[] = [
    { path: '/users/dashboard', labelKey: 'nav.dashboard' },
    { path: '/users/assets', labelKey: 'nav.inventory' },
  ];

  t(key: string): string {
    return this.translation.t(key);
  }

  toggleSidebar(): void {
    this.open.update((v) => !v);
  }

  goToProfile(): void {
    this.menuOpen.set(false);
    void this.router.navigate(['/users', 'profile']);
  }

  logout(): void {
    this.menuOpen.set(false);
    this.auth.logout();
  }
}
