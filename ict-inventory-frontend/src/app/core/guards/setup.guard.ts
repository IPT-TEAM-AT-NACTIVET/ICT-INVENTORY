import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ProfileService } from '../services/profile.service';

export const setupGuard: CanActivateFn = (_route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  const auth = inject(AuthService);
  const profile = inject(ProfileService);
  const router = inject(Router);

  if (auth.isAdmin()) {
    return true;
  }

  return profile.getMe().pipe(
    map((p) => {
      if (p.setupCompleted) {
        return true;
      }
      return router.createUrlTree(['/staff/setup'], {
        queryParams: { returnUrl: state.url },
      });
    }),
  );
};