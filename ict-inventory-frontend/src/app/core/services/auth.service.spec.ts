import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { TokenStorageService } from './token-storage.service';
import { environment } from '../../env';
import { LoginResponse } from '../models/user.model';

const routerStub = { navigate: (): Promise<boolean> => Promise.resolve(true) } as unknown as Router;
const loginResponse: LoginResponse = {
  token: 'jwt-token',
  user: {
    id: 1,
    employeeId: 'EMP001',
    fullName: 'System Admin',
    email: 'admin@ict.go.tz',
    role: 'ADMIN',
  },
};

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let store: TokenStorageService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: Router, useValue: routerStub },
      ],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    store = TestBed.inject(TokenStorageService);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should start unauthenticated', () => {
    expect(service.isAuthenticated()).toBe(false);
  });

  it('should login and persist token and user', () => {
    service.login({ email: 'admin@ict.go.tz', password: 'admin123' }).subscribe((response) => {
      expect(response.token).toBe('jwt-token');
    });
    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush(loginResponse);

    expect(service.token()).toBe('jwt-token');
    expect(service.user()?.role).toBe('ADMIN');
    expect(service.isAuthenticated()).toBe(true);
    expect(store.getToken()).toBe('jwt-token');
    expect(store.getUser()?.email).toBe('admin@ict.go.tz');
  });

  it('should restore a session from storage', () => {
    store.setSession('stored-token', loginResponse.user);
    const restored = new AuthService(TestBed.inject(HttpClient), routerStub, store);
    expect(restored.token()).toBe('stored-token');
    expect(restored.user()?.fullName).toBe('System Admin');
    expect(restored.isAuthenticated()).toBe(true);
  });

  it('should logout and clear storage', () => {
    service.login({ email: 'admin@ict.go.tz', password: 'admin123' }).subscribe();
    httpMock.expectOne(`${environment.apiUrl}/auth/login`).flush(loginResponse);

    expect(service.isAuthenticated()).toBe(true);
    service.logout();

    expect(service.token()).toBeNull();
    expect(service.user()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
    expect(store.getToken()).toBeNull();
    expect(store.getUser()).toBeNull();
  });
});