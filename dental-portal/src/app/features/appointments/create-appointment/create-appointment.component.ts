import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { DentistService } from '../../dentists/services/dentist.service';
import { PatientSummary } from '../../dentists/interfaces/patient.interface';
import { AppointmentRequest } from '../../dentists/interfaces/appointment.interface';
import { LocalStorageService } from '../../../core/services/auth/local-storage.service';
import { GenericFormComponent } from '../../../shared/generic-form/generic-form.component';
import { FormField } from '../../../shared/generic-form/generic-form.component';
import { Validators } from '@angular/forms';

@Component({
  selector: 'app-create-appointment',
  standalone: true,
  imports: [CommonModule, GenericFormComponent],
  templateUrl: './create-appointment.component.html',
})
export class CreateAppointmentComponent {
  private dentistService = inject(DentistService);
  private localStorage = inject(LocalStorageService);
  private dentistId = this.localStorage.getDentistId();
  private router = inject(Router);

  patients: PatientSummary[] = [];
  loading = false;
  formFields: FormField[] = []; // ✅ Inicializado como array vacío

  ngOnInit() {
    this.formFields = [
      {
        name: 'patientId',
        label: 'Paciente',
        type: 'select',
        options: [
          { label: 'Seleccionar un paciente', value: '' }, // ✅ Opción por defecto
          ...this.patients.map((p) => ({
            label: `${p.firstName} ${p.lastName}`,
            value: p.id,
          })),
        ],
        validators: [Validators.required], // ✅ Agregado
      },
      {
        name: 'startDateTime',
        label: 'Fecha y Hora de Inicio',
        type: 'datetime-local',
        validators: [Validators.required], // ✅ Agregado
      },
      {
        name: 'endDateTime',
        label: 'Fecha y Hora de Fin',
        type: 'datetime-local',
        validators: [Validators.required], // ✅ Agregado
      },
      {
        name: 'reason',
        label: 'Motivo',
        type: 'text',
        placeholder: 'Motivo de la cita',
        validators: [Validators.required], // ✅ Agregado
      },
      {
        name: 'notes',
        label: 'Notas',
        type: 'textarea',
        placeholder: 'Notas adicionales...',
        validators: [], // ✅ Opcional, sin validadores
      },
    ];
  }

  handleFormSubmit(data: any) {
    this.loading = true;
    console.log('Form data:', data);

    const appointmentData: AppointmentRequest = {
      patientId: Number(data.patientId), // ✅ Asegúrate de convertir a número si es necesario
      startDateTime: data.startDateTime,
      endDateTime: data.endDateTime,
      reason: data.reason,
      notes: data.notes,
    };

    this.dentistService
      .createAppointment(this.dentistId, appointmentData)
      .subscribe({
        next: (response) => {
          console.log('Appointment created:', response);
          this.router.navigate(['/dentist/appointments']);
        },
        error: (error) => {
          console.error('Error creating appointment:', error);
          this.loading = false;
        },
      });
  }
}
