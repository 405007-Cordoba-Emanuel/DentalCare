import { Component, inject, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { IconComponent } from '../../../shared/icon/icon.component';
import { AuthService } from '../../../core/services/auth/auth.service';
import { GoogleSignInService } from '../../../core/services/auth/google-signin.service';
import { EmailAuthRequest } from '../../../interfaces/auth/auth-response.interface';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    IconComponent
  ],
  templateUrl: './pacient-login.component.html',
  styleUrl: './pacient-login.component.css'
})
export class LoginComponent implements AfterViewInit {
  @ViewChild('googleButton') googleButton!: ElementRef;
  
  hidePassword = true;
  userType = 'Usuario'; // Cambiado a genérico ya que sirve para ambos roles
  email = '';
  password = '';
  isLoading = false;
  errorMessage = '';

  private router = inject(Router);
  private authService = inject(AuthService);
  private googleSignInService = inject(GoogleSignInService);

  onSubmit() {
    if (this.email && this.password) {
      this.isLoading = true;
      this.errorMessage = '';

      const loginRequest: EmailAuthRequest = {
        email: this.email,
        password: this.password
      };

      this.authService.login(loginRequest).subscribe({
        next: (response) => {
          console.log('Login exitoso:', response);
          
          // Redirigir directamente según el rol del usuario
          if (response.role === 'DENTIST') {
            this.router.navigate(['/dentist']);
          } else if (response.role === 'PATIENT') {
            this.router.navigate(['/patient']);
          } else if (response.role === 'ADMIN') {
            this.router.navigate(['/admin']);
          } else {
            // Por defecto, redirigir al dashboard genérico
            this.router.navigate(['/dashboard']);
          }
        },
        error: (error) => {
          console.error('Error en login:', error);
          this.isLoading = false;
          
          // Manejar diferentes tipos de errores
          if (error.status === 401) {
            this.errorMessage = 'Email o contraseña incorrectos';
          } else if (error.status === 0) {
            this.errorMessage = 'Error de conexión con el servidor';
          } else {
            this.errorMessage = error.error?.message || 'Error al iniciar sesión';
          }
        },
        complete: () => {
          this.isLoading = false;
        }
      });
    }
  }

  goToRegister() {
    // Redirigir al registro de pacientes por defecto
    this.router.navigate(['/patient-register']);
  }

  goBack() {
    this.router.navigate(['/']);
  }

  ngAfterViewInit(): void {
    // Inicializar y renderizar el botón de Google
    this.googleSignInService.initializeGoogleSignIn();
    
    // Pequeño delay para asegurar que Google esté completamente cargado
    setTimeout(() => {
      if (this.googleButton && this.googleButton.nativeElement) {
        this.googleSignInService.renderButton(this.googleButton.nativeElement);
      }
    }, 100);
  }
}

