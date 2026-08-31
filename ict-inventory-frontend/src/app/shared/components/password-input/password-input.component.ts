import { Component, forwardRef, inject, input, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { TranslationService } from '../../../core/services/translation.service';

@Component({
  selector: 'app-password-input',
  imports: [],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => PasswordInputComponent),
      multi: true,
    },
  ],
  template: `
    <div class="password-wrap">
      <input
        [id]="inputId()"
        class="input password-input"
        type="text"
        [attr.type]="show() ? 'text' : 'password'"
        [attr.autocomplete]="autocomplete()"
        [value]="value()"
        (input)="onInput($event)"
        (blur)="onBlur()"
        [placeholder]="placeholder()"
      />
      <button
        type="button"
        class="password-toggle"
        [attr.aria-label]="show() ? t('login.hidePassword') : t('login.showPassword')"
        (click)="toggle()"
      >
        <span class="pw-icon" [class.off]="!show()"></span>
      </button>
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
      }
      .password-wrap {
        position: relative;
      }
      .password-input {
        width: 100%;
        padding-right: 2.75rem;
      }
      .password-toggle {
        position: absolute;
        right: 0.25rem;
        top: 50%;
        transform: translateY(-50%);
        border: none;
        background: transparent;
        cursor: pointer;
        padding: 0.4rem;
        border-radius: 6px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        color: var(--text-2);
      }
      .password-toggle:hover {
        color: var(--accent);
        background: var(--surface-hover, rgba(0, 0, 0, 0.04));
      }
      .pw-icon {
        width: 18px;
        height: 14px;
        border: 1.5px solid currentColor;
        border-radius: 3px;
        position: relative;
        display: inline-block;
      }
      .pw-icon.off {
        opacity: 0.45;
      }
      .pw-icon::after {
        content: '';
        position: absolute;
        left: 50%;
        top: 50%;
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: currentColor;
        transform: translate(-50%, -50%);
      }
    `,
  ],
})
export class PasswordInputComponent implements ControlValueAccessor {
  readonly inputId = input<string>('password');
  readonly placeholder = input<string>('');
  readonly autocomplete = input<string>('current-password');

  private readonly translation = inject(TranslationService);

  t = (k: string) => this.translation.t(k);

  readonly show = signal(false);
  readonly value = signal('');

  private onChange: (v: string) => void = () => {};
  private onTouched: () => void = () => {};

  toggle(): void {
    this.show.update((v) => !v);
  }

  onInput(event: Event): void {
    const el = event.target as HTMLInputElement;
    this.value.set(el.value);
    this.onChange(el.value);
  }

  onBlur(): void {
    this.onTouched();
  }

  writeValue(value: string): void {
    this.value.set(value ?? '');
  }

  registerOnChange(fn: (v: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }
}
