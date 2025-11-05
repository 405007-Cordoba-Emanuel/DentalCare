import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LocalStorageService } from '../../core/services/auth/local-storage.service';
import { User } from '../../interfaces/user/user.interface';
import { TreatmentService } from '../../core/services/treatment.service';
import { PatientService } from '../../core/services/patient.service';
import { TreatmentResponse } from '../dentists/interfaces/treatment.interface';

@Component({
  selector: 'app-patient-treatments',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
    MatChipsModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './patient-treatments.component.html',
  styleUrl: './patient-treatments.component.css'
})
export class PatientTreatmentsComponent implements OnInit {
  private localStorage = inject(LocalStorageService);
  private treatmentService = inject(TreatmentService);
  private patientService = inject(PatientService);

  user: User | null = null;
  patientId: number | null = null;
  treatments: TreatmentResponse[] = [];
  isLoading = true;
  errorMessage = '';

  ngOnInit() {
    this.loadUserDataAndPatientId();
  }

  private loadUserDataAndPatientId() {
    this.isLoading = true;
    const userDataString = this.localStorage.getUserData();
    if (!userDataString) {
      console.error('No hay usuario autenticado');
      this.errorMessage = 'No hay usuario autenticado';
      this.isLoading = false;
      return;
    }

    try {
      this.user = JSON.parse(userDataString);

      // Obtener patientId
      if (this.user?.patientId) {
        this.patientId = this.user.patientId;
      } else if (this.user?.id) {
        const userId = parseInt(this.user.id, 10);
        if (isNaN(userId)) {
          this.errorMessage = 'ID de usuario inválido';
          this.isLoading = false;
          return;
        }
        this.patientService.getPatientIdByUserId(userId).subscribe({
          next: (id) => {
            this.patientId = id;
            this.loadTreatments();
          },
          error: (error) => {
            console.error('Error al obtener patientId:', error);
            this.errorMessage = 'Error al obtener información del paciente';
            this.isLoading = false;
          }
        });
        return;
      } else {
        this.errorMessage = 'No se encontró información del paciente';
        this.isLoading = false;
        return;
      }

      this.loadTreatments();
    } catch (error) {
      console.error('Error al cargar datos:', error);
      this.errorMessage = 'Error al cargar los datos';
      this.isLoading = false;
    }
  }

  loadTreatments() {
    if (!this.patientId) {
      this.isLoading = false;
      return;
    }

    this.treatmentService.getTreatmentsByPatientId(this.patientId).subscribe({
      next: (treatments) => {
        this.treatments = treatments;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error al cargar tratamientos:', error);
        this.errorMessage = 'Error al cargar los tratamientos';
        this.isLoading = false;
      }
    });
  }

  formatDate(dateStr: string | null): string {
    if (!dateStr) return 'N/A';
    // Parsear manualmente para evitar problemas de timezone
    const parts = dateStr.split('T')[0].split('-');
    const year = parseInt(parts[0]);
    const month = parseInt(parts[1]) - 1;
    const day = parseInt(parts[2]);
    const date = new Date(year, month, day);
    return date.toLocaleDateString('es-ES', { year: 'numeric', month: 'long', day: 'numeric' });
  }

  getProgressColor(percentage: number): string {
    if (percentage >= 80) return 'primary';
    if (percentage >= 50) return 'accent';
    return 'warn';
  }

  getProgressLabel(percentage: number): string {
    if (percentage === 100) return 'Completado';
    if (percentage >= 75) return 'Casi completado';
    if (percentage >= 50) return 'En progreso';
    if (percentage >= 25) return 'Iniciado';
    return 'Recién comenzado';
  }

  getStatusColor(status: string): string {
    const statusUpper = status?.toUpperCase() || '';
    if (statusUpper === 'COMPLETADO') return 'status-completado';
    if (statusUpper === 'EN_CURSO' || statusUpper === 'EN CURSO') return 'status-en-curso';
    if (statusUpper === 'ABANDONADO') return 'status-abandonado';
    return 'status-default';
  }

  getStatusLabel(status: string): string {
    const statusUpper = status?.toUpperCase() || '';
    if (statusUpper === 'COMPLETADO') return 'Completado';
    if (statusUpper === 'EN_CURSO' || statusUpper === 'EN CURSO') return 'En Curso';
    if (statusUpper === 'ABANDONADO') return 'Abandonado';
    return status || 'Sin estado';
  }
}
