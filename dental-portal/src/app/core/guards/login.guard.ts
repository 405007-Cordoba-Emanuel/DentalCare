import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth/auth.service';

export const loginGuard: CanActivateFn = (route) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  const isAuthenticated = authService.isAuthenticated();

  // Si está autenticado, redirigir según el rol
  if (isAuthenticated) {
    const currentUser = authService.currentUser;
    if (currentUser?.role === 'ADMIN') {
      router.navigate(['/dashboard']);
    } else {
      router.navigate(['/dashboard']);
    }
    return false;
  }

  return true;
};
