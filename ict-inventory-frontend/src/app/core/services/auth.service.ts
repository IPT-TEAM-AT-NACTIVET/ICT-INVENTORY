import { HttpClient } from '@angular/common/http';
import { Injectable, Signal, WritableSignal, computed, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../env';
import { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse, User } from '../models/user.model';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http: HttpClient;
  private readonly router: Router;
  private readonly tokenStorage: TokenStorageService;

  private readonly tokenSignal: WritableSignal<string | null>;
  private readonly userSignal: WritableSignal<User | null>;

  readonly token: Signal<string | null>;
  readonly user: Signal<User | null>;
  readonly isAuthenticated: Signal<boolean>;
  readonly isAdmin: Signal<boolean>;
  readonly isStaff: Signal<boolean>;

  constructor(http: HttpClient, router: Router, tokenStorage: TokenStorageService) {
    this.http = http;
    this.router = router;
    this.tokenStorage = tokenStorage;
    this.tokenSignal = signal(tokenStorage.getToken());
    this.userSignal = signal(tokenStorage.getUser());
    this.token = this.tokenSignal.asReadonly();
    this.user = this.userSignal.asReadonly();
    this.isAuthenticated = computed(() => this.tokenSignal() !== null);
    this.isAdmin = computed(() => this.userSignal()?.role === 'ADMIN');
    this.isStaff = computed(() => this.userSignal()?.role === 'STAFF');
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, credentials).pipe(
      tap((response) => {
        this.tokenStorage.setSession(response.token, response.user);
        this.tokenSignal.set(response.token);
        this.userSignal.set(response.user);
      }),
    );
  }

  register(request: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${environment.apiUrl}/auth/register`, request);
  }

  updateStoredUser(user: User): void {
    this.tokenStorage.updateUser(user);
    this.userSignal.set(user);
  }

  logout(): void {
    this.tokenStorage.clear();
    this.tokenSignal.set(null);
    this.userSignal.set(null);
    this.router.navigate(['/login']);
  }
}