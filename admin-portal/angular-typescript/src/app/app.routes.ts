import { Routes } from '@angular/router';
import { Dashboard } from './pages/dashboard';
import { Login } from './pages/login';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: Login },
  {
    path: '',
    component: Dashboard,
    canActivate: [authGuard]
  },
  { path: '**', redirectTo: '' }
];
