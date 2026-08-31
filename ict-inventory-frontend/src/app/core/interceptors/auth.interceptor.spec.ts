import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';
import { TokenStorageService } from '../services/token-storage.service';
import { environment } from '../../env';
import { User } from '../models/user.model';

const adminUser: User = {
  id: 1,
  employeeId: 'EMP001',
  fullName: 'System Admin',
  username: 'admin',
  email: 'admin@ict.go.tz',
  role: 'ADMIN',
};

describe('authInterceptor', () => {
  let httpMock: HttpTestingController;
  let store: TokenStorageService;
  let routerMock: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    localStorage.clear();
    routerMock = { navigate: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: Router, useValue: routerMock },
      ],
    });
    httpMock = TestBed.inject(HttpTestingController);
    store = TestBed.inject(TokenStorageService);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should attach the Bearer token to API requests', () => {
    store.setSession('secret-token', adminUser);
    const http = TestBed.inject(HttpClient);
    http.get(`${environment.apiUrl}/admin/dashboard`).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/admin/dashboard`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer secret-token');
    req.flush({});
  });

  it('should not attach a token to the login request', () => {
    store.setSession('secret-token', adminUser);
    const http = TestBed.inject(HttpClient);
    http.post(`${environment.apiUrl}/auth/login`, {}).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('should logout and redirect to /login on a 401 response', () => {
    store.setSession('secret-token', adminUser);
    const http = TestBed.inject(HttpClient);
    http.get(`${environment.apiUrl}/admin/assets`).subscribe({
      error: () => undefined,
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/admin/assets`);
    req.flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(store.getToken()).toBeNull();
    expect(TestBed.inject(AuthService).isAuthenticated()).toBe(false);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should pass non-401 errors through without logging out', () => {
    store.setSession('secret-token', adminUser);
    const http = TestBed.inject(HttpClient);
    http.get(`${environment.apiUrl}/admin/assets`).subscribe({
      error: () => undefined,
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/admin/assets`);
    req.flush({}, { status: 500, statusText: 'Server Error' });

    expect(store.getToken()).toBe('secret-token');
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });
});