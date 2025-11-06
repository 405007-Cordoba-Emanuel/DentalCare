import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { LocalStorageService } from '../../core/services/auth/local-storage.service';
import { PrescriptionService, Prescription } from '../../core/services/prescription.service';
import { PatientService } from '../../core/services/patient.service';
import { User } from '../../interfaces/user/user.interface';
import { PrescriptionDetailDialogComponent } from './prescription-detail-dialog.component';

@Component({
  selector: 'app-prescription-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatDialogModule
  ],
  templateUrl: './prescription-list.component.html',
  styleUrl: './prescription-list.component.css'
})
export class PrescriptionListComponent implements OnInit {
  private prescriptionService = inject(PrescriptionService);
  private localStorage = inject(LocalStorageService);
  private patientService = inject(PatientService);
  private dialog = inject(MatDialog);

  user: User | null = null;
  prescriptions: Prescription[] = [];
  filteredPrescriptions: Prescription[] = [];
  isLoading = false;
  searchTerm = '';
  totalPrescriptions = 0;
  thisMonthPrescriptions = 0;

  ngOnInit() {
    this.loadData();
  }

  private loadData() {
    this.isLoading = true;

    // Obtener usuario desde localStorage
    const userStr = this.localStorage.getUserData();
    if (!userStr) {
      console.error('No hay usuario autenticado');
      this.isLoading = false;
      return;
    }

    this.user = JSON.parse(userStr);

    // Si no tiene patientId, obtenerlo usando userId
    if (!this.user?.patientId && this.user?.id) {
      const userId = parseInt(this.user.id, 10);
      if (!isNaN(userId)) {
        this.patientService.getPatientIdByUserId(userId).subscribe({
          next: (patientId) => {
            this.user!.patientId = patientId;
            // Actualizar en localStorage
            this.localStorage.setUserData(this.user!);
            this.loadPrescriptions();
          },
          error: (error) => {
            console.error('Error al obtener patientId:', error);
            this.isLoading = false;
          }
        });
        return;
      }
    }

    // Si ya tiene patientId, cargar directamente
    if (this.user?.patientId) {
      this.loadPrescriptions();
    } else {
      console.error('No se pudo obtener patientId');
      this.isLoading = false;
    }
  }

  private loadPrescriptions() {
    if (!this.user?.patientId) {
      this.isLoading = false;
      return;
    }

    this.prescriptionService.getPrescriptionsByPatientId(this.user.patientId).subscribe({
      next: (data) => {
        this.prescriptions = data;
        this.filteredPrescriptions = data;
        this.calculateKPIs();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error al cargar recetas:', error);
        this.isLoading = false;
      }
    });

    // Obtener conteo de recetas
    this.prescriptionService.getPrescriptionCount(this.user.patientId).subscribe({
      next: (count) => {
        this.totalPrescriptions = count;
      },
      error: (error) => {
        console.error('Error al obtener conteo de recetas:', error);
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

  openPrescriptionDetail(prescription: Prescription) {
    if (!this.user?.patientId) return;

    this.dialog.open(PrescriptionDetailDialogComponent, {
      width: '600px',
      maxWidth: '90vw',
      data: prescription
    });
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
    const date = new Date(dateStr);
    return date.toLocaleDateString('es-ES');
  }

  getMedicationsPreview(medications: string): string[] {
    if (!medications) return [];
    // Dividir por líneas o comas
    const lines = medications.split('\n').filter(line => line.trim());
    if (lines.length === 0) {
      // Intentar por comas
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

