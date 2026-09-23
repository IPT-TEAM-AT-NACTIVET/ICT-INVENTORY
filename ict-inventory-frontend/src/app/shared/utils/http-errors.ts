import { HttpErrorResponse } from '@angular/common/http';

export type TranslateFn = (key: string) => string;

export function httpErrorMessage(
  error: unknown,
  fallback: string,
  t: TranslateFn = (key) => key,
): string {
  if (error instanceof HttpErrorResponse) {
    switch (error.status) {
      case 0:
        return t('httpErrors.serverUnreachable');
      case 401:
        return t('httpErrors.sessionExpired');
      case 403:
        return t('httpErrors.forbidden');
      case 404:
        return t('httpErrors.notFound');
      case 500:
      case 502:
      case 503:
        return t('httpErrors.serverError');
    }
  }
  return fallback;
}