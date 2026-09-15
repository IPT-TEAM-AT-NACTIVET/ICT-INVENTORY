import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { AdminLayout } from './features/layout/admin-layout/admin-layout';
import { Login } from './features/auth/login/login';

export const routes: Routes = [
  { path: 'login', component: Login },
  {
    path: 'admin',
    component: AdminLayout,
    canActivate: [authGuard],
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
        path: 'recycle-bin',
        loadComponent: () =>
          import('./features/admin/recycle-bin/recycle-bin').then((m) => m.RecycleBin),
      },
      {
        path: 'reports',
        loadComponent: () => import('./features/admin/reports/reports').then((m) => m.Reports),
      },
      {
        path: 'audit-logs',
        loadComponent: () =>
          import('./features/admin/audit-logs/audit-logs').then((m) => m.AuditLogs),
      },
      {
        path: 'users',
        loadComponent: () => import('./features/admin/users/users').then((m) => m.Users),
      },
      {
        path: 'profile',
        loadComponent: () => import('./features/profile/profile').then((m) => m.ProfilePage),
      },
    ],
  },
  {
    path: 'users',
    redirectTo: '/admin/dashboard',
    pathMatch: 'full',
  },
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: '**', redirectTo: 'login' },
];
