import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { UserService } from '../../core/services/auth/user.service';
import { PatientService, Patient, PatientUpdateRequest } from '../../core/services/patient.service';
import { LocalStorageService } from '../../core/services/auth/local-storage.service';
import { User } from '../../interfaces/user/user.interface';

@Component({
  selector: 'app-patient-profile',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSnackBarModule
  ],
  templateUrl: './patient-profile.component.html',
  styleUrl: './patient-profile.component.css'
})
export class PatientProfileComponent implements OnInit {
  private userService = inject(UserService);
  private patientService = inject(PatientService);
  private localStorage = inject(LocalStorageService);
  private snackBar = inject(MatSnackBar);

  // Datos del usuario
  user: User | null = null;
  
  // Datos del paciente
  patient: Patient | null = null;
  isEditing = false;
  isLoading = false;
  
  // Para el datepicker
  maxDate = new Date();

  // Formulario de edición
  editForm = {
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    birthDate: '',
    dni: '',
    address: ''
  };

  ngOnInit() {
    this.loadData();
  }

  private loadData() {
    this.isLoading = true;
    
    // Obtener usuario autenticado
    const userStr = this.localStorage.getUserData();
    if (!userStr) {
      console.error('No hay usuario autenticado');
      this.isLoading = false;
      return;
    }

    this.user = JSON.parse(userStr);

    // Establecer datos iniciales del usuario
    this.editForm.email = this.user?.email || '';
    this.editForm.firstName = this.user?.firstName || '';
    this.editForm.lastName = this.user?.lastName || '';

    // Obtener información del usuario
    this.userService.getUser().subscribe({
      next: (userData) => {
        // Actualizar con datos del perfil de usuario
        this.editForm.phone = userData.phone || '';
        this.editForm.birthDate = userData.birthDate ? new Date(userData.birthDate).toISOString().split('T')[0] : '';
        this.editForm.firstName = userData.firstName || this.editForm.firstName;
        this.editForm.lastName = userData.lastName || this.editForm.lastName;
        this.editForm.address = userData.address || '';

        console.log('Usuario cargado:', this.user);
        console.log('patientId del usuario:', this.user?.patientId);

        // Obtener información del paciente si existe
        if (this.user?.patientId) {
          console.log('Intentando cargar paciente con ID:', this.user.patientId);
          this.patientService.getPatientById(this.user.patientId).subscribe({
            next: (patientData) => {
              console.log('=== DATOS COMPLETOS DEL PACIENTE ===');
              console.log('Patient data completo:', JSON.stringify(patientData, null, 2));
              console.log('patientData.dni específicamente:', patientData.dni);
              console.log('Tipo de dato DNI:', typeof patientData.dni);
              console.log('=== FIN DATOS ===');
              
              this.patient = patientData;
              this.editForm.dni = patientData.dni || '';
              console.log('DNI asignado a editForm:', this.editForm.dni);
              // Si el paciente tiene datos que no tiene el usuario, usarlos
              if (patientData.address && !userData.address) {
                this.editForm.address = patientData.address;
              }
              if (patientData.phone && !userData.phone) {
                this.editForm.phone = patientData.phone;
              }
              this.isLoading = false;
            },
            error: (error) => {
              console.error('Error al cargar datos del paciente:', error);
              // Si falla, seguir mostrando lo que se pudo cargar
              this.isLoading = false;
            }
          });
        } else {
          console.warn('El usuario no tiene patientId asociado');
          this.isLoading = false;
        }
      },
      error: (error) => {
        console.error('Error al cargar datos del usuario:', error);
        this.isLoading = false;
      }
    });
  }

  toggleEdit() {
    this.isEditing = !this.isEditing;
    if (this.isEditing) {
      // Ya tenemos los datos cargados en editForm
    } else {
      // Recargar datos originales
      this.loadData();
    }
  }

  saveChanges() {
    this.isLoading = true;

    // Actualizar usuario - construir el objeto solo con campos válidos
    const userUpdate = {
      firstName: this.editForm.firstName,
      lastName: this.editForm.lastName,
      phone: this.editForm.phone,
      address: this.editForm.address,
      ...(this.editForm.birthDate && { birthDate: new Date(this.editForm.birthDate) })
    };

    this.userService.updateUserProfile(userUpdate).subscribe({
      next: () => {
        console.log('Usuario actualizado correctamente');
        
        // Debug: verificar datos necesarios para actualizar paciente
        console.log('this.patient:', this.patient);
        console.log('this.user?.patientId:', this.user?.patientId);
        console.log('this.editForm.dni:', this.editForm.dni);
        
        // Si es paciente, actualizar también los datos del paciente
        if (this.user?.patientId) {
          console.log('Intentando actualizar paciente. DNI en formulario:', this.editForm.dni);
          console.log('patientId a actualizar:', this.user.patientId);
          
          const patientUpdate: PatientUpdateRequest = {
            dni: this.editForm.dni ? this.editForm.dni.trim() : '',
            active: true
          };
          
          console.log('Actualizando paciente con:', patientUpdate);
          console.log('DNI en el formulario:', this.editForm.dni);
          console.log('DNI antes de enviar:', patientUpdate.dni);

          this.patientService.updatePatient(this.user.patientId, patientUpdate).subscribe({
            next: () => {
              console.log('Paciente actualizado correctamente');
              this.showSuccessMessage('Información actualizada correctamente');
              this.isEditing = false;
              this.isLoading = false;
              this.loadData(); // Recargar datos
            },
            error: (error) => {
              console.error('Error al actualizar paciente:', error);
              this.showErrorMessage('Error al actualizar los datos del paciente');
              this.isLoading = false;
            }
          });
        } else {
          console.warn('No se actualiza paciente porque no hay patientId');
          console.warn('- this.user?.patientId:', this.user?.patientId || 'NO EXISTE');
          this.showSuccessMessage('Información actualizada correctamente');
          this.isEditing = false;
          this.isLoading = false;
          this.loadData(); // Recargar datos
        }
      },
      error: (error) => {
        console.error('Error al actualizar usuario:', error);
        this.showErrorMessage('Error al actualizar la información');
        this.isLoading = false;
      }
    });
  }

  private showSuccessMessage(message: string) {
    this.snackBar.open(message, 'Cerrar', {
      duration: 4000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['success-snackbar']
    });
  }

  private showErrorMessage(message: string) {
    this.snackBar.open(message, 'Cerrar', {
      duration: 5000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    });
  }

  cancelEdit() {
    this.isEditing = false;
    this.loadData();
  }

  get displayName(): string {
    if (this.editForm.firstName && this.editForm.lastName) {
      return `${this.editForm.firstName} ${this.editForm.lastName}`;
    }
    return 'Usuario';
  }

  get displayDate(): string {
    if (this.editForm.birthDate) {
      // Parsear la fecha en formato YYYY-MM-DD para evitar problemas de zona horaria
      const dateStr = this.editForm.birthDate;
      if (dateStr.includes('T')) {
        // Si ya es una fecha con hora, usar directamente
        const date = new Date(dateStr);
        return date.toLocaleDateString('es-ES');
      } else {
        // Si es solo YYYY-MM-DD, parsearla manualmente para evitar problemas de zona horaria
        const parts = dateStr.split('-');
        if (parts.length === 3) {
          const year = parseInt(parts[0]);
          const month = parseInt(parts[1]) - 1; // Los meses en JS son 0-indexed
          const day = parseInt(parts[2]);
          const date = new Date(year, month, day);
          return date.toLocaleDateString('es-ES');
        }
      }
    }
    return 'No especificada';
  }
}

