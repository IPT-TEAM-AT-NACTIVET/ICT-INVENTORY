import { Component, inject } from '@angular/core';
import { ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'app-theme-toggle',
  imports: [],
  template: `
    <button
      type="button"
      class="icon-toggle"
      (click)="theme.toggle()"
      [attr.aria-label]="theme.theme() === 'light' ? 'Switch to dark mode' : 'Switch to light mode'"
    >
      @if (theme.theme() === 'light') {
        <span class="ic ic-moon" title="Dark mode"></span>
      } @else {
        <span class="ic ic-sun" title="Light mode"></span>
      }
    </button>
  `,
  styles: [
    `
      .icon-toggle {
        border: 1px solid var(--border);
        background: var(--surface);
        color: var(--text);
        width: 36px;
        height: 36px;
        border-radius: 8px;
        cursor: pointer;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        transition: background 0.15s, border-color 0.15s;
      }
      .icon-toggle:hover {
        background: var(--surface-hover, rgba(0, 0, 0, 0.04));
      }
      .ic {
        width: 16px;
        height: 16px;
        display: inline-block;
        border-radius: 50%;
      }
      .ic-moon {
        background: currentColor;
        box-shadow: inset -4px -3px 0 0 var(--bg);
      }
      .ic-sun {
        background: transparent;
        border: 1.5px solid currentColor;
        box-shadow: 0 0 0 3px var(--bg), 0 0 0 4px currentColor;
      }
    `,
  ],
})
export class ThemeToggleComponent {
  readonly theme = inject(ThemeService);
}
