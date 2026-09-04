import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { Register } from './register';
import { AuthService } from '../../../core/services/auth.service';

describe('Register', () => {
  let authService: {
    register: ReturnType<typeof vi.fn>;
  };
  let routerMock: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    localStorage.clear();
    authService = { register: vi.fn() };
    routerMock = { navigate: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [Register],
      providers: [
        provideHttpClient(),
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: routerMock },
      ],
    }).compileComponents();
  });

  it('should create the component', () => {
    const fixture = TestBed.createComponent(Register);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should not submit an empty form', () => {
    const fixture = TestBed.createComponent(Register);
    const component = fixture.componentInstance;
    component.submit();
    expect(authService.register).not.toHaveBeenCalled();
  });

  it('should submit registration and show success message', () => {
    authService.register = vi.fn(() => of({} as never));
    const fixture = TestBed.createComponent(Register);
    const component = fixture.componentInstance;

    component.form.setValue({
      fullName: 'Jane Doe',
      email: 'jane.doe@nactvet.go.tz',
      phoneNumber: '0712345678',
      password: 'secret123',
      confirmPassword: 'secret123',
    });
    component.submit();

    expect(authService.register).toHaveBeenCalled();
    expect(component.successMessage()).toBeTruthy();
  });

  it('should show an error when passwords do not match', () => {
    const fixture = TestBed.createComponent(Register);
    const component = fixture.componentInstance;

    component.form.setValue({
      fullName: 'Jane Doe',
      email: 'jane.doe@nactvet.go.tz',
      phoneNumber: '0712345678',
      password: 'secret123',
      confirmPassword: 'different',
    });
    component.submit();

    expect(authService.register).not.toHaveBeenCalled();
    expect(component.errorMessage()).toBeTruthy();
  });
});
