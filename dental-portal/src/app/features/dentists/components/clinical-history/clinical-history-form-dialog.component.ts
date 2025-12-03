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
import { DentistClinicalHistoryService, ClinicalHistoryEntry } from '../../services/clinical-history.service';

export interface ClinicalHistoryFormData {
  dentistId: number;
  patientId: number;
  patientName: string;
  entry: ClinicalHistoryEntry | null; // null = crear, ClinicalHistoryEntry = editar
}

@Component({
  selector: 'app-clinical-history-form-dialog',
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
  templateUrl: './clinical-history-form-dialog.component.html',
  styleUrl: './clinical-history-form-dialog.component.css'
})
export class ClinicalHistoryFormDialogComponent implements OnInit {
  isEditMode = false;
  isLoading = false;

  description = '';
  selectedFile: File | null = null;
  previewUrl: string | null = null;

  constructor(
    public dialogRef: MatDialogRef<ClinicalHistoryFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ClinicalHistoryFormData,
    private clinicalHistoryService: DentistClinicalHistoryService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.isEditMode = !!this.data.entry;
    
    if (this.isEditMode && this.data.entry) {
      // Cargar datos de la entrada existente
      this.description = this.data.entry.description || '';
      
      // Si hay una imagen existente, mostrar preview
      if (this.data.entry.fileUrl && this.isImageFile(this.data.entry.fileType || '')) {
        this.previewUrl = this.data.entry.fileUrl;
      }
    }
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      
      // Validar que sea una imagen
      if (!file.type.startsWith('image/')) {
        this.snackBar.open('Solo se permiten archivos de imagen', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top',
          panelClass: ['error-snackbar']
        });
        return;
      }

      // Validar tamaño (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.snackBar.open('El archivo no debe exceder 5MB', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top',
          panelClass: ['error-snackbar']
        });
        return;
      }

      this.selectedFile = file;

      // Crear preview de la imagen
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.previewUrl = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  removeFile() {
    this.selectedFile = null;
    this.previewUrl = null;
    
    // Si estamos editando y había una imagen existente, limpiar también el preview
    if (this.isEditMode && this.data.entry?.fileUrl && this.isImageFile(this.data.entry.fileType || '')) {
      this.previewUrl = null;
    }
  }

  isImageFile(fileType: string): boolean {
    return fileType.startsWith('image/');
  }

  onSave() {
    if (!this.description.trim()) {
      this.snackBar.open('Por favor, ingrese una descripción', 'Cerrar', {
        duration: 3000,
        horizontalPosition: 'end',
        verticalPosition: 'top',
        panelClass: ['error-snackbar']
      });
      return;
    }

    this.isLoading = true;

    if (this.isEditMode && this.data.entry) {
      // Actualizar entrada existente
      this.clinicalHistoryService.updateClinicalHistoryEntry(
        this.data.dentistId,
        this.data.entry.id,
        this.data.patientId,
        this.description.trim(),
        this.selectedFile || undefined
      ).subscribe({
        next: () => {
          this.snackBar.open('Entrada actualizada exitosamente', 'Cerrar', {
            duration: 3000,
            horizontalPosition: 'end',
            verticalPosition: 'top',
            panelClass: ['success-snackbar']
          });
          this.dialogRef.close(true);
        },
        error: (error: any) => {
          console.error('Error al actualizar entrada:', error);
          this.snackBar.open('Error al actualizar la entrada', 'Cerrar', {
            duration: 3000,
            horizontalPosition: 'end',
            verticalPosition: 'top',
            panelClass: ['error-snackbar']
          });
          this.isLoading = false;
        }
      });
    } else {
      // Crear nueva entrada
      this.clinicalHistoryService.createClinicalHistoryEntry(
        this.data.dentistId,
        this.data.patientId,
        this.description.trim(),
        this.selectedFile || undefined
      ).subscribe({
        next: () => {
          this.snackBar.open('Entrada creada exitosamente', 'Cerrar', {
            duration: 3000,
            horizontalPosition: 'end',
            verticalPosition: 'top',
            panelClass: ['success-snackbar']
          });
          this.dialogRef.close(true);
        },
        error: (error: any) => {
          console.error('Error al crear entrada:', error);
          this.snackBar.open('Error al crear la entrada', 'Cerrar', {
            duration: 3000,
            horizontalPosition: 'end',
            verticalPosition: 'top',
            panelClass: ['error-snackbar']
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

