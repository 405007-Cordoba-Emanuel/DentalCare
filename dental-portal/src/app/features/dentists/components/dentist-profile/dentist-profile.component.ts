import { Component, inject, OnInit, signal } from '@angular/core';
import { Validators } from '@angular/forms';
import { DentistService } from '../../services/dentist.service';
import { UserService } from '../../../../core/services/auth/user.service';
import {
  FormField,
  GenericFormComponent,
} from '../../../../shared/generic-form/generic-form.component';
import { LocalStorageService } from '../../../../core/services/auth/local-storage.service';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dentist-profile',
  standalone: true,
  imports: [GenericFormComponent, MatIconModule, CommonModule],
  templateUrl: './dentist-profile.component.html',
  styleUrls: ['./dentist-profile.component.css'],
})
export class DentistProfileComponent implements OnInit {
  private userService = inject(UserService);
  private dentistService = inject(DentistService);
  private localStorageService = inject(LocalStorageService);

  // ⭐ NUEVO: Signals para los datos cargados
  userProfileData = signal<any>({});
  dentistProfileData = signal<any>({});

  // Estados de carga y mensajes
  loadingPersonal = signal(false);
  loadingProfessional = signal(false);
  successPersonal = signal(false);
  successProfessional = signal(false);
  errorPersonal = signal<string | null>(null);
  errorProfessional = signal<string | null>(null);

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
      label: 'Ingrese su número de teléfono',
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
    {
      name: 'active',
      label: 'Estado',
      type: 'select',
      options: [
        { label: 'Activo', value: true },
        { label: 'Inactivo', value: false },
      ],
      validators: [Validators.required],
      fullWidth: true,
    },
  ]);

  // Método para actualizar datos personales
  updatePersonalData(formData: any) {
    this.loadingPersonal.set(true);
    this.successPersonal.set(false);
    this.errorPersonal.set(null);
    this.userService.updateUserProfile(formData).subscribe({
      next: () => {
        this.loadingPersonal.set(false);
        this.successPersonal.set(true);
        this.editingPersonal.set(false); // Volver a modo visual
        this.loadUserProfile(); // Recargar datos actualizados
        setTimeout(() => this.successPersonal.set(false), 3000);
      },
      error: (err) => {
        this.loadingPersonal.set(false);
        this.errorPersonal.set(
          err.error?.message || 'Error al actualizar datos personales'
        );
        setTimeout(() => this.errorPersonal.set(null), 5000);
      },
    });
  }

  // Método para actualizar datos profesionales
  updateProfessionalData(formData: any) {
    this.loadingProfessional.set(true);
    this.successProfessional.set(false);
    this.errorProfessional.set(null);

    const payload = {
      ...formData,
      active: formData.active === 'true' || formData.active === true,
    };

    this.dentistService
      .updateDentist(this.localStorageService.getDentistId(), payload)
      .subscribe({
        next: () => {
          this.loadingProfessional.set(false);
          this.successProfessional.set(true);
          this.editingProfessional.set(false); // Volver a modo visual
          this.loadDentistProfile(); // Recargar datos actualizados
          setTimeout(() => this.successProfessional.set(false), 3000);
        },
        error: (err) => {
          this.loadingProfessional.set(false);
          this.errorProfessional.set(
            err.error?.message || 'Error al actualizar datos profesionales'
          );
          setTimeout(() => this.errorProfessional.set(null), 5000);
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

  // ⭐ MODIFICADO: Guardar los datos en el signal
  loadUserProfile() {
    this.userService.getUser().subscribe({
      next: (user) => {
        // birthDate ya viene como string en formato YYYY-MM-DD
        this.userProfileData.set(user);
      },
      error: (err) => {
        console.error('Error cargando perfil de usuario:', err);
      },
    });
  }

  // ⭐ MODIFICADO: Guardar los datos en el signal
  loadDentistProfile() {
    this.dentistService
      .getDentistById(this.localStorageService.getDentistId())
      .subscribe({
        next: (dentist) => {
          // Mapear solo los campos profesionales que necesita el formulario
          const professionalData = {
            licenseNumber: dentist.licenseNumber || '',
            specialty: dentist.specialty || '',
            active: dentist.active !== undefined ? dentist.active : true,
          };
          this.dentistProfileData.set(professionalData);
        },
        error: (err) => {
          console.error('Error cargando perfil de dentista:', err);
        },
      });
  }
}
