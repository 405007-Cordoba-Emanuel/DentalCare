import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DentistService } from '../../../core/services/dentist.service';
import { AppointmentResponse } from '../../dentists/interfaces/appointment.interface';
import { GenericFormComponent, FormField } from '../../../shared/generic-form/generic-form.component';

@Component({
  selector: 'app-appointment-detail-dialog',
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    GenericFormComponent
  ],
  templateUrl: './appointment-detail-dialog.component.html',
  styleUrl: './appointment-detail-dialog.component.css'
})
export class AppointmentDetailDialogComponent implements OnInit {
  @ViewChild(GenericFormComponent) genericForm!: GenericFormComponent;
  
  private dialogRef = inject(MatDialogRef<AppointmentDetailDialogComponent>);
  private data = inject<{ appointment: AppointmentResponse, dentistId: number, userRole: string }>(MAT_DIALOG_DATA);
  private dentistService = inject(DentistService);
  private snackBar = inject(MatSnackBar);

  appointment: AppointmentResponse;
  dentistId: number;
  userRole: string;
  isEditing = false;
  isSaving = false;
  
  formFields: FormField[] = [];
  initialFormData: any = {};
  disabledFields: string[] = [];

  statuses = [
    { value: 'PROGRAMADO', label: 'Programado', color: 'blue' },
    { value: 'CONFIRMADO', label: 'Confirmado', color: 'green' },
    { value: 'COMPLETADO', label: 'Completado', color: 'gray' },
    { value: 'AUSENTE', label: 'Ausente', color: 'amber' },
    { value: 'CANCELADO', label: 'Cancelado', color: 'red' }
  ];

  constructor() {
    this.appointment = this.data.appointment;
    this.dentistId = this.data.dentistId;
    this.userRole = this.data.userRole;
  }

  ngOnInit() {
    this.initFormFields();
    this.initFormData();
  }

  private initFormFields() {
    // Definir los campos del formulario
    this.formFields = [
      {
        name: 'date',
        label: 'Fecha',
        type: 'date',
        validators: [Validators.required],
        fullWidth: false
      },
      {
        name: 'startTime',
        label: 'Hora de Inicio',
        type: 'time',
        validators: [Validators.required],
        fullWidth: false
      },
      {
        name: 'endTime',
        label: 'Hora de Fin',
        type: 'time',
        validators: [Validators.required],
        fullWidth: false
      },
      {
        name: 'reason',
        label: 'Motivo de la Consulta',
        type: 'text',
        validators: [Validators.required, Validators.maxLength(200)],
        fullWidth: true
      },
      {
        name: 'notes',
        label: 'Notas Adicionales',
        type: 'textarea',
        validators: [Validators.maxLength(2000)],
        fullWidth: true
      }
    ];

    // Agregar el campo de estado solo para dentistas
    if (this.userRole === 'DENTIST') {
      this.formFields.push({
        name: 'status',
        label: 'Estado',
        type: 'select',
        options: this.statuses,
        fullWidth: false
      });
    }
  }

  private initFormData() {
    // Extraer fecha y hora del startDateTime y endDateTime
    const startDateTime = this.appointment.startDateTime || `${this.appointment.date}T${this.appointment.startTime}`;
    const endDateTime = this.appointment.endDateTime || `${this.appointment.date}T${this.appointment.endTime}`;

    const startDate = new Date(startDateTime);
    const endDate = new Date(endDateTime);

    this.initialFormData = {
      date: this.formatDate(startDate),
      startTime: this.formatTime(startDate),
      endTime: this.formatTime(endDate),
      reason: this.appointment.reason || '',
      notes: this.appointment.notes || '',
      status: this.appointment.status
    };
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private formatTime(date: Date): string {
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
  }

  toggleEdit() {
    this.isEditing = !this.isEditing;
    if (!this.isEditing) {
      // Restaurar valores originales
      this.initFormData();
    }
  }

  onFormSubmit(formValue: any) {
    if (this.userRole !== 'DENTIST') {
      this.snackBar.open('Solo los dentistas pueden actualizar citas', 'Cerrar', {
        duration: 3000,
        panelClass: ['error-snackbar']
      });
      return;
    }

    this.isSaving = true;

    const date = new Date(formValue.date);
    const [startHours, startMinutes] = formValue.startTime.split(':');
    const [endHours, endMinutes] = formValue.endTime.split(':');

    const startDateTime = new Date(date);
    startDateTime.setHours(parseInt(startHours), parseInt(startMinutes), 0, 0);

    const endDateTime = new Date(date);
    endDateTime.setHours(parseInt(endHours), parseInt(endMinutes), 0, 0);

    // Validar que la hora de inicio sea antes que la de fin
    if (startDateTime >= endDateTime) {
      this.snackBar.open('La hora de inicio debe ser anterior a la hora de fin', 'Cerrar', {
        duration: 3000,
        panelClass: ['error-snackbar']
      });
      this.isSaving = false;
      return;
    }

    // Detectar cambios significativos
    const hasDateOrTimeChanged = this.hasDateOrTimeChanged(startDateTime, endDateTime);
    const hasStatusChanged = formValue.status && formValue.status !== this.appointment.status;

    const updateRequest = {
      startDateTime: this.formatDateTimeForBackend(startDateTime),
      endDateTime: this.formatDateTimeForBackend(endDateTime),
      reason: formValue.reason,
      notes: formValue.notes
    };

    this.dentistService.updateAppointment(this.dentistId, this.appointment.id, updateRequest).subscribe({
      next: (updatedAppointment) => {
        // Si el estado cambió, actualizar el estado también
        if (hasStatusChanged) {
          this.dentistService.updateAppointmentStatus(this.dentistId, this.appointment.id, formValue.status).subscribe({
            next: () => {
              this.showSuccessMessage(hasDateOrTimeChanged, hasStatusChanged);
              this.dialogRef.close({ updated: true, appointment: updatedAppointment });
              this.isSaving = false;
            },
            error: (error) => {
              console.error('Error updating status:', error);
              this.snackBar.open('Error al actualizar el estado de la cita', 'Cerrar', {
                duration: 3000,
                panelClass: ['error-snackbar']
              });
              this.isSaving = false;
            }
          });
        } else {
          this.showSuccessMessage(hasDateOrTimeChanged, false);
          this.dialogRef.close({ updated: true, appointment: updatedAppointment });
          this.isSaving = false;
        }
      },
      error: (error) => {
        console.error('Error updating appointment:', error);
        let errorMessage = 'Error al actualizar la cita';
        if (error.error?.message) {
          errorMessage = error.error.message;
        }
        this.snackBar.open(errorMessage, 'Cerrar', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
        this.isSaving = false;
      }
    });
  }

  private hasDateOrTimeChanged(newStartDateTime: Date, newEndDateTime: Date): boolean {
    const originalStartDateTime = this.appointment.startDateTime || 
                                  `${this.appointment.date}T${this.appointment.startTime}`;
    const originalEndDateTime = this.appointment.endDateTime || 
                               `${this.appointment.date}T${this.appointment.endTime}`;

    const originalStart = new Date(originalStartDateTime);
    const originalEnd = new Date(originalEndDateTime);

    return originalStart.getTime() !== newStartDateTime.getTime() ||
           originalEnd.getTime() !== newEndDateTime.getTime();
  }

  private showSuccessMessage(hasDateOrTimeChanged: boolean, hasStatusChanged: boolean) {
    let message = 'Cita actualizada exitosamente';
    
    if (hasDateOrTimeChanged || hasStatusChanged) {
      message += '. Se ha enviado un email de notificación al paciente';
    }

    this.snackBar.open(message, 'Cerrar', {
      duration: 5000,
      panelClass: ['success-snackbar']
    });
  }

  private formatDateTimeForBackend(date: Date): string {
    // Formato: yyyy-MM-dd'T'HH:mm:ss (sin zona horaria)
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');
    
    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
  }

  onCancel() {
    if (confirm('¿Estás seguro de que deseas cancelar esta cita? Esta acción no se puede deshacer.')) {
      this.dentistService.cancelAppointment(this.dentistId, this.appointment.id).subscribe({
        next: () => {
          this.snackBar.open('Cita cancelada exitosamente', 'Cerrar', {
            duration: 3000,
            panelClass: ['success-snackbar']
          });
          this.dialogRef.close({ updated: true, cancelled: true });
        },
        error: (error) => {
          console.error('Error cancelling appointment:', error);
          this.snackBar.open('Error al cancelar la cita', 'Cerrar', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
      });
    }
  }

  onClose() {
    this.dialogRef.close();
  }

  getStatusColor(status: string): string {
    const statusObj = this.statuses.find(s => s.value === status);
    return statusObj ? statusObj.color : 'gray';
  }

  getStatusLabel(status: string): string {
    const statusObj = this.statuses.find(s => s.value === status);
    return statusObj ? statusObj.label : status;
  }
}
