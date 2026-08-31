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
    username: 'admin',
    email: 'admin@ict.go.tz',
    role: 'ADMIN',
  },
};

describe('Login', () => {
  let authService: {
    login: ReturnType<typeof vi.fn>;
    isAdmin: () => boolean;
    user: () => User | null;
  };
  let routerMock: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    localStorage.clear();
    authService = { login: vi.fn(), isAdmin: () => false, user: () => null };
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

  it('should login and navigate an admin to /admin/dashboard', () => {
    authService.login = vi.fn(() => of(loginResponse));
    authService.isAdmin = () => true;
    const fixture = TestBed.createComponent(Login);
    const component = fixture.componentInstance;

    component.form.setValue({ email: 'admin@nactvet.go.tz', password: 'admin123' });
    component.submit();

    expect(authService.login).toHaveBeenCalledWith({ email: 'admin@nactvet.go.tz', password: 'admin123' });
    expect(routerMock.navigate).toHaveBeenCalledWith(['/admin/dashboard']);
  });

  it('should navigate a staff user to /staff/dashboard', () => {
    authService.login = vi.fn(() =>
      of({
        token: 'jwt',
        user: { ...loginResponse.user, role: 'STAFF' },
      }),
    );
    authService.isAdmin = () => false;
    const fixture = TestBed.createComponent(Login);
    const component = fixture.componentInstance;

    component.form.setValue({ email: 'jane@nactvet.go.tz', password: 'secret' });
    component.submit();

    expect(routerMock.navigate).toHaveBeenCalledWith(['/staff/dashboard']);
  });

  it('should show an error message on invalid credentials', () => {
    authService.login = vi.fn(() => throwError(() => ({ status: 401 })));
    const fixture = TestBed.createComponent(Login);
    const component = fixture.componentInstance;

    component.form.setValue({ email: 'admin@nactvet.go.tz', password: 'wrong' });
    component.submit();

    expect(component.errorMessage()).toBe('Invalid email or password.');
  });
});