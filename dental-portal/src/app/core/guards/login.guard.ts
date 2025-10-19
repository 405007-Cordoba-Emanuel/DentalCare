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
    const userRole = currentUser?.role;
    
    console.log('User already authenticated, redirecting based on role:', userRole);
    
    switch (userRole) {
      case 'DENTIST':
        router.navigate(['/dentist']);
        return false;
      case 'PATIENT':
        router.navigate(['/patient']);
        return false;
      case 'ADMIN':
        router.navigate(['/dentist']); // Por defecto al dashboard de dentista
        return false;
      default:
        router.navigate(['/patient']); // Por defecto al dashboard de paciente
        return false;
    }
  }

  return true;
};
