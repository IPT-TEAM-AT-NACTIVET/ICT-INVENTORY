import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { roleGuard } from './role.guard';
import { TokenStorageService } from '../services/token-storage.service';
import { User } from '../models/user.model';

const adminUser: User = {
  id: 1,
  employeeId: 'EMP001',
  fullName: 'System Admin',
  email: 'admin@ict.go.tz',
  role: 'ADMIN',
};

describe('roleGuard', () => {
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

  it('should allow an ADMIN user into ADMIN routes', () => {
    store.setSession('token', adminUser);
    const result = TestBed.runInInjectionContext(() => roleGuard(['ADMIN'])(null!, null!));
    expect(result).toBe(true);
  });

  it('should route a user without the required role to login', () => {
    const otherUser: User = { ...adminUser, role: 'ADMIN' };
    store.setSession('token', otherUser);
    const result = TestBed.runInInjectionContext(() => roleGuard(['OTHER_ROLE'])(null!, null!));
    expect(result).toEqual(router.createUrlTree(['/login']));
  });

  it('should route an anonymous user to login', () => {
    const result = TestBed.runInInjectionContext(() => roleGuard(['ADMIN'])(null!, null!));
    expect(result).toEqual(router.createUrlTree(['/login']));
  });
});