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
import { AuthService } from '../../../core/services/auth/auth.service';
import { EmailRegisterRequest } from '../../../interfaces/auth/auth-response.interface';
import { LocalStorageService } from '../../../core/services/auth/local-storage.service';
import {
  GenericFormComponent,
  FormField,
} from '../../../shared/generic-form/generic-form.component';
import { Validators } from '@angular/forms';

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
    GenericFormComponent,
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent implements OnInit {
  userType: string = 'PATIENT'; // Valor por defecto
  isLoading = false;
  errorMessage = '';
  formFields: FormField[] = [];

  private router = inject(Router);
  private authService = inject(AuthService);
  private localStorage = inject(LocalStorageService);
  ngOnInit(): void {
    // Detectar la ruta actual para establecer el rol correcto
    const currentPath = this.router.url;
    if (currentPath.includes('dentist-register')) {
      this.userType = 'DENTIST';
    } else if (currentPath.includes('patient-register')) {
      this.userType = 'PATIENT';
    }

    // Configurar campos del formulario
    this.configureFormFields();
  }

  private configureFormFields() {
    this.formFields = [
      {
        name: 'firstName',
        label: 'Nombre',
        type: 'text',
        placeholder: 'Ingresa tu nombre',
        validators: [Validators.required, Validators.minLength(2)],
      },
      {
        name: 'lastName',
        label: 'Apellido',
        type: 'text',
        placeholder: 'Ingresa tu apellido',
        validators: [Validators.required, Validators.minLength(2)],
      },
      {
        name: 'email',
        label: 'Email',
        type: 'text',
        placeholder: 'tu@email.com',
        validators: [Validators.required, Validators.email],
      },
      {
        name: 'password',
        label: 'Contraseña',
        type: 'password',
        placeholder: 'Ingresa tu contraseña',
        validators: [Validators.required, Validators.minLength(6)],
      },
      {
        name: 'confirmPassword',
        label: 'Confirmar Contraseña',
        type: 'password',
        placeholder: 'Confirma tu contraseña',
        validators: [Validators.required],
      },
    ];
  }

  handleFormSubmit(data: any) {
    // Validaciones
    if (data.password !== data.confirmPassword) {
      this.errorMessage = 'Las contraseñas no coinciden';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const registerRequest: EmailRegisterRequest = {
      firstName: data.firstName,
      lastName: data.lastName,
      email: data.email,
      password: data.password,
      confirmPassword: data.confirmPassword,
      role: this.userType, // Enviar el rol seleccionado (PATIENT o DENTIST)
    };

    this.authService.register(registerRequest).subscribe({
      next: (response) => {
        console.log('Registro exitoso:', response);

        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.isLoading = false;

        // Manejar diferentes tipos de errores
        if (error.status === 400) {
          this.errorMessage = error.error?.message || 'Datos inválidos';
        } else if (error.status === 409) {
          this.errorMessage = 'El email ya está registrado';
        } else if (error.status === 0) {
          this.errorMessage = 'Error de conexión con el servidor';
        } else {
          this.errorMessage =
            error.error?.message || 'Error al registrar usuario';
        }
      },
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
