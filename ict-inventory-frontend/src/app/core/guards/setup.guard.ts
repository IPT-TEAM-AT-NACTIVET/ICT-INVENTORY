import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ProfileService } from '../services/profile.service';

export const setupGuard: CanActivateFn = (_route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  const auth = inject(AuthService);
  const profile = inject(ProfileService);
  const router = inject(Router);

  if (auth.isAuthenticated() && (auth.user()?.setupCompleted ?? false)) {
    return true;
  }

  return profile.getMe().pipe(
    map((p) => {
      if (p.setupCompleted) {
        return true;
      }
      return router.createUrlTree(['/users/setup'], {
        queryParams: { returnUrl: state.url },
      });
    }),
  );
};