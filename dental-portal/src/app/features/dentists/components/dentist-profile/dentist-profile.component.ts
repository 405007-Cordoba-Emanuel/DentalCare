import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { DentistService } from '../../../../core/services/dentist.service';
import { UserService } from '../../../../core/services/auth/user.service';
import {
  FormField,
  GenericFormComponent,
} from '../../../../shared/generic-form/generic-form.component';
import { LocalStorageService } from '../../../../core/services/auth/local-storage.service';

@Component({
  selector: 'app-dentist-profile',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    GenericFormComponent,
  ],
  templateUrl: './dentist-profile.component.html',
  styleUrls: ['./dentist-profile.component.css'],
})
export class DentistProfileComponent implements OnInit {
  private userService = inject(UserService);
  private dentistService = inject(DentistService);
  private localStorageService = inject(LocalStorageService);
  private snackBar = inject(MatSnackBar);

  // Datos cargados
  userProfileData = signal<any>({});
  dentistProfileData = signal<any>({});

  // Estados de carga
  loadingPersonal = signal(false);
  loadingProfessional = signal(false);

  // Estados de edición
  editingPersonal = signal(false);
  editingProfessional = signal(false);

  ngOnInit(): void {
    this.loadUserProfile();
    this.loadDentistProfile();
  }

  // Configuración de campos - Datos Personales
  personalFields = signal<FormField[]>([
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
      name: 'phone',
      label: 'Teléfono',
      type: 'text',
      placeholder: '+54 9 11 1234-5678',
      validators: [Validators.required],
    },
    {
      name: 'address',
      label: 'Dirección',
      type: 'text',
      placeholder: 'Calle, número, piso, depto',
      fullWidth: true,
    },
    {
      name: 'birthDate',
      label: 'Fecha de Nacimiento',
      type: 'date',
      validators: [Validators.required],
      fullWidth: true,
      allowPastDates: true, // Permitir fechas pasadas para fecha de nacimiento
    },
  ]);

  // Configuración de campos - Datos Profesionales
  professionalFields = signal<FormField[]>([
    {
      name: 'licenseNumber',
      label: 'Número de Matrícula',
      type: 'text',
      placeholder: 'MP 12345',
      validators: [Validators.required],
      fullWidth: true,
    },
    {
      name: 'specialty',
      label: 'Especialidad',
      type: 'text',
      placeholder: 'Ej: Ortodoncia, Endodoncia',
      validators: [Validators.required],
      fullWidth: true,
    },
  ]);

  // Método para actualizar datos personales
  updatePersonalData(formData: any) {
    this.loadingPersonal.set(true);
    this.userService.updateUserProfile(formData).subscribe({
      next: () => {
        this.loadingPersonal.set(false);
        this.editingPersonal.set(false);
        this.loadUserProfile();
        this.showSuccessMessage('Información personal actualizada correctamente');
      },
      error: (err) => {
        this.loadingPersonal.set(false);
        this.showErrorMessage('Error al actualizar datos personales');
        console.error('Error:', err);
      },
    });
  }

  // Método para actualizar datos profesionales
  updateProfessionalData(formData: any) {
    this.loadingProfessional.set(true);

    this.dentistService
      .updateDentist(this.localStorageService.getDentistId(), formData)
      .subscribe({
        next: () => {
          this.loadingProfessional.set(false);
          this.editingProfessional.set(false);
          this.loadDentistProfile();
          this.showSuccessMessage('Información profesional actualizada correctamente');
        },
        error: (err) => {
          this.loadingProfessional.set(false);
          this.showErrorMessage('Error al actualizar datos profesionales');
          console.error('Error:', err);
        },
      });
  }

  // Métodos para alternar modo edición
  toggleEditPersonal() {
    const wasEditing = this.editingPersonal();
    this.editingPersonal.set(!wasEditing);

    // Si se está cancelando la edición, recargar datos originales
    if (wasEditing) {
      this.loadUserProfile();
    }
  }

  toggleEditProfessional() {
    const wasEditing = this.editingProfessional();
    this.editingProfessional.set(!wasEditing);

    // Si se está cancelando la edición, recargar datos originales
    if (wasEditing) {
      this.loadDentistProfile();
    }
  }

  // Cargar perfil de usuario
  loadUserProfile() {
    this.loadingPersonal.set(true);
    this.userService.getUser().subscribe({
      next: (user) => {
        this.userProfileData.set(user);
        this.loadingPersonal.set(false);
      },
      error: (err) => {
        console.error('Error cargando perfil de usuario:', err);
        this.loadingPersonal.set(false);
      },
    });
  }

  // Cargar perfil de dentista
  loadDentistProfile() {
    this.loadingProfessional.set(true);
    this.dentistService
      .getDentistById(this.localStorageService.getDentistId())
      .subscribe({
        next: (dentist) => {
          const professionalData = {
            licenseNumber: dentist.licenseNumber || '',
            specialty: dentist.specialty || '',
          };
          this.dentistProfileData.set(professionalData);
          this.loadingProfessional.set(false);
        },
        error: (err) => {
          console.error('Error cargando perfil de dentista:', err);
          this.loadingProfessional.set(false);
        },
      });
  }

  private showSuccessMessage(message: string) {
    this.snackBar.open(message, 'Cerrar', {
      duration: 4000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['success-snackbar'],
    });
  }

  private showErrorMessage(message: string) {
    this.snackBar.open(message, 'Cerrar', {
      duration: 5000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['error-snackbar'],
    });
  }
}
