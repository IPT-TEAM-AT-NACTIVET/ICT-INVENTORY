import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { AdminLayout } from './features/layout/admin-layout/admin-layout';
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  {
    path: 'admin',
    component: AdminLayout,
    canActivate: [authGuard, roleGuard(['ADMIN'])],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/admin/dashboard/dashboard').then((m) => m.AdminDashboard),
      },
      {
        path: 'directorates',
        loadComponent: () =>
          import('./features/admin/master-data/directorates').then((m) => m.DirectoratesComponent),
      },
      {
        path: 'sections',
        loadComponent: () =>
          import('./features/admin/master-data/sections').then((m) => m.SectionsComponent),
      },
      {
        path: 'units',
        loadComponent: () => import('./features/admin/master-data/units').then((m) => m.UnitsComponent),
      },
      {
        path: 'zones',
        loadComponent: () => import('./features/admin/master-data/zones').then((m) => m.ZonesComponent),
      },
      {
        path: 'device-types',
        loadComponent: () =>
          import('./features/admin/master-data/device-types').then((m) => m.DeviceTypesComponent),
      },
      {
        path: 'inventory',
        loadComponent: () => import('./features/admin/inventory/inventory').then((m) => m.Inventory),
      },
      {
        path: 'reports',
        loadComponent: () => import('./features/admin/reports/reports').then((m) => m.Reports),
      },
      {
        path: 'users',
        loadComponent: () => import('./features/admin/users/users').then((m) => m.Users),
      },
      {
        path: 'register-asset',
        loadComponent: () => import('./features/users/assets/asset-form').then((m) => m.AssetForm),
      },
      {
        path: 'profile',
        loadComponent: () => import('./features/profile/profile').then((m) => m.ProfilePage),
      },
    ],
  },
  {
    path: 'users',
    component: AdminLayout,
    canActivate: [authGuard, roleGuard(['ADMIN'])],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/users/dashboard/users-dashboard').then((m) => m.UsersDashboard),
      },
      {
        path: 'setup',
        loadComponent: () => import('./features/users/setup/setup').then((m) => m.Setup),
      },
      {
        path: 'assets',
        loadComponent: () => import('./features/users/assets/assets-list').then((m) => m.AssetsList),
      },
      {
        path: 'assets/register',
        loadComponent: () => import('./features/users/assets/asset-form').then((m) => m.AssetForm),
      },
      {
        path: 'assets/:id',
        loadComponent: () => import('./features/users/assets/asset-detail').then((m) => m.AssetDetail),
      },
      {
        path: 'assets/:id/edit',
        loadComponent: () => import('./features/users/assets/asset-form').then((m) => m.AssetForm),
      },
      {
        path: 'profile',
        loadComponent: () => import('./features/profile/profile').then((m) => m.ProfilePage),
      },
    ],
  },
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: '**', redirectTo: 'login' },
];
