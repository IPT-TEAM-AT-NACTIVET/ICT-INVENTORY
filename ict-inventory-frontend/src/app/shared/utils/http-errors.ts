import { HttpErrorResponse } from '@angular/common/http';

export function httpErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof HttpErrorResponse) {
    switch (error.status) {
      case 0:
        return 'Cannot reach the server. Check that the backend is running at the configured API URL.';
      case 401:
        return 'Your session has expired. Please log in again.';
      case 403:
        return 'You do not have permission to view this content.';
      case 404:
        return 'The requested content was not found on the server.';
      case 500:
      case 502:
      case 503:
        return 'The server encountered an error. Please try again.';
    }
  }
  return fallback;
}