import { Component, inject } from '@angular/core';
import { ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'app-theme-toggle',
  imports: [],
  template: `
    <div class="theme-toggle" role="group" aria-label="Theme">
      <button
        type="button"
        class="theme-option"
        [class.active]="theme.theme() === 'light'"
        (click)="theme.set('light')"
        [attr.aria-label]="'Light mode'"
        [attr.aria-pressed]="theme.theme() === 'light'"
        title="Light mode"
      >
        <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <circle cx="12" cy="12" r="4"></circle>
          <path d="M12 2v2"></path>
          <path d="M12 20v2"></path>
          <path d="m4.93 4.93 1.41 1.41"></path>
          <path d="m17.66 17.66 1.41 1.41"></path>
          <path d="M2 12h2"></path>
          <path d="M20 12h2"></path>
          <path d="m6.34 17.66-1.41 1.41"></path>
          <path d="m19.07 4.93-1.41 1.41"></path>
        </svg>
      </button>
      <button
        type="button"
        class="theme-option"
        [class.active]="theme.theme() === 'dark'"
        (click)="theme.set('dark')"
        [attr.aria-label]="'Dark mode'"
        [attr.aria-pressed]="theme.theme() === 'dark'"
        title="Dark mode"
      >
        <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
        </svg>
      </button>
    </div>
  `,
  styles: [
    `
      :host {
        display: inline-flex;
        align-items: center;
      }
      .theme-toggle {
        display: inline-flex;
        align-items: center;
        gap: 2px;
        padding: 2px;
        background: var(--surface);
        border: 1px solid var(--border);
        border-radius: 8px;
      }
      .theme-option {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 26px;
        height: 24px;
        margin: 0;
        padding: 0;
        border: none;
        border-radius: 6px;
        background: transparent;
        color: var(--text-2);
        cursor: pointer;
        transition: background 0.15s, color 0.15s, box-shadow 0.15s;
      }
      .theme-option:hover {
        color: var(--text);
        background: var(--surface-hover);
      }
      .theme-option.active {
        background: var(--accent);
        color: #fff;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
      }
      .theme-option .icon {
        width: 14px;
        height: 14px;
        display: block;
      }
    `,
  ],
})
export class ThemeToggleComponent {
  readonly theme = inject(ThemeService);
}