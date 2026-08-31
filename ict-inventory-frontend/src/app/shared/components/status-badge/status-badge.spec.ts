import { TestBed } from '@angular/core/testing';
import { StatusBadge } from './status-badge';

describe('StatusBadge', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StatusBadge],
    }).compileComponents();
  });

  it('should render the label', () => {
    const fixture = TestBed.createComponent(StatusBadge);
    fixture.componentRef.setInput('label', 'Verified');
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Verified');
  });

  it('should apply the tone class', () => {
    const fixture = TestBed.createComponent(StatusBadge);
    fixture.componentRef.setInput('label', 'Active');
    fixture.componentRef.setInput('tone', 'success');
    fixture.detectChanges();
    const badge = fixture.nativeElement.querySelector('.badge') as HTMLElement;
    expect(badge.classList.contains('badge')).toBe(true);
    expect(badge.classList.contains('tone-success')).toBe(true);
  });

  it('should default to the neutral tone', () => {
    const fixture = TestBed.createComponent(StatusBadge);
    fixture.componentRef.setInput('label', 'Unknown');
    fixture.detectChanges();
    const badge = fixture.nativeElement.querySelector('.badge') as HTMLElement;
    expect(badge.classList.contains('tone-neutral')).toBe(true);
  });
});