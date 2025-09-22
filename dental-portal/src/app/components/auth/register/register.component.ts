import { Component } from '@angular/core';
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
export class RegisterComponent {
  userType: string = 'Paciente';
  firstName: string = '';
  lastName: string = '';
  email: string = '';
  phone: string = '';
  password: string = '';
  confirmPassword: string = '';

  constructor(private router: Router) {}

  onSubmit() {
    if (this.password !== this.confirmPassword) {
      alert('Las contraseñas no coinciden');
      return;
    }

    // Aquí iría la lógica de registro
    console.log('Register attempt:', { 
      userType: this.userType, 
      firstName: this.firstName, 
      lastName: this.lastName,
      email: this.email,
      phone: this.phone
    });
    
    // Redirigir según el tipo de usuario
    if (this.userType === 'Paciente') {
      this.router.navigate(['/login']);
    } else {
      this.router.navigate(['/dentist-login']);
    }
  }

  goToLogin() {
    // Redirigir al login correspondiente según el tipo de usuario
    if (this.userType === 'Paciente') {
      this.router.navigate(['/login']);
    } else {
      this.router.navigate(['/dentist-login']);
    }
  }

  goBack() {
    this.router.navigate(['/']);
  }
}
