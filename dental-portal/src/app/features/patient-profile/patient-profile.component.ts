import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { Validators } from '@angular/forms';
import { UserService } from '../../core/services/auth/user.service';
import {
  PatientService,
  Patient,
  PatientUpdateRequest,
} from '../../core/services/patient.service';
import { LocalStorageService } from '../../core/services/auth/local-storage.service';
import { User } from '../../interfaces/user/user.interface';
import {
  GenericFormComponent,
  FormField,
} from '../../shared/generic-form/generic-form.component';

@Component({
  selector: 'app-patient-profile',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    GenericFormComponent,
  ],
  templateUrl: './patient-profile.component.html',
  styleUrl: './patient-profile.component.css',
})
export class PatientProfileComponent implements OnInit {
  private userService = inject(UserService);
  private patientService = inject(PatientService);
  private localStorage = inject(LocalStorageService);
  private snackBar = inject(MatSnackBar);

  user: User | null = null;
  patient: Patient | null = null;
  isEditing = false;
  isLoading = false;

  formFields: FormField[] = [];
  formData: any = {};

  ngOnInit() {
    this.loadData();
  }

  private loadData() {
    this.isLoading = true;

    const userStr = this.localStorage.getUserData();
    if (!userStr) {
      console.error('No hay usuario autenticado');
      this.isLoading = false;
      return;
    }

    this.user = JSON.parse(userStr);

    this.userService.getUser().subscribe({
      next: (userData) => {
        this.formData = {
          firstName: userData.firstName || '',
          lastName: userData.lastName || '',
          phone: userData.phone || '',
          birthDate: userData.birthDate
            ? new Date(userData.birthDate).toISOString().slice(0, 16)
            : '',
          address: userData.address || '',
          dni: '',
        };

        if (this.user?.patientId) {
          this.patientService.getPatientById(this.user.patientId).subscribe({
            next: (patientData) => {
              this.patient = patientData;
              this.formData.dni = patientData.dni || '';
              if (patientData.address && !userData.address) {
                this.formData.address = patientData.address;
              }
              if (patientData.phone && !userData.phone) {
                this.formData.phone = patientData.phone;
              }
              this.buildFormFields();
              this.isLoading = false;
            },
            error: (error) => {
              console.error('Error al cargar datos del paciente:', error);
              this.buildFormFields();
              this.isLoading = false;
            },
          });
        } else {
          this.buildFormFields();
          this.isLoading = false;
        }
      },
      error: (error) => {
        console.error('Error al cargar datos del usuario:', error);
        this.isLoading = false;
      },
    });
  }

  private buildFormFields() {
    const fields: FormField[] = [
      {
        name: 'firstName',
        label: 'Nombre',
        type: 'text',
        placeholder: 'Ingrese su nombre',
        validators: [Validators.required],
      },
      {
        name: 'lastName',
        label: 'Apellido',
        type: 'text',
        placeholder: 'Ingrese su apellido',
        validators: [Validators.required],
      },
      {
        name: 'phone',
        label: 'Teléfono',
        type: 'text',
        placeholder: 'Ingrese su teléfono',
      },
      {
        name: 'email',
        label: 'Email',
        type: 'text',
        placeholder: 'Email',
      },
      {
        name: 'birthDate',
        label: 'Fecha de Nacimiento',
        type: 'datetime-local',
        placeholder: 'Seleccione su fecha de nacimiento',
      },
    ];

    // Agregar DNI solo si es paciente
    if (this.user?.role === 'PATIENT') {
      fields.push({
        name: 'dni',
        label: 'DNI',
        type: 'text',
        placeholder: 'Ingrese su DNI',
      });
    }

    // Agregar dirección (full width)
    fields.push({
      name: 'address',
      label: 'Dirección',
      type: 'text',
      placeholder: 'Ingrese su dirección',
      fullWidth: true,
    });

    this.formFields = fields;
  }

  toggleEdit() {
    this.isEditing = !this.isEditing;
    if (!this.isEditing) {
      this.loadData();
    }
  }

  onFormSubmit(formValue: any) {
    this.isLoading = true;

    const userUpdate = {
      firstName: formValue.firstName,
      lastName: formValue.lastName,
      phone: formValue.phone,
      address: formValue.address,
      ...(formValue.birthDate && { birthDate: new Date(formValue.birthDate) }),
    };

    this.userService.updateUserProfile(userUpdate).subscribe({
      next: () => {
        if (this.user?.patientId) {
          const patientUpdate: PatientUpdateRequest = {
            dni: formValue.dni ? formValue.dni.trim() : '',
            active: true,
          };

          this.patientService
            .updatePatient(this.user.patientId, patientUpdate)
            .subscribe({
              next: () => {
                this.showSuccessMessage(
                  'Información actualizada correctamente'
                );
                this.isEditing = false;
                this.isLoading = false;
                this.loadData();
              },
              error: (error) => {
                console.error('Error al actualizar paciente:', error);
                this.showErrorMessage(
                  'Error al actualizar los datos del paciente'
                );
                this.isLoading = false;
              },
            });
        } else {
          this.showSuccessMessage('Información actualizada correctamente');
          this.isEditing = false;
          this.isLoading = false;
          this.loadData();
        }
      },
      error: (error) => {
        console.error('Error al actualizar usuario:', error);
        this.showErrorMessage('Error al actualizar la información');
        this.isLoading = false;
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
