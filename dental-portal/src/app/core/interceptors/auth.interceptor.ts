import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { LocalStorageService } from '../services/auth/local-storage.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const localStorageService = inject(LocalStorageService);
  const token = localStorageService.getAuthToken();

  // Si existe el token y no es una petición de autenticación, agregar el header
  if (token && !req.url.includes('/auth/') && !req.url.includes('/api/core/dentist/{id}/patients')) {
    const clonedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(clonedRequest);
  }

  // Si no hay token y es una petición protegida, la petición fallará con 401
  // Esto es esperado y el frontend debe manejar el error
  return next(req);
};

