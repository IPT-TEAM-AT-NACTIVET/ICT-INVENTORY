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
  selector: 'app-admin-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ThemeToggleComponent, LanguageSwitcherComponent],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.css',
})
export class AdminLayout {
  readonly auth = inject(AuthService);
  readonly translation = inject(TranslationService);

  readonly open = signal(false);

  readonly navItems: NavItem[] = [
    { path: '/admin/dashboard', labelKey: 'nav.dashboard' },
    { path: '/admin/inventory', labelKey: 'nav.inventory' },
    { path: '/admin/register-asset', labelKey: 'nav.registerAsset' },
    { path: '/admin/reports', labelKey: 'nav.reports' },
    { path: '/admin/users', labelKey: 'nav.users' },
    { path: '/admin/directorates', labelKey: 'nav.directorates' },
    { path: '/admin/sections', labelKey: 'nav.sections' },
    { path: '/admin/units', labelKey: 'nav.units' },
    { path: '/admin/zones', labelKey: 'nav.zones' },
    { path: '/admin/device-types', labelKey: 'nav.deviceTypes' },
    { path: '/admin/profile', labelKey: 'nav.profile' },
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
