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
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { LocalStorageService } from '../../../../core/services/auth/local-storage.service';
import { TreatmentService } from '../../services/treatment.service';
import { DentistService } from '../../../../core/services/dentist.service';
import { User } from '../../../../interfaces/user/user.interface';
import { TreatmentResponse, TreatmentDetailResponse } from '../../interfaces/treatment.interface';
import { TreatmentFormDialogComponent } from './treatment-form-dialog.component';
import { TreatmentSessionDialogComponent } from './treatment-session-dialog.component';
import { TreatmentDetailDialogComponent } from './treatment-detail-dialog.component';
import { ConfirmDeleteDialogComponent, ConfirmDeleteData } from '../prescriptions/confirm-delete-dialog.component';

@Component({
  selector: 'app-dentist-treatment-list',
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
    MatProgressSpinnerModule,
    MatProgressBarModule
  ],
  templateUrl: './dentist-treatment-list.component.html',
  styleUrl: './dentist-treatment-list.component.css'
})
export class DentistTreatmentListComponent implements OnInit {
  private treatmentService = inject(TreatmentService);
  private localStorage = inject(LocalStorageService);
  private dentistService = inject(DentistService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  user: User | null = null;
  patientId: number | null = null;
  dentistId: number | null = null;
  treatments: TreatmentResponse[] = [];
  filteredTreatments: TreatmentResponse[] = [];
  isLoading = false;
  searchTerm = '';
  deletingTreatmentIds = new Set<number>();

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

    if (this.user?.id) {
      const userId = parseInt(this.user.id, 10);
      if (!isNaN(userId)) {
        this.dentistService.getDentistIdByUserId(userId.toString()).subscribe({
          next: (dentistId) => {
            this.dentistId = dentistId;
            this.loadTreatments();
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

  private loadTreatments() {
    if (!this.dentistId || !this.patientId) {
      this.isLoading = false;
      return;
    }

    this.treatmentService.getTreatmentsByPatient(this.dentistId, this.patientId).subscribe({
      next: (data) => {
        this.treatments = data;
        this.filteredTreatments = data;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error al cargar tratamientos:', error);
        this.isLoading = false;
        this.snackBar.open('Error al cargar tratamientos', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
      }
    });
  }

  onSearchChange() {
    if (!this.searchTerm.trim()) {
      this.filteredTreatments = this.treatments;
      return;
    }

    const search = this.searchTerm.toLowerCase().trim();
    this.filteredTreatments = this.treatments.filter(treatment =>
      treatment.name?.toLowerCase().includes(search) ||
      treatment.description?.toLowerCase().includes(search) ||
      treatment.status?.toLowerCase().includes(search)
    );
  }

  openCreateTreatmentDialog() {
    if (!this.dentistId || !this.patientId) return;

    const dialogRef = this.dialog.open(TreatmentFormDialogComponent, {
      width: '600px',
      maxWidth: '90vw',
      data: {
        dentistId: this.dentistId,
        patientId: this.patientId,
        patientName: `${this.patientInfo.firstName} ${this.patientInfo.lastName}`,
        treatment: null
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadTreatments();
      }
    });
  }

  openEditTreatmentDialog(treatment: TreatmentResponse) {
    if (!this.dentistId) return;

    const dialogRef = this.dialog.open(TreatmentFormDialogComponent, {
      width: '600px',
      maxWidth: '90vw',
      data: {
        dentistId: this.dentistId,
        patientId: treatment.patientId,
        patientName: treatment.patientName,
        treatment: treatment
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadTreatments();
      }
    });
  }

  openAddSessionDialog(treatment: TreatmentResponse) {
    if (!this.dentistId || !this.patientId) return;

    const dialogRef = this.dialog.open(TreatmentSessionDialogComponent, {
      width: '600px',
      maxWidth: '90vw',
      data: {
        dentistId: this.dentistId,
        patientId: this.patientId,
        treatmentId: treatment.id,
        treatmentName: treatment.name
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadTreatments();
      }
    });
  }

  openTreatmentDetailDialog(treatment: TreatmentResponse) {
    if (!this.dentistId) return;

    this.treatmentService.getTreatmentDetail(this.dentistId, treatment.id).subscribe({
      next: (detail) => {
        const dialogRef = this.dialog.open(TreatmentDetailDialogComponent, {
          width: '900px',
          maxWidth: '95vw',
          data: {
            treatment: detail,
            dentistId: this.dentistId,
            patientId: this.patientId
          }
        });

        dialogRef.afterClosed().subscribe(result => {
          if (result) {
            this.loadTreatments();
          }
        });
      },
      error: (error) => {
        console.error('Error al cargar detalle del tratamiento:', error);
        this.snackBar.open('Error al cargar el detalle del tratamiento', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
      }
    });
  }

  confirmDeleteTreatment(treatment: TreatmentResponse) {
    const dialogRef = this.dialog.open(ConfirmDeleteDialogComponent, {
      width: '500px',
      maxWidth: '90vw',
      data: {
        title: 'Eliminar Tratamiento',
        message: '¿Está seguro de eliminar este tratamiento?',
        itemName: 'Tratamiento',
        itemDetails: {
          name: treatment.name,
          status: this.getStatusLabel(treatment.status)
        }
      } as ConfirmDeleteData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.dentistId) {
        this.deleteTreatment(treatment.id);
      }
    });
  }

  private deleteTreatment(treatmentId: number) {
    if (!this.dentistId) return;

    this.deletingTreatmentIds.add(treatmentId);
    this.treatmentService.deleteTreatment(this.dentistId, treatmentId).subscribe({
      next: () => {
        this.snackBar.open('Tratamiento eliminado exitosamente', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.loadTreatments();
      },
      error: (error) => {
        console.error('Error al eliminar tratamiento:', error);
        this.snackBar.open('Error al eliminar el tratamiento', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.deletingTreatmentIds.delete(treatmentId);
      }
    });
  }

  isDeleting(treatmentId: number): boolean {
    return this.deletingTreatmentIds.has(treatmentId);
  }

  formatDate(dateStr: string | null): string {
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

  getProgressPercentage(treatment: TreatmentResponse): number {
    if (!treatment.totalSessions || treatment.totalSessions === 0) return 0;
    return Math.round((treatment.completedSessions / treatment.totalSessions) * 100);
  }

  getProgressColor(percentage: number): string {
    if (percentage >= 80) return 'primary';
    if (percentage >= 50) return 'accent';
    return 'warn';
  }

  getStatusLabel(status: string): string {
    const statusUpper = status?.toUpperCase() || '';
    if (statusUpper === 'COMPLETADO') return 'Completado';
    if (statusUpper === 'EN_CURSO' || statusUpper === 'EN CURSO') return 'En Curso';
    if (statusUpper === 'ABANDONADO') return 'Abandonado';
    return status || 'Sin estado';
  }

  getStatusColor(status: string): string {
    const statusUpper = status?.toUpperCase() || '';
    if (statusUpper === 'COMPLETADO') return 'status-completed';
    if (statusUpper === 'EN_CURSO' || statusUpper === 'EN CURSO') return 'status-in-progress';
    if (statusUpper === 'ABANDONADO') return 'status-abandoned';
    return 'status-default';
  }
}

