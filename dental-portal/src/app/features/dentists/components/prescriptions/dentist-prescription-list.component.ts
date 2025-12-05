import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LocalStorageService } from '../../../../core/services/auth/local-storage.service';
import { PrescriptionService, Prescription } from '../../../../core/services/prescription.service';
import { DentistService } from '../../../../core/services/dentist.service';
import { User } from '../../../../interfaces/user/user.interface';
import { PrescriptionFormDialogComponent } from './prescription-form-dialog.component';
import { ConfirmDeleteDialogComponent, ConfirmDeleteData } from './confirm-delete-dialog.component';

@Component({
  selector: 'app-dentist-prescription-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatDialogModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './dentist-prescription-list.component.html',
  styleUrl: './dentist-prescription-list.component.css'
})
export class DentistPrescriptionListComponent implements OnInit {
  private prescriptionService = inject(PrescriptionService);
  private localStorage = inject(LocalStorageService);
  private dentistService = inject(DentistService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  user: User | null = null;
  patientId: number | null = null;
  dentistId: number | null = null;
  prescriptions: Prescription[] = [];
  filteredPrescriptions: Prescription[] = [];
  isLoading = false;
  searchTerm = '';
  totalPrescriptions = 0;
  thisMonthPrescriptions = 0;
  deletingPrescriptionIds = new Set<number>(); // Rastrear recetas que se están eliminando

  // Información del paciente
  patientInfo = {
    firstName: 'Cargando',
    lastName: '...',
    dni: '...'
  };

  ngOnInit() {
    this.loadData();
  }

  private loadData() {
    this.isLoading = true;

    // Obtener patientId de la ruta
    this.route.params.subscribe(params => {
      this.patientId = +params['patientId'];
      if (this.patientId) {
        this.loadUserAndDentist();
      } else {
        this.isLoading = false;
        this.snackBar.open('Error: No se encontró el ID del paciente', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
      }
    });
  }

  private loadUserAndDentist() {
    const userStr = this.localStorage.getUserData();
    if (!userStr) {
      console.error('No hay usuario autenticado');
      this.isLoading = false;
      return;
    }

    this.user = JSON.parse(userStr);

    // Obtener dentistId
    if (this.user?.id) {
      const userId = parseInt(this.user.id, 10);
      if (!isNaN(userId)) {
        this.dentistService.getDentistIdByUserId(userId.toString()).subscribe({
          next: (dentistId) => {
            this.dentistId = dentistId;
            this.loadPrescriptions();
            this.loadPatientInfo();
          },
          error: (error) => {
            console.error('Error al obtener dentistId:', error);
            this.isLoading = false;
          }
        });
      }
    }
  }

  private loadPatientInfo() {
    if (!this.dentistId || !this.patientId) return;

    this.dentistService.getPatientsByDentistId(this.dentistId).subscribe({
      next: (response) => {
        const patient = response.patients.find(p => p.id === this.patientId);
        if (patient) {
          this.patientInfo = {
            firstName: patient.firstName || 'Sin nombre',
            lastName: patient.lastName || '',
            dni: patient.dni || 'Sin DNI'
          };
        }
      },
      error: (error) => {
        console.error('Error al cargar información del paciente:', error);
      }
    });
  }

  private loadPrescriptions() {
    if (!this.dentistId || !this.patientId) {
      this.isLoading = false;
      return;
    }

    this.prescriptionService.getPrescriptionsByDentistIdAndPatientId(this.dentistId, this.patientId).subscribe({
      next: (data) => {
        this.prescriptions = data;
        this.filteredPrescriptions = data;
        this.totalPrescriptions = data.length; // Actualizar total inmediatamente
        this.calculateKPIs();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error al cargar recetas:', error);
        this.isLoading = false;
        this.snackBar.open('Error al cargar recetas', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
      }
    });
  }

  private calculateKPIs() {
    const now = new Date();
    const currentMonth = now.getMonth();
    const currentYear = now.getFullYear();

    this.thisMonthPrescriptions = this.prescriptions.filter(prescription => {
      const prescriptionDate = new Date(prescription.prescriptionDate);
      return prescriptionDate.getMonth() === currentMonth && 
             prescriptionDate.getFullYear() === currentYear;
    }).length;
  }

  onSearchChange() {
    if (!this.searchTerm.trim()) {
      this.filteredPrescriptions = this.prescriptions;
      return;
    }

    const search = this.searchTerm.toLowerCase().trim();
    this.filteredPrescriptions = this.prescriptions.filter(prescription =>
      prescription.medications?.toLowerCase().includes(search) ||
      prescription.observations?.toLowerCase().includes(search) ||
      prescription.dentistName?.toLowerCase().includes(search)
    );
  }

  openCreatePrescriptionDialog() {
    if (!this.dentistId || !this.patientId) return;

    const dialogRef = this.dialog.open(PrescriptionFormDialogComponent, {
      width: '600px',
      maxWidth: '90vw',
      data: {
        dentistId: this.dentistId,
        patientId: this.patientId,
        patientName: `${this.patientInfo.firstName} ${this.patientInfo.lastName}`,
        prescription: null // null indica que es creación
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadPrescriptions();
      }
    });
  }

  openEditPrescriptionDialog(prescription: Prescription) {
    if (!this.dentistId || !this.patientId) return;

    const dialogRef = this.dialog.open(PrescriptionFormDialogComponent, {
      width: '600px',
      maxWidth: '90vw',
      data: {
        dentistId: this.dentistId,
        patientId: this.patientId,
        patientName: `${this.patientInfo.firstName} ${this.patientInfo.lastName}`,
        prescription: prescription // prescription indica que es edición
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadPrescriptions();
      }
    });
  }

  downloadPrescriptionPdf(prescription: Prescription) {
    if (!this.dentistId) return;

    this.prescriptionService.downloadPrescriptionPdf(this.dentistId, prescription.id).subscribe({
      next: (response) => {
        const url = window.URL.createObjectURL(response.blob);
        const link = document.createElement('a');
        link.href = url;
        
        // Usar el nombre del archivo que viene del backend
        link.download = response.filename;
        
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);

        this.snackBar.open('Receta descargada exitosamente', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
      },
      error: (error) => {
        console.error('Error al descargar receta:', error);
        this.snackBar.open('Error al descargar la receta', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
      }
    });
  }

  confirmDeletePrescription(prescription: Prescription) {
    const dialogRef = this.dialog.open(ConfirmDeleteDialogComponent, {
      width: '500px',
      maxWidth: '90vw',
      data: {
        title: 'Eliminar receta',
        message: '¿Está seguro de eliminar este registro?',
        itemName: 'Receta',
        itemDetails: {
          patientName: prescription.patientName,
          date: this.formatDate(prescription.prescriptionDate)
        }
      } as ConfirmDeleteData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.dentistId) {
        this.deletePrescription(prescription.id);
      }
    });
  }

  private deletePrescription(prescriptionId: number) {
    if (!this.dentistId) return;

    // Evitar eliminaciones duplicadas
    if (this.deletingPrescriptionIds.has(prescriptionId)) {
      return;
    }

    // Marcar esta receta como en proceso de eliminación
    this.deletingPrescriptionIds.add(prescriptionId);

    this.prescriptionService.deletePrescription(this.dentistId, prescriptionId).subscribe({
      next: () => {
        this.deletingPrescriptionIds.delete(prescriptionId);
        this.snackBar.open('Receta eliminada exitosamente', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.loadPrescriptions();
      },
      error: (error) => {
        console.error('Error al eliminar receta:', error);
        this.deletingPrescriptionIds.delete(prescriptionId);
        
        // Mensaje de error más específico
        let errorMessage = 'Error al eliminar la receta';
        if (error.status === 404) {
          errorMessage = 'La receta no fue encontrada o ya fue eliminada';
        } else if (error.status === 400) {
          errorMessage = error.error?.message || 'No se puede eliminar esta receta';
        } else if (error.status === 500) {
          errorMessage = 'Error del servidor al eliminar la receta';
        }
        
        this.snackBar.open(errorMessage, 'Cerrar', {
          duration: 4000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
      }
    });
  }

  isDeleting(prescriptionId: number): boolean {
    return this.deletingPrescriptionIds.has(prescriptionId);
  }

  getInitials(name: string): string {
    if (!name) return '';
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return 'No especificada';
    // Para evitar problemas de timezone, parseamos la fecha manualmente
    const parts = dateStr.split('-');
    if (parts.length === 3) {
      const year = parseInt(parts[0]);
      const month = parseInt(parts[1]) - 1; // Los meses en JS van de 0-11
      const day = parseInt(parts[2]);
      const date = new Date(year, month, day);
      return date.toLocaleDateString('es-ES');
    }
    return dateStr;
  }

  getMedicationsPreview(medications: string): string[] {
    if (!medications) return [];
    const lines = medications.split('\n').filter(line => line.trim());
    if (lines.length === 0) {
      return medications.split(',').map(m => m.trim()).filter(m => m).slice(0, 2);
    }
    return lines.slice(0, 2);
  }

  hasMoreMedications(medications: string): boolean {
    if (!medications) return false;
    const lines = medications.split('\n').filter(line => line.trim());
    return lines.length > 2;
  }

  getRemainingMedicationsCount(medications: string): number {
    if (!medications) return 0;
    const lines = medications.split('\n').filter(line => line.trim());
    return Math.max(0, lines.length - 2);
  }
}

