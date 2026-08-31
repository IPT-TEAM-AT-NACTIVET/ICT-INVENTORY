import { Component, inject } from '@angular/core';
import { TranslationService } from '../../../core/services/translation.service';

@Component({
  selector: 'app-language-switcher',
  imports: [],
  template: `
    <div class="lang-switcher" role="group" aria-label="Language">
      <button
        type="button"
        class="lang-btn"
        [class.active]="translation.lang() === 'en'"
        (click)="translation.setLanguage('en')"
      >
        EN
      </button>
      <button
        type="button"
        class="lang-btn"
        [class.active]="translation.lang() === 'sw'"
        (click)="translation.setLanguage('sw')"
      >
        SW
      </button>
    </div>
  `,
  styles: [
    `
      .lang-switcher {
        display: inline-flex;
        border: 1px solid var(--border);
        border-radius: 8px;
        overflow: hidden;
        background: var(--surface);
      }
      .lang-btn {
        border: none;
        background: var(--surface);
        color: var(--text-2);
        padding: 0.35rem 0.6rem;
        font-size: 0.75rem;
        font-weight: 700;
        cursor: pointer;
        letter-spacing: 0.04em;
      }
      .lang-btn.active {
        background: var(--accent);
        color: #fff;
      }
    `,
  ],
})
export class LanguageSwitcherComponent {
  readonly translation = inject(TranslationService);
}
