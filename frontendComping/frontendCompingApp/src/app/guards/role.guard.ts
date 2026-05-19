import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const roleGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const token = localStorage.getItem('token');
  const expectedRoles = route.data['roles'] as Array<string>;

  if (!token) {
    router.navigate(['/login']);
    return false;
  }

  const payload = JSON.parse(atob(token.split('.')[1]));
  const userRole = payload.role;

  if (expectedRoles.includes(userRole)) {
    return true;
  }

  router.navigate(['/Campino']);
  return false;
};
