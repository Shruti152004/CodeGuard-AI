import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const token = localStorage.getItem('accessToken');

  if (token) {
    return true;
  }

  // Not logged in, redirect to login
  router.navigate(['/login']);
  return false;
};
