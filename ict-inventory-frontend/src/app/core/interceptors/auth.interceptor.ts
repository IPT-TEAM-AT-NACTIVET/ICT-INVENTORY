import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token();
  const isLoginRequest = req.url.includes('/auth/login');

  let authReq = req;
  if (token && !isLoginRequest) {
    authReq = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }

  return next(authReq).pipe(
    catchError((error) => {
      if (error.status === 401 && !isLoginRequest) {
        auth.logout();
      }
      return throwError(() => error);
    }),
  );
};