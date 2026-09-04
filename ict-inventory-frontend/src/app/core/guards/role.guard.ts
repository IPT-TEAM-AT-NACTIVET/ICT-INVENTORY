import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Role } from '../models/enums';
import { AuthService } from '../services/auth.service';

export function roleGuard(allowed: Role[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    const role = auth.user()?.role ?? null;
    if (role !== null && allowed.includes(role)) {
      return true;
    }
    return router.createUrlTree(['/login']);
  };
}