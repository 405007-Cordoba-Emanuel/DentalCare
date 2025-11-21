import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { DentistService } from '../../../core/services/dentist.service';
import { PatientSummary } from '../../../features/dentists/interfaces/patient.interface';
import { LocalStorageService } from '../../../core/services/auth/local-storage.service';
import { AppointmentModalComponent } from './appointment-modal/appointment-modal.component';

@Component({
  selector: 'app-dentist-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatTooltipModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatDialogModule
  ],
  templateUrl: './dentist-dashboard.component.html',
  styleUrl: './dentist-dashboard.component.css'
})
export class DentistDashboardComponent implements OnInit {
  private router = inject(Router);
  private dentistService = inject(DentistService);
  private localStorage = inject(LocalStorageService);
  private dialog = inject(MatDialog);
  
  dentistId = this.localStorage.getDentistId();
  patients: PatientSummary[] = [];
  filteredPatients: PatientSummary[] = [];
  searchTerm: string = '';
  isLoading: boolean = false;

  // Estadísticas (KPIs)
  stats = {
    appointmentsToday: 3,
    unreadMessages: 2
  };

  ngOnInit() {
    if (this.dentistId) {
      this.loadPatients(this.dentistId);
    } else {
      // Fallback: Intentar obtener el dentistId usando el userId
      const userDataString = this.localStorage.getUserData();
      if (userDataString) {
        try {
          const userData = JSON.parse(userDataString);
          const userId = userData.id;
          
          if (userId) {
            this.dentistService.getDentistIdByUserId(userId).subscribe({
              next: (dentistId) => {
                this.dentistId = dentistId;
                
                // Actualizar localStorage con el dentistId
                userData.dentistId = dentistId;
                this.localStorage.setUserData(userData);
                
                // Cargar pacientes
                this.loadPatients(dentistId);
              },
              error: (error) => {
                console.error('Error fetching dentistId:', error);
              }
            });
          } else {
            console.error('No userId found in localStorage');
          }
        } catch (error) {
          console.error('Error parsing user data:', error);
        }
      }
    }
  }
  
  loadPatients(dentistId: number) {
    this.isLoading = true;
    
    this.dentistService.getActivePatientsByDentistId(dentistId).subscribe({
      next: (response) => {
        this.patients = response.patients;
        this.filteredPatients = [...this.patients];
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading patients:', error);
        this.isLoading = false;
      }
    });
  }

  filterPatients() {
    if (!this.searchTerm.trim()) {
      this.filteredPatients = [...this.patients];
      return;
    }

    const searchLower = this.searchTerm.toLowerCase().trim();
    this.filteredPatients = this.patients.filter(patient => {
      const fullName = `${patient.firstName} ${patient.lastName}`.toLowerCase();
      const dni = patient.dni.toString();
      
      return fullName.includes(searchLower) || dni.includes(searchLower);
    });
  }

  clearSearch() {
    this.searchTerm = '';
    this.filterPatients();
  }

  getInitials(firstName: string, lastName: string): string {
    const firstInitial = firstName?.charAt(0)?.toUpperCase() || '';
    const lastInitial = lastName?.charAt(0)?.toUpperCase() || '';
    return `${firstInitial}${lastInitial}`;
  }

  // Navegación a secciones principales
  navigateToAppointments() {
    this.router.navigate(['/dentist/appointments']);
  }

  navigateToMessages() {
    this.router.navigate(['/messages']);
  }

  // Acciones de pacientes
  scheduleAppointment(patientId: number) {
    const patient = this.patients.find(p => p.id === patientId);
    
    if (!patient || !this.dentistId) {
      console.error('Patient or dentist not found');
      return;
    }

    const dialogRef = this.dialog.open(AppointmentModalComponent, {
      width: '600px',
      maxWidth: '95vw',
      disableClose: false,
      data: {
        patientId: patientId,
        patientName: `${patient.firstName} ${patient.lastName}`,
        dentistId: this.dentistId
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        console.log('Cita creada exitosamente:', result);
        // Aquí podrías actualizar las estadísticas del dashboard si es necesario
      }
    });
  }

  viewTreatments(patientId: number) {
    this.router.navigate([`/dentist/${patientId}/treatments`]);
    console.log('Ver tratamientos del paciente:', patientId);
  }

  viewPrescriptions(patientId: number) {
    console.log('Navegando a recetas del paciente:', patientId);
    this.router.navigate(['/dentist/patients', patientId, 'prescriptions']).then(
      success => console.log('Navegación exitosa:', success),
      error => console.error('Error en navegación:', error)
    );
  }

  viewProgress(patientId: number) {
    // TODO: Implementar ruta de progreso por paciente
    console.log('Ver progreso del paciente:', patientId);
  }

  viewOdontogram(patientId: number) {
    console.log('Navegando a odontograma del paciente:', patientId);
    this.router.navigate(['/dentist/patients', patientId, 'odontogram']).then(
      success => console.log('Navegación exitosa:', success),
      error => console.error('Error en navegación:', error)
    );
  }

  viewClinicalHistory(patientId: number) {
    this.router.navigate(['/dentist/patients', patientId, 'clinical-history']).then(
      success => console.log('Navegación a historia clínica exitosa:', success),
      error => console.error('Error en navegación a historia clínica:', error)
    );
  }
}
