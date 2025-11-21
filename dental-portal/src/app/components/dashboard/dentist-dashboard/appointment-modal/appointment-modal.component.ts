import { Component, Inject, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DentistService } from '../../../../core/services/dentist.service';
import { AppointmentRequest } from '../../../../features/dentists/interfaces/appointment.interface';

export interface AppointmentModalData {
  patientId: number;
  patientName: string;
  dentistId: number;
}

@Component({
  selector: 'app-appointment-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './appointment-modal.component.html',
  styleUrl: './appointment-modal.component.css'
})
export class AppointmentModalComponent implements OnInit {
  private fb = inject(FormBuilder);
  private dentistService = inject(DentistService);
  private snackBar = inject(MatSnackBar);
  
  appointmentForm!: FormGroup;
  isLoading = false;
  minDate = new Date();

  constructor(
    public dialogRef: MatDialogRef<AppointmentModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AppointmentModalData
  ) {}

  ngOnInit() {
    this.initForm();
  }

  initForm() {
    this.appointmentForm = this.fb.group({
      appointmentDate: [null, Validators.required],
      startTime: ['', [Validators.required, Validators.pattern(/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/)]],
      endTime: ['', [Validators.required, Validators.pattern(/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/)]],
      reason: ['', [Validators.maxLength(200)]],
      notes: ['', [Validators.maxLength(2000)]]
    });
  }

  onSubmit() {
    if (this.appointmentForm.valid) {
      this.isLoading = true;
      
      const formValue = this.appointmentForm.value;
      const appointmentDate = new Date(formValue.appointmentDate);
      
      // Combinar fecha con hora de inicio
      const [startHour, startMinute] = formValue.startTime.split(':');
      const startDateTime = new Date(appointmentDate);
      startDateTime.setHours(parseInt(startHour), parseInt(startMinute), 0, 0);
      
      // Combinar fecha con hora de fin
      const [endHour, endMinute] = formValue.endTime.split(':');
      const endDateTime = new Date(appointmentDate);
      endDateTime.setHours(parseInt(endHour), parseInt(endMinute), 0, 0);
      
      // Validar que la hora de fin sea posterior a la de inicio
      if (endDateTime <= startDateTime) {
        this.snackBar.open('La hora de fin debe ser posterior a la hora de inicio', 'Cerrar', {
          duration: 4000,
          panelClass: ['error-snackbar']
        });
        this.isLoading = false;
        return;
      }
      
      // Formatear fechas al formato que espera el backend: yyyy-MM-dd'T'HH:mm:ss
      const formatDateTime = (date: Date): string => {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');
        return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
      };
      
      const appointmentRequest: any = {
        patientId: this.data.patientId,
        startDateTime: formatDateTime(startDateTime),
        endDateTime: formatDateTime(endDateTime),
        reason: formValue.reason || '',
        notes: formValue.notes || ''
      };
      
      this.dentistService.createAppointment(this.data.dentistId, appointmentRequest)
        .subscribe({
          next: (response) => {
            this.isLoading = false;
            this.snackBar.open('Cita creada exitosamente. Se han enviado notificaciones por email.', 'Cerrar', {
              duration: 5000,
              panelClass: ['success-snackbar']
            });
            this.dialogRef.close(response);
          },
          error: (error) => {
            this.isLoading = false;
            let errorMessage = 'Error al crear la cita';
            
            if (error.error?.message) {
              errorMessage = error.error.message;
            } else if (error.message) {
              errorMessage = error.message;
            }
            
            this.snackBar.open(errorMessage, 'Cerrar', {
              duration: 5000,
              panelClass: ['error-snackbar']
            });
          }
        });
    } else {
      this.markFormGroupTouched(this.appointmentForm);
    }
  }

  onCancel() {
    this.dialogRef.close();
  }

  private markFormGroupTouched(formGroup: FormGroup) {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
    });
  }

  getErrorMessage(fieldName: string): string {
    const control = this.appointmentForm.get(fieldName);
    
    if (control?.hasError('required')) {
      return 'Este campo es obligatorio';
    }
    
    if (control?.hasError('maxlength')) {
      const maxLength = control.errors?.['maxlength'].requiredLength;
      return `Máximo ${maxLength} caracteres`;
    }
    
    if (control?.hasError('pattern')) {
      return 'Formato inválido (HH:MM)';
    }
    
    return '';
  }
}

