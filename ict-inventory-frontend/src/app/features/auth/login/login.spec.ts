import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { Login } from './login';
import { AuthService } from '../../../core/services/auth.service';
import { LoginResponse, User } from '../../../core/models/user.model';

const loginResponse: LoginResponse = {
  token: 'jwt',
  user: {
    id: 1,
    employeeId: 'EMP001',
    fullName: 'System Admin',
    email: 'admin@ict.go.tz',
    role: 'ADMIN',
  },
};

describe('Login', () => {
  let authService: {
    login: ReturnType<typeof vi.fn>;
    user: () => User | null;
  };
  let routerMock: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    localStorage.clear();
    authService = { login: vi.fn(), user: () => null };
    routerMock = { navigate: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideHttpClient(),
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: routerMock },
      ],
    }).compileComponents();
  });

  it('should create the component', () => {
    const fixture = TestBed.createComponent(Login);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should not submit an empty form', () => {
    const fixture = TestBed.createComponent(Login);
    const component = fixture.componentInstance;
    component.submit();
    expect(authService.login).not.toHaveBeenCalled();
  });

  it('should login and navigate to /admin/dashboard when setup is complete', () => {
    authService.login = vi.fn(() => of(loginResponse));
    authService.user = () => ({ ...loginResponse.user, setupCompleted: true });
    const fixture = TestBed.createComponent(Login);
    const component = fixture.componentInstance;

    component.form.setValue({ email: 'admin@nactvet.go.tz', password: 'admin123' });
    component.submit();

    expect(authService.login).toHaveBeenCalledWith({ email: 'admin@nactvet.go.tz', password: 'admin123' });
    expect(routerMock.navigate).toHaveBeenCalledWith(['/admin/dashboard']);
  });

  it('should navigate to /users/setup when setup is not complete', () => {
    authService.login = vi.fn(() =>
      of({
        token: 'jwt',
        user: { ...loginResponse.user, role: 'ADMIN', setupCompleted: false },
      }),
    );
    authService.user = () => ({ ...loginResponse.user, role: 'ADMIN', setupCompleted: false });
    const fixture = TestBed.createComponent(Login);
    const component = fixture.componentInstance;

    component.form.setValue({ email: 'jane@nactvet.go.tz', password: 'secret' });
    component.submit();

    expect(routerMock.navigate).toHaveBeenCalledWith(['/users/setup']);
  });

  it('should show an error message on invalid credentials', () => {
    authService.login = vi.fn(() => throwError(() => ({ status: 401 })));
    const fixture = TestBed.createComponent(Login);
    const component = fixture.componentInstance;

    component.form.setValue({ email: 'admin@nactvet.go.tz', password: 'wrong' });
    component.submit();

    expect(component.errorMessage()).toBe('Invalid email or password');
  });
});