import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/services/auth/auth.service';

@Component({
  selector: 'app-forgot-password-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="forgot-password-dialog">
      <h2 mat-dialog-title>
        <mat-icon>lock_reset</mat-icon>
        Recuperar Contraseña
      </h2>

      <mat-dialog-content>
        @if (!emailSent) {
          <p class="description">
            Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.
          </p>

          @if (errorMessage) {
            <div class="error-message">
              <mat-icon>error</mat-icon>
              <span>{{ errorMessage }}</span>
            </div>
          }

          <mat-form-field class="full-width" appearance="fill">
            <mat-label>Email</mat-label>
            <input 
              matInput 
              type="email" 
              [(ngModel)]="email" 
              placeholder="tu@email.com"
              [disabled]="isLoading"
              (keyup.enter)="onSubmit()"
              required
            >
            <mat-icon matPrefix>email</mat-icon>
          </mat-form-field>
        } @else {
          <div class="success-message">
            <mat-icon>check_circle</mat-icon>
            <h3>¡Correo Enviado!</h3>
            <p>
              Hemos enviado un correo electrónico a <strong>{{ email }}</strong> con las instrucciones 
              para restablecer tu contraseña.
            </p>
            <p class="info">
              Por favor revisa tu bandeja de entrada y sigue las instrucciones. 
              El enlace será válido por 1 hora.
            </p>
          </div>
        }
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        @if (!emailSent) {
          <button mat-button (click)="onCancel()" [disabled]="isLoading">
            Cancelar
          </button>
          <button 
            mat-raised-button 
            color="primary" 
            (click)="onSubmit()"
            [disabled]="!email || isLoading"
          >
            @if (isLoading) {
              <mat-spinner diameter="20"></mat-spinner>
              <span>Enviando...</span>
            } @else {
              <span>Enviar Enlace</span>
            }
          </button>
        } @else {
          <button mat-raised-button color="primary" (click)="onCancel()">
            Cerrar
          </button>
        }
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .forgot-password-dialog {
      min-width: 400px;
    }

    h2 {
      display: flex;
      align-items: center;
      gap: 10px;
      color: #667eea;
      margin: 0;
    }

    mat-dialog-content {
      padding: 20px 0;
    }

    .description {
      color: #666;
      margin-bottom: 20px;
    }

    .full-width {
      width: 100%;
    }

    .error-message {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px;
      background-color: #fee;
      color: #c33;
      border-radius: 4px;
      margin-bottom: 16px;
    }

    .error-message mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .success-message {
      text-align: center;
      padding: 20px;
    }

    .success-message mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: #4caf50;
      margin-bottom: 16px;
    }

    .success-message h3 {
      color: #4caf50;
      margin: 0 0 16px 0;
    }

    .success-message p {
      margin: 12px 0;
      color: #666;
    }

    .success-message .info {
      font-size: 0.9em;
      color: #999;
    }

    mat-dialog-actions button {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    mat-dialog-actions button mat-spinner {
      margin-right: 8px;
    }
  `]
})
export class ForgotPasswordDialogComponent {
  private dialogRef = inject(MatDialogRef<ForgotPasswordDialogComponent>);
  private authService = inject(AuthService);

  email = '';
  isLoading = false;
  errorMessage = '';
  emailSent = false;

  onSubmit() {
    if (!this.email) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.forgotPassword({ email: this.email }).subscribe({
      next: (response) => {
        this.emailSent = true;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error in forgot password:', error);
        this.isLoading = false;
        
        if (error.status === 0) {
          this.errorMessage = 'Error de conexión con el servidor';
        } else {
          this.errorMessage = error.error?.message || 'Error al procesar la solicitud';
        }
      }
    });
  }

  onCancel() {
    this.dialogRef.close();
  }
}

