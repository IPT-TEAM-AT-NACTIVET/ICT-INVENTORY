import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { authGuard } from './auth.guard';
import { TokenStorageService } from '../services/token-storage.service';
import { User } from '../models/user.model';

const adminUser: User = {
  id: 1,
  employeeId: 'EMP001',
  fullName: 'System Admin',
  email: 'admin@ict.go.tz',
  role: 'ADMIN',
};

describe('authGuard', () => {
  let router: Router;
  let store: TokenStorageService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([])],
    });
    router = TestBed.inject(Router);
    store = TestBed.inject(TokenStorageService);
  });

  it('should allow access when a token is present', () => {
    store.setSession('token', adminUser);
    const result = TestBed.runInInjectionContext(() => authGuard(null!, null!));
    expect(result).toBe(true);
  });

  it('should redirect to login when unauthenticated', () => {
    const result = TestBed.runInInjectionContext(() => authGuard(null!, null!));
    expect(result).toEqual(router.createUrlTree(['/login']));
  });
});