import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PrescriptionService, Prescription } from '../../../../core/services/prescription.service';
import { DentistService } from '../../../../core/services/dentist.service';

export interface PrescriptionFormData {
  dentistId: number;
  patientId: number;
  patientName: string;
  prescription: Prescription | null; // null = crear, Prescription = editar
}

@Component({
  selector: 'app-prescription-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatDividerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule
  ],
  templateUrl: './prescription-form-dialog.component.html',
  styleUrl: './prescription-form-dialog.component.css'
})
export class PrescriptionFormDialogComponent implements OnInit {
  isEditMode = false;
  isLoading = false;

  // Información del dentista (precargada)
  dentistInfo = {
    name: 'Cargando...',
    licenseNumber: '...',
    specialty: '...'
  };

  // Información de la receta
  medications = '';
  observations = '';
  prescriptionDate = '';

  constructor(
    public dialogRef: MatDialogRef<PrescriptionFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: PrescriptionFormData,
    private prescriptionService: PrescriptionService,
    private dentistService: DentistService,
    private snackBar: MatSnackBar
  ) {
    this.isEditMode = !!data.prescription;
  }

  ngOnInit() {
    this.loadDentistInfo();
    
    if (this.isEditMode && this.data.prescription) {
      // Cargar datos de la receta para editar
      this.medications = this.data.prescription.medications || '';
      this.observations = this.data.prescription.observations || '';
      this.prescriptionDate = this.data.prescription.prescriptionDate || '';
      
      // Verificar si la receta es antigua
      this.checkOldPrescription();
    } else {
      // Fecha actual para nueva receta (usando fecha local)
      this.prescriptionDate = this.getLocalDateString();
    }
  }

  private loadDentistInfo() {
    this.dentistService.getDentistById(this.data.dentistId).subscribe({
      next: (dentist) => {
        this.dentistInfo = {
          name: `${dentist.firstName || ''} ${dentist.lastName || ''}`.trim() || 'Sin nombre',
          licenseNumber: dentist.licenseNumber || 'Sin matrícula',
          specialty: dentist.specialty || 'Sin especialidad'
        };
      },
      error: (error) => {
        console.error('Error al cargar información del dentista:', error);
        this.dentistInfo = {
          name: 'Error al cargar',
          licenseNumber: '...',
          specialty: '...'
        };
      }
    });
  }

  private checkOldPrescription() {
    if (!this.data.prescription) return;

    const prescriptionDate = new Date(this.data.prescription.prescriptionDate);
    const today = new Date();
    const daysDiff = Math.floor((today.getTime() - prescriptionDate.getTime()) / (1000 * 60 * 60 * 24));

    // Si la receta tiene más de 30 días, mostrar alerta
    if (daysDiff > 30) {
      this.snackBar.open(
        `Advertencia: Está editando una receta con fecha antigua (${this.formatDate(this.data.prescription.prescriptionDate)}). La fecha se actualizará a la fecha actual.`,
        'Cerrar',
        {
          duration: 5000,
          horizontalPosition: 'end',
          verticalPosition: 'top',
          panelClass: ['warning-snackbar']
        }
      );
    }
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return 'No especificada';
    const date = new Date(dateStr);
    return date.toLocaleDateString('es-ES');
  }

  private getLocalDateString(): string {
    // Obtener fecha actual en zona horaria local de la máquina
    // Usar métodos directos para evitar problemas de conversión UTC
    const now = new Date();
    
    // Obtener componentes de fecha en zona horaria local
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0'); // getMonth() es 0-indexed
    const day = String(now.getDate()).padStart(2, '0');
    
    // Retornar en formato ISO: YYYY-MM-DD
    return `${year}-${month}-${day}`;
  }

  onSave() {
    if (!this.medications.trim()) {
      this.snackBar.open('Por favor, ingrese al menos un medicamento', 'Cerrar', {
        duration: 3000,
        horizontalPosition: 'end',
        verticalPosition: 'top'
      });
      return;
    }

    this.isLoading = true;

    const prescriptionData = {
      patientId: this.data.patientId,
      prescriptionDate: this.getLocalDateString(), // Fecha local sin problemas de zona horaria
      medications: this.medications.trim(),
      observations: this.observations.trim()
    };

    if (this.isEditMode && this.data.prescription) {
      // Actualizar receta existente
      this.prescriptionService.updatePrescription(
        this.data.dentistId,
        this.data.prescription.id,
        prescriptionData
      ).subscribe({
        next: () => {
          this.snackBar.open('Receta actualizada exitosamente', 'Cerrar', {
            duration: 3000,
            horizontalPosition: 'end',
            verticalPosition: 'top'
          });
          this.dialogRef.close(true);
        },
        error: (error) => {
          console.error('Error al actualizar receta:', error);
          this.snackBar.open('Error al actualizar la receta', 'Cerrar', {
            duration: 3000,
            horizontalPosition: 'end',
            verticalPosition: 'top'
          });
          this.isLoading = false;
        }
      });
    } else {
      // Crear nueva receta
      this.prescriptionService.createPrescription(this.data.dentistId, prescriptionData).subscribe({
        next: () => {
          this.snackBar.open('Receta creada exitosamente', 'Cerrar', {
            duration: 3000,
            horizontalPosition: 'end',
            verticalPosition: 'top'
          });
          this.dialogRef.close(true);
        },
        error: (error) => {
          console.error('Error al crear receta:', error);
          this.snackBar.open('Error al crear la receta', 'Cerrar', {
            duration: 3000,
            horizontalPosition: 'end',
            verticalPosition: 'top'
          });
          this.isLoading = false;
        }
      });
    }
  }

  onCancel() {
    this.dialogRef.close(false);
  }
}

