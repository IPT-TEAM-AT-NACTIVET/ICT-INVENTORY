import { Component, computed, inject, signal } from '@angular/core';
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
  selector: 'app-admin-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ThemeToggleComponent, LanguageSwitcherComponent],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.css',
})
export class AdminLayout {
  readonly auth = inject(AuthService);
  readonly translation = inject(TranslationService);
  private readonly router = inject(Router);

  readonly open = signal(false);
  readonly menuOpen = signal(false);
  readonly settingsOpen = signal(false);

  readonly navItems: NavItem[] = [
    { path: '/admin/dashboard', labelKey: 'nav.dashboard' },
    { path: '/admin/inventory', labelKey: 'nav.inventory' },
    { path: '/admin/reports', labelKey: 'nav.reports' },
  ];

  readonly settingsItems: NavItem[] = [
    { path: '/admin/users', labelKey: 'nav.users' },
    { path: '/admin/directorates', labelKey: 'nav.directorates' },
    { path: '/admin/sections', labelKey: 'nav.sections' },
    { path: '/admin/units', labelKey: 'nav.units' },
    { path: '/admin/zones', labelKey: 'nav.zones' },
    { path: '/admin/device-types', labelKey: 'nav.deviceTypes' },
  ];

  readonly hasActiveSettingsChild = computed(() => {
    const url = this.router.url;
    return this.settingsItems.some((item) => url.startsWith(item.path));
  });

  t(key: string): string {
    return this.translation.t(key);
  }

  toggleSidebar(): void {
    this.open.update((v) => !v);
  }

  toggleSettings(): void {
    const willOpen = !this.settingsOpen();
    this.settingsOpen.set(willOpen);
  }

  isSettingsActive(): boolean {
    return this.hasActiveSettingsChild() || this.settingsOpen();
  }

  goToProfile(): void {
    this.menuOpen.set(false);
    const section = this.router.url.startsWith('/users') ? '/users' : '/admin';
    void this.router.navigate([section, 'profile']);
  }

  logout(): void {
    this.menuOpen.set(false);
    this.auth.logout();
  }
}
