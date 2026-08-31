import { Injectable, signal } from '@angular/core';
import { en } from '../../shared/i18n/en';
import { sw } from '../../shared/i18n/sw';

export type Language = 'en' | 'sw';
export type Dict = typeof en;

export type TranslationKey = string;

const STORAGE_KEY = 'ict-language';
const DICTS: Record<Language, Dict> = { en, sw };

@Injectable({ providedIn: 'root' })
export class TranslationService {
  readonly lang = signal<Language>(this.getInitialLanguage());

  t(key: string): string {
    const dict = DICTS[this.lang()];
    return getByPath(dict, key) ?? key;
  }

  setLanguage(lang: Language): void {
    this.lang.set(lang);
    localStorage.setItem(STORAGE_KEY, lang);
    document.documentElement.setAttribute('lang', lang);
  }

  toggle(): void {
    this.setLanguage(this.lang() === 'en' ? 'sw' : 'en');
  }

  private getInitialLanguage(): Language {
    const stored = localStorage.getItem(STORAGE_KEY);
    return stored === 'sw' ? 'sw' : 'en';
  }
}

function getByPath(source: unknown, path: string): string | undefined {
  let cur: unknown = source;
  const parts = path.split('.');
  for (const part of parts) {
    if (cur && typeof cur === 'object' && part in (cur as Record<string, unknown>)) {
      cur = (cur as Record<string, unknown>)[part];
    } else {
      return undefined;
    }
  }
  return typeof cur === 'string' ? cur : undefined;
}
