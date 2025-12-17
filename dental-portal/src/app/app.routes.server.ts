import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  // Rutas protegidas que requieren autenticación - usar Server-side rendering
  // (Angular SSR matching es específico primero, luego general)
  {
    path: 'patient/**',
    renderMode: RenderMode.Server
  },
  {
    path: 'dentist/**',
    renderMode: RenderMode.Server
  },
  {
    path: 'admin/**',
    renderMode: RenderMode.Server
  },
  // Rutas públicas - prerenderizar
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
