import { Component, inject, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
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
import { EmailRegisterRequest } from '../../../interfaces/auth/auth-response.interface';

@Component({
  selector: 'app-register',
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
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent implements OnInit {
  userType: string = 'PATIENT'; // Valor por defecto
  firstName: string = '';
  lastName: string = '';
  email: string = '';
  phone: string = '';
  password: string = '';
  confirmPassword: string = '';
  isLoading = false;
  errorMessage = '';

  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);

  ngOnInit(): void {
    // Detectar la ruta actual para establecer el rol correcto
    const currentPath = this.router.url;
    if (currentPath.includes('dentist-register')) {
      this.userType = 'DENTIST';
    } else if (currentPath.includes('patient-register')) {
      this.userType = 'PATIENT';
    }
  }

  onSubmit() {
    // Validaciones
    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'Las contraseñas no coinciden';
      return;
    }

    if (!this.firstName || !this.lastName || !this.email || !this.password) {
      this.errorMessage = 'Por favor completa todos los campos requeridos';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const registerRequest: EmailRegisterRequest = {
      firstName: this.firstName,
      lastName: this.lastName,
      email: this.email,
      password: this.password,
      confirmPassword: this.confirmPassword,
      role: this.userType // Enviar el rol seleccionado (PATIENT o DENTIST)
    };

    console.log('Register request:', registerRequest);
    this.authService.register(registerRequest).subscribe({
      next: (response) => {
        console.log('Registro exitoso:', response);
        
        // Redirigir según el rol
        if (response.role === 'DENTIST') {
          this.router.navigate(['/dentist-dashboard']);
        } else if (response.role === 'PATIENT') {
          this.router.navigate(['/patient-dashboard']);
        } else {
          this.router.navigate(['/dashboard']);
        }
      },
      error: (error) => {
        console.error('Error en registro:', error);
        this.isLoading = false;
        
        // Manejar diferentes tipos de errores
        if (error.status === 400) {
          this.errorMessage = error.error?.message || 'Datos inválidos';
        } else if (error.status === 409) {
          this.errorMessage = 'El email ya está registrado';
        } else if (error.status === 0) {
          this.errorMessage = 'Error de conexión con el servidor';
        } else {
          this.errorMessage = error.error?.message || 'Error al registrar usuario';
        }
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  goToLogin() {
    // Redirigir al login único
    this.router.navigate(['/login']);
  }

  goBack() {
    this.router.navigate(['/']);
  }
}
