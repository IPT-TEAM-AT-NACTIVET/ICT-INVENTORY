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
  selector: 'app-staff-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ThemeToggleComponent, LanguageSwitcherComponent],
  templateUrl: './staff-layout.html',
  styleUrl: './staff-layout.css',
})
export class StaffLayout {
  readonly auth = inject(AuthService);
  readonly translation = inject(TranslationService);

  readonly open = signal(false);

  readonly navItems: NavItem[] = [
    { path: '/staff/dashboard', labelKey: 'nav.dashboard' },
    { path: '/staff/assets', labelKey: 'nav.myAssets' },
    { path: '/staff/assets/register', labelKey: 'nav.registerAsset' },
    { path: '/staff/profile', labelKey: 'nav.profile' },
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
