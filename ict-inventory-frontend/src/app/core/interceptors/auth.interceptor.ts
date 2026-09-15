import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token();
  const isAuthEndpoint = req.url.includes('/auth/login') || req.url.includes('/auth/logout');

  let authReq = req;
  if (token && !isAuthEndpoint) {
    authReq = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }

  return next(authReq).pipe(
    catchError((error) => {
      if (error.status === 401 && !isAuthEndpoint) {
        auth.logout();
      }
      return throwError(() => error);
    }),
  );
};