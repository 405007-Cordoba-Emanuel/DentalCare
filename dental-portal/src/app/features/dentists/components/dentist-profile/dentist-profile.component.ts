import { Component, inject, OnInit, signal } from '@angular/core';
import { Validators } from '@angular/forms';
import { DentistService } from '../../services/dentist.service';
import { UserService } from '../../../../core/services/auth/user.service';
import {
  FormField,
  GenericFormComponent,
} from '../../../../shared/generic-form/generic-form.component';
import { LocalStorageService } from '../../../../core/services/auth/local-storage.service';

@Component({
  selector: 'app-dentist-profile',
  standalone: true,
  imports: [GenericFormComponent],
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
      type: 'text',
      placeholder: 'YYYY-MM-DD',
      validators: [
        Validators.required,
        Validators.pattern(/^\d{4}-\d{2}-\d{2}$/),
      ],
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

  // ⭐ MODIFICADO: Guardar los datos en el signal
  loadUserProfile() {
    this.userService.getUser().subscribe({
      next: (user) => {
        // Formatear la fecha si es necesario
        const formattedUser = {
          ...user,
          birthDate: user.birthDate ? user.birthDate.toISOString().split('T')[0] : '',
        };
        this.userProfileData.set(formattedUser);
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
          this.dentistProfileData.set(dentist);
        },
        error: (err) => {
          console.error('Error cargando perfil de dentista:', err);
        },
      });
  }
}
