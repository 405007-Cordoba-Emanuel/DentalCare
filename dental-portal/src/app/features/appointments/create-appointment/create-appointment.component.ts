import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { DentistService } from '../../../core/services/dentist.service';
import { PatientSummary } from '../../dentists/interfaces/patient.interface';
import { AppointmentRequest } from '../../dentists/interfaces/appointment.interface';
import { LocalStorageService } from '../../../core/services/auth/local-storage.service';
import { GenericFormComponent } from '../../../shared/generic-form/generic-form.component';
import { FormField } from '../../../shared/generic-form/generic-form.component';
import { Validators } from '@angular/forms';
import { catchError, of, switchMap } from 'rxjs';

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
  errorMessage: string = '';
  successMessage: string = '';

  ngOnInit() {
    this.loadPatients();
  }

  private initializeFormFields() {
    this.formFields = [
      {
        name: 'patientId',
        label: 'Paciente',
        type: 'select',
        options: [
          { label: 'Seleccionar un paciente', value: '' },
          ...this.patients.map((p) => ({
            label: `${p.firstName} ${p.lastName}`,
            value: p.id,
          })),
        ],
        validators: [Validators.required],
      },
      {
        name: 'appointmentDate',
        label: 'Fecha de la Cita',
        type: 'date',
        validators: [Validators.required],
        fullWidth: true,
      },
      {
        name: 'startTime',
        label: 'Horario de Inicio',
        type: 'time',
        validators: [Validators.required],
      },
      {
        name: 'endTime',
        label: 'Horario de Fin',
        type: 'time',
        validators: [Validators.required],
      },
      {
        name: 'reason',
        label: 'Motivo',
        type: 'text',
        placeholder: 'Motivo de la cita',
        validators: [Validators.required],
        fullWidth: true,
      },
      {
        name: 'notes',
        label: 'Notas',
        type: 'textarea',
        placeholder: 'Notas adicionales...',
        validators: [],
        fullWidth: true,
      },
    ];
  }

  handleFormSubmit(data: any) {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Validación 1: Verificar que endTime > startTime
    if (!this.validateTimeRange(data.startTime, data.endTime)) {
      this.errorMessage = 'La hora de fin debe ser posterior a la hora de inicio';
      this.loading = false;
      return;
    }

    // Combinar fecha con horarios (agregar :00 para los segundos)
    const startDateTime = `${data.appointmentDate}T${data.startTime}:00`;
    const endDateTime = `${data.appointmentDate}T${data.endTime}:00`;

    // Validación 2: Verificar conflictos de horario con el servidor
    this.dentistService
      .checkTimeConflict(this.dentistId, startDateTime, endDateTime)
      .pipe(
        switchMap((hasConflict) => {
          if (hasConflict) {
            this.errorMessage = 'Ya existe un turno en ese horario. Por favor, selecciona otro horario.';
            this.loading = false;
            throw new Error('Time conflict');
          }

          // Si no hay conflicto, crear el turno
          const appointmentData: AppointmentRequest = {
            patientId: Number(data.patientId),
            startDateTime: startDateTime,
            endDateTime: endDateTime,
            reason: data.reason,
            notes: data.notes || '',
          };

          return this.dentistService.createAppointment(this.dentistId, appointmentData);
        }),
        catchError((error) => {
          if (error.message !== 'Time conflict') {
            console.error('Error creating appointment:', error);
            this.errorMessage = 'Error al crear el turno. Por favor, intenta nuevamente.';
            this.loading = false;
          }
          return of(null);
        })
      )
      .subscribe({
        next: (response) => {
          if (response) {
            this.successMessage = 'Turno creado exitosamente';
            setTimeout(() => {
              this.router.navigate(['/dentist/appointments']);
            }, 1500);
          }
        },
      });
  }

  // Validar que el horario de fin sea posterior al de inicio
  private validateTimeRange(startTime: string, endTime: string): boolean {
    const [startHours, startMinutes] = startTime.split(':').map(Number);
    const [endHours, endMinutes] = endTime.split(':').map(Number);

    const startTotalMinutes = startHours * 60 + startMinutes;
    const endTotalMinutes = endHours * 60 + endMinutes;

    return endTotalMinutes > startTotalMinutes;
  }

  private loadPatients() {
    this.loading = true;
    this.dentistService.getPatientsByDentistId(this.dentistId).subscribe({
      next: (patients) => {
        this.patients = patients.patients;
        this.initializeFormFields();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading patients:', error);
        this.loading = false;
      },
    });
  }
}
