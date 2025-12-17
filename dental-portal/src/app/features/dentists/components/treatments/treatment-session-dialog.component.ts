import { Component, Inject, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DentistClinicalHistoryService } from '../../services/clinical-history.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiConfig } from '../../../../core/config/api.config';

export interface TreatmentSessionFormData {
  dentistId: number;
  patientId: number;
  treatmentId: number;
  treatmentName: string;
}

@Component({
  selector: 'app-treatment-session-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule
  ],
  templateUrl: './treatment-session-dialog.component.html',
  styleUrl: './treatment-session-dialog.component.css'
})
export class TreatmentSessionDialogComponent implements OnInit {
  isLoading = false;
  description = '';
  selectedFile: File | null = null;
  previewUrl: string | null = null;

  private apiConfig = inject(ApiConfig);
  private apiUrl = this.apiConfig.coreDentistUrl;

  constructor(
    public dialogRef: MatDialogRef<TreatmentSessionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: TreatmentSessionFormData,
    private clinicalHistoryService: DentistClinicalHistoryService,
    private http: HttpClient,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {}

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      
      // Validar tipo de archivo (imagen o PDF)
      const allowedTypes = ['image/jpeg', 'image/png', 'image/jpg', 'application/pdf'];
      if (!allowedTypes.includes(file.type)) {
        this.snackBar.open('Solo se permiten archivos JPG, PNG o PDF', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        return;
      }

      // Validar tamaño (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.snackBar.open('El archivo no debe exceder 5MB', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        return;
      }

      this.selectedFile = file;

      // Crear preview si es imagen
      if (file.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.previewUrl = e.target.result;
        };
        reader.readAsDataURL(file);
      } else {
        this.previewUrl = null;
      }
    }
  }

  removeFile() {
    this.selectedFile = null;
    this.previewUrl = null;
  }

  onSave() {
    if (!this.description.trim()) {
      this.snackBar.open('Por favor, ingrese una descripción de la sesión', 'Cerrar', {
        duration: 3000,
        horizontalPosition: 'end',
        verticalPosition: 'top'
      });
      return;
    }

    this.isLoading = true;

    const formData = new FormData();
    if (this.selectedFile) {
      formData.append('file', this.selectedFile);
    }

    // El backend espera description y treatmentId como query params, file como multipart
    this.createEntryWithTreatment(this.data.dentistId, formData).subscribe({
      next: () => {
        this.snackBar.open('Sesión agregada exitosamente', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.dialogRef.close(true);
      },
      error: (error) => {
        console.error('Error al agregar sesión:', error);
        this.snackBar.open('Error al agregar la sesión', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.isLoading = false;
      }
    });
  }

  private createEntryWithTreatment(dentistId: number, formData: FormData): Observable<any> {
    const params: any = {
      description: this.description.trim(),
      treatmentId: this.data.treatmentId.toString()
    };

    return this.http.post(
      `${this.apiUrl}/${dentistId}/patients/${this.data.patientId}/clinical-history`,
      formData,
      { params }
    );
  }

  onCancel() {
    this.dialogRef.close(false);
  }
}

