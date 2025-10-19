import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { DentistService } from '../../dentists/services/dentist.service';
import { PatientSummary } from '../../dentists/interfaces/patient.interface';
import { AppointmentRequest } from '../../dentists/interfaces/appointment.interface';
import { LocalStorageService } from '../../../core/services/auth/local-storage.service';
import { GenericFormComponent } from '../../../shared/generic-form/generic-form.component';

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
  formFields: any[] = [];

  ngOnInit() {
    this.loadPatients();
  }

  loadPatients() {
    this.loading = true;
    this.dentistService.getActivePatientsByDentistId(this.dentistId).subscribe({
      next: (response) => {
        this.patients = response.patients;

        // Configurar los campos del formulario después de cargar pacientes
        this.formFields = [
          {
            name: 'patientId',
            label: 'Patient',
            type: 'select',
            options: this.patients.map((p) => ({
              label: `${p.firstName} ${p.lastName}`,
              value: p.id,
            })),
          },
          {
            name: 'startDateTime',
            label: 'Start Date and Time',
            type: 'datetime-local',
          },
          {
            name: 'endDateTime',
            label: 'End Date and Time',
            type: 'datetime-local',
          },
          {
            name: 'reason',
            label: 'Reason',
            type: 'text',
            placeholder: 'Reason for appointment',
          },
          {
            name: 'notes',
            label: 'Notes',
            type: 'textarea',
            placeholder: 'Additional notes...',
          },
        ];
      },
      error: (error) => {
        console.error('Error loading patients:', error);
      },
      complete: () => {
        this.loading = false;
      },
    });
  }

  handleFormSubmit(data: any) {
    this.loading = true;
    console.log('Form data:', data);
    this.dentistService
      .createAppointment(this.dentistId, data as AppointmentRequest)
      .subscribe({
        next: (response) => {
          console.log('Appointment created:', response);
        },
        error: (error) => {
          console.error('Error creating appointment:', error);
        },
        complete: () => {
          this.loading = false;
          this.router.navigate(['/dentist/appointments']);
        },
      });
  }
}
