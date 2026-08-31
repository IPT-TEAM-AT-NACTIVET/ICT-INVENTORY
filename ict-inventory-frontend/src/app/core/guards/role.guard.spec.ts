import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { roleGuard } from './role.guard';
import { TokenStorageService } from '../services/token-storage.service';
import { User } from '../models/user.model';

const staffUser: User = {
  id: 2,
  employeeId: 'EMP002',
  fullName: 'Jane Staff',
  email: 'jane@ict.go.tz',
  role: 'STAFF',
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

  it('should allow a staff user into staff routes', () => {
    store.setSession('token', staffUser);
    const result = TestBed.runInInjectionContext(() => roleGuard(['STAFF'])(null!, null!));
    expect(result).toBe(true);
  });

  it('should route a staff user away from admin routes', () => {
    store.setSession('token', staffUser);
    const result = TestBed.runInInjectionContext(() => roleGuard(['ADMIN'])(null!, null!));
    expect(result).toEqual(router.createUrlTree(['/login']));
  });

  it('should route an anonymous user to login', () => {
    const result = TestBed.runInInjectionContext(() => roleGuard(['ADMIN'])(null!, null!));
    expect(result).toEqual(router.createUrlTree(['/login']));
  });
});