import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth/auth.service';
import { LocalStorageService } from '../services/auth/local-storage.service';
import { User } from '../../interfaces/user/user.interface';

/**
 * Guard que redirige automáticamente al dashboard apropiado basado en el rol del usuario
 * Si el usuario está autenticado, lo redirige según su rol:
 * - DENTIST -> dentist-dashboard
 * - PATIENT -> patient-dashboard
 */
export const roleRedirectGuard: CanActivateFn = (route) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const localStorageService = inject(LocalStorageService);

  // Verificar si el usuario está autenticado
  if (!authService.isAuthenticated()) {
    console.log('User not authenticated, redirecting to login');
    router.navigate(['/login']);
    return false;
  }

  let currentUser = authService.currentUser;
  
  // Si currentUser es null, leer desde localStorage
  if (!currentUser) {
    const userDataString = localStorageService.getUserData();
    if (userDataString) {
      try {
        currentUser = JSON.parse(userDataString) as User;
      } catch (error) {
        console.error('Error parsing user data from localStorage:', error);
      }
    }
  }
  
  if (!currentUser) {
    console.log('No user data found, redirecting to login');
    router.navigate(['/login']);
    return false;
  }

  const userRole = currentUser.role || 'PATIENT';
  console.log('User role detected:', userRole);

  // Redirigir según el rol
  switch (userRole) {
    case 'DENTIST':
      console.log('Redirecting to dentist dashboard');
      router.navigate(['/dentist']);
      return false;
    case 'PATIENT':
      console.log('Redirecting to patient dashboard');
      router.navigate(['/patient']);
      return false;
    case 'ADMIN':
      console.log('Redirecting to admin dashboard');
      router.navigate(['/admin']);
      return false;
    default:
      // Para roles no reconocidos, redirigir al dashboard de paciente por defecto
      console.log('Unknown role, redirecting to patient dashboard');
      router.navigate(['/patient']);
      return false;
  }
};
