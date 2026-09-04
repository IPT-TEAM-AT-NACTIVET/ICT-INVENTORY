import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
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

  readonly open = signal(false);

  readonly navItems: NavItem[] = [
    { path: '/users/dashboard', labelKey: 'nav.dashboard' },
    { path: '/users/assets', labelKey: 'nav.inventory' },
    { path: '/users/assets/register', labelKey: 'nav.registerAsset' },
    { path: '/users/profile', labelKey: 'nav.profile' },
  ];

  t(key: string): string {
    return this.translation.t(key);
  }

  toggleSidebar(): void {
    this.open.update((v) => !v);
  }

  logout(): void {
    this.auth.logout();
  }
}
