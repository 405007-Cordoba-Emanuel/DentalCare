import { Injectable, NgZone, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { GOOGLE_CONFIG } from '../../config/google.config';
import { GoogleAuthService } from './google-auth.service';

declare const google: any;

@Injectable({
  providedIn: 'root',
})
export class GoogleSignInService {
  private googleAuthService = inject(GoogleAuthService);
  private router = inject(Router);
  private ngZone = inject(NgZone);
  private platformId = inject(PLATFORM_ID);
  private isInitialized = false;

  /**
   * Inicializa Google Sign-In
   */
  initializeGoogleSignIn(): void {
    // Solo ejecutar en el navegador
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    if (this.isInitialized) {
      return;
    }

    if (typeof google !== 'undefined') {
      this.setupGoogleSignIn();
    } else {
      // Esperar a que el script de Google se cargue
      window.addEventListener('load', () => {
        if (typeof google !== 'undefined') {
          this.setupGoogleSignIn();
        }
      });
    }
  }

  private setupGoogleSignIn(): void {
    try {
      google.accounts.id.initialize({
        client_id: GOOGLE_CONFIG.CLIENT_ID,
        callback: (response: any) => this.handleCredentialResponse(response),
        auto_select: false,
        cancel_on_tap_outside: true,
      });
      this.isInitialized = true;
      console.log('Google Sign-In inicializado correctamente');
    } catch (error) {
      console.error('Error al inicializar Google Sign-In:', error);
    }
  }

  /**
   * Renderiza el botón de Google Sign-In en un elemento específico
   */
  renderButton(element: HTMLElement): void {
    // Solo ejecutar en el navegador
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    if (!this.isInitialized) {
      console.warn('Google Sign-In no está inicializado');
      return;
    }

    if (typeof google !== 'undefined' && google.accounts && google.accounts.id) {
      google.accounts.id.renderButton(element, {
        type: 'standard',
        shape: 'rectangular',
        theme: 'outline',
        text: 'signin_with',
        size: 'large',
        logo_alignment: 'left',
        width: element.offsetWidth || 300,
      });
    }
  }

  /**
   * Muestra el prompt de One Tap
   */
  promptOneTap(): void {
    // Solo ejecutar en el navegador
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    if (!this.isInitialized) {
      console.warn('Google Sign-In no está inicializado');
      return;
    }

    if (typeof google !== 'undefined' && google.accounts && google.accounts.id) {
      google.accounts.id.prompt((notification: any) => {
        if (notification.isNotDisplayed() || notification.isSkippedMoment()) {
          console.log('One Tap no se mostró:', notification.getNotDisplayedReason());
        }
      });
    }
  }

  /**
   * Maneja la respuesta del credential después del login
   */
  private handleCredentialResponse(response: any): void {
    const idToken = response.credential;

    if (!idToken) {
      console.error('No se recibió token de Google');
      return;
    }

    // Enviar el token al backend
    this.googleAuthService.loginWithGoogle(idToken).subscribe({
      next: (authResponse) => {
        console.log('Login con Google exitoso:', authResponse);
        
        // Redirigir usando el guard de redirección automática
        this.ngZone.run(() => {
          this.router.navigate(['/dashboard']);
        });
      },
      error: (error) => {
        console.error('Error al autenticar con Google:', error);
        // Aquí podrías mostrar un mensaje de error al usuario
      },
    });
  }

  /**
   * Cierra la sesión de Google
   */
  signOut(): void {
    if (typeof google !== 'undefined' && google.accounts && google.accounts.id) {
      google.accounts.id.disableAutoSelect();
    }
  }
}

