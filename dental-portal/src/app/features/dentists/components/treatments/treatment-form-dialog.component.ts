import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TreatmentService } from '../../services/treatment.service';
import { TreatmentResponse } from '../../interfaces/treatment.interface';

export interface TreatmentFormData {
  dentistId: number;
  patientId: number;
  patientName: string;
  treatment: TreatmentResponse | null; // null = crear, TreatmentResponse = editar
}

@Component({
  selector: 'app-treatment-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSnackBarModule
  ],
  templateUrl: './treatment-form-dialog.component.html',
  styleUrl: './treatment-form-dialog.component.css'
})
export class TreatmentFormDialogComponent implements OnInit {
  isEditMode = false;
  isLoading = false;

  // Campos del formulario
  name = '';
  description = '';
  startDate = '';
  estimatedEndDate = '';
  totalSessions: number | null = null;
  notes = '';
  status = 'EN_CURSO';

  // Opciones de estado
  statusOptions = [
    { value: 'EN_CURSO', label: 'En Curso' },
    { value: 'COMPLETADO', label: 'Completado' },
    { value: 'ABANDONADO', label: 'Abandonado' }
  ];

  constructor(
    public dialogRef: MatDialogRef<TreatmentFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: TreatmentFormData,
    private treatmentService: TreatmentService,
    private snackBar: MatSnackBar
  ) {
    this.isEditMode = !!data.treatment;
  }

  ngOnInit() {
    if (this.isEditMode && this.data.treatment) {
      // Cargar datos del tratamiento para editar
      this.name = this.data.treatment.name || '';
      this.description = this.data.treatment.description || '';
      this.startDate = this.data.treatment.startDate || '';
      this.estimatedEndDate = this.data.treatment.estimatedEndDate || '';
      this.totalSessions = this.data.treatment.totalSessions || null;
      this.notes = this.data.treatment.notes || '';
      this.status = this.data.treatment.status || 'EN_CURSO';
    } else {
      // Fecha actual para nuevo tratamiento
      this.startDate = this.getLocalDateString();
      this.status = 'EN_CURSO';
    }
  }

  private getLocalDateString(): string {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return 'No especificada';
    const parts = dateStr.split('-');
    if (parts.length === 3) {
      const year = parseInt(parts[0]);
      const month = parseInt(parts[1]) - 1;
      const day = parseInt(parts[2]);
      const date = new Date(year, month, day);
      return date.toLocaleDateString('es-ES');
    }
    return dateStr;
  }

  onSave() {
    if (!this.name.trim()) {
      this.snackBar.open('Por favor, ingrese el nombre del tratamiento', 'Cerrar', {
        duration: 3000,
        horizontalPosition: 'end',
        verticalPosition: 'top'
      });
      return;
    }

    if (!this.totalSessions || this.totalSessions <= 0) {
      this.snackBar.open('Por favor, ingrese un número válido de sesiones', 'Cerrar', {
        duration: 3000,
        horizontalPosition: 'end',
        verticalPosition: 'top'
      });
      return;
    }

    this.isLoading = true;

    const treatmentData: any = {
      patientId: this.data.patientId,
      name: this.name.trim(),
      description: this.description.trim(),
      startDate: this.startDate,
      estimatedEndDate: this.estimatedEndDate || null,
      totalSessions: this.totalSessions,
      notes: this.notes.trim() || null
    };

    // Solo incluir el estado si estamos editando
    if (this.isEditMode) {
      treatmentData.status = this.status;
    }

    if (this.isEditMode && this.data.treatment) {
      // Actualizar tratamiento existente
      this.treatmentService.updateTreatment(
        this.data.dentistId,
        this.data.treatment.id,
        treatmentData
      ).subscribe({
        next: () => {
          this.snackBar.open('Tratamiento actualizado exitosamente', 'Cerrar', {
            duration: 3000,
            horizontalPosition: 'end',
            verticalPosition: 'top'
          });
          this.dialogRef.close(true);
        },
        error: (error) => {
          console.error('Error al actualizar tratamiento:', error);
          this.snackBar.open('Error al actualizar el tratamiento', 'Cerrar', {
            duration: 3000,
            horizontalPosition: 'end',
            verticalPosition: 'top'
          });
          this.isLoading = false;
        }
      });
    } else {
      // Crear nuevo tratamiento
      this.treatmentService.createTreatment(this.data.dentistId, treatmentData).subscribe({
        next: () => {
          this.snackBar.open('Tratamiento creado exitosamente', 'Cerrar', {
            duration: 3000,
            horizontalPosition: 'end',
            verticalPosition: 'top'
          });
          this.dialogRef.close(true);
        },
        error: (error) => {
          console.error('Error al crear tratamiento:', error);
          this.snackBar.open('Error al crear el tratamiento', 'Cerrar', {
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

