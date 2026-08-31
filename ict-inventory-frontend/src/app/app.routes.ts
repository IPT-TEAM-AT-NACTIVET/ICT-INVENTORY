import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { setupGuard } from './core/guards/setup.guard';
import { AdminLayout } from './features/layout/admin-layout/admin-layout';
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';
import { StaffLayout } from './features/layout/staff-layout/staff-layout';

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
        path: 'users',
        loadComponent: () =>
          import('./features/admin/staff-management/staff-management').then((m) => m.StaffManagement),
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
        path: 'offices',
        loadComponent: () =>
          import('./features/admin/master-data/offices').then((m) => m.OfficesComponent),
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
        path: 'verification',
        loadComponent: () =>
          import('./features/admin/verification/verification').then((m) => m.Verification),
      },
      {
        path: 'reports',
        loadComponent: () => import('./features/admin/reports/reports').then((m) => m.Reports),
      },
      {
        path: 'profile',
        loadComponent: () => import('./features/profile/profile').then((m) => m.ProfilePage),
      },
    ],
  },
  {
    path: 'staff',
    component: StaffLayout,
    canActivate: [authGuard, roleGuard(['STAFF'])],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/staff/dashboard/staff-dashboard').then((m) => m.StaffDashboard),
      },
      {
        path: 'setup',
        loadComponent: () => import('./features/staff/setup/setup').then((m) => m.Setup),
      },
      {
        path: 'assets',
        canActivate: [setupGuard],
        loadComponent: () => import('./features/staff/assets/assets-list').then((m) => m.AssetsList),
      },
      {
        path: 'assets/register',
        canActivate: [setupGuard],
        loadComponent: () => import('./features/staff/assets/asset-form').then((m) => m.AssetForm),
      },
      {
        path: 'assets/:id',
        canActivate: [setupGuard],
        loadComponent: () => import('./features/staff/assets/asset-detail').then((m) => m.AssetDetail),
      },
      {
        path: 'assets/:id/edit',
        canActivate: [setupGuard],
        loadComponent: () => import('./features/staff/assets/asset-form').then((m) => m.AssetForm),
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