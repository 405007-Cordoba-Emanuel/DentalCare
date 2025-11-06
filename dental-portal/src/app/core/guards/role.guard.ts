import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../services/auth/auth.service';
import { LocalStorageService } from '../services/auth/local-storage.service';
import { User } from '../../interfaces/user/user.interface';

export const roleGuard: CanActivateFn = (route) => {
  const router = inject(Router);
  const snackBar = inject(MatSnackBar);
  const authService = inject(AuthService);
  const localStorageService = inject(LocalStorageService);

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
  
  const allowedRoles = route.data?.['roles'] as string[];
  const currentPath = route.routeConfig?.path || '';

  if (!currentUser) {
    snackBar.open('Debes iniciar sesión para acceder a esta página', 'Cerrar', {
      duration: 3000,
    });
    router.navigate(['/login']);
    return false;
  }

  const userRole = currentUser.role || 'PATIENT';

  // Si se especificaron roles permitidos, verificar si el usuario tiene uno de ellos
  if (allowedRoles && allowedRoles.length > 0) {
    if (!allowedRoles.includes(userRole)) {
      snackBar.open('No tienes permisos para acceder a esta página', 'Cerrar', {
        duration: 3000,
      });
      
      // Redirigir al dashboard apropiado según el rol
      if (userRole === 'DENTIST') {
        router.navigate(['/dentist']);
      } else {
        router.navigate(['/patient']);
      }
      return false;
    }
    return true;
  }

  // ADMIN puede acceder a todas las rutas
  if (userRole === 'ADMIN') {
    return true;
  }

  // DENTIST no puede acceder a rutas que contengan 'admin'
  if (userRole === 'DENTIST') {
    if (currentPath === 'admin') {
      snackBar.open('No tienes permisos para acceder a esta página', 'Cerrar', {
        duration: 3000,
      });
      router.navigate(['/dentist']);
      return false;
    }
    return true;
  }

  // PATIENT no puede acceder a rutas que contengan 'admin' ni 'charts'
  if (userRole === 'PATIENT') {
    if (currentPath === 'admin' || currentPath === 'charts') {
      snackBar.open('No tienes permisos para acceder a esta página', 'Cerrar', {
        duration: 3000,
      });
      router.navigate(['/patient']);
      return false;
    }
    return true;
  }

  // Para cualquier otro rol, denegar acceso
  snackBar.open('No tienes permisos para acceder a esta página', 'Cerrar', {
    duration: 3000,
  });
  router.navigate(['/patient']);
  return false;
}; 