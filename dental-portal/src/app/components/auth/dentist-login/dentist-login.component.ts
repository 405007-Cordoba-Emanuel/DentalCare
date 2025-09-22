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
  selector: 'app-dentist-login',
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
  templateUrl: './dentist-login.component.html',
  styleUrl: './dentist-login.component.css'
})
export class DentistLoginComponent {
  hidePassword = true;
  userType = 'Dentista';
  email = '';
  password = '';

  constructor(private router: Router) {}

  onSubmit() {
    if (this.email && this.password) {
      // Aquí iría la lógica de autenticación
      console.log('Login del dentista:', { userType: this.userType, email: this.email });
      this.router.navigate(['/dentist-dashboard']);
    }
  }

  goToRegister() {
    this.router.navigate(['/register']);
  }

  goBack() {
    this.router.navigate(['/']);
  }
}
