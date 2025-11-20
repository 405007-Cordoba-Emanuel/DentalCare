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
import { DentistService } from '../../../core/services/dentist.service';
import { PatientSummary } from '../../../features/dentists/interfaces/patient.interface';
import { LocalStorageService } from '../../../core/services/auth/local-storage.service';

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
    MatProgressSpinnerModule
  ],
  templateUrl: './dentist-dashboard.component.html',
  styleUrl: './dentist-dashboard.component.css'
})
export class DentistDashboardComponent implements OnInit {
  private router = inject(Router);
  private dentistService = inject(DentistService);
  private localStorage = inject(LocalStorageService);
  
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
    this.router.navigate(['/dentist/appointments/create'], { 
      queryParams: { patientId } 
    });
    console.log('Agendar cita para paciente:', patientId);
  }

  viewTreatments(patientId: number) {
    this.router.navigate([`/dentist/patients/${patientId}/treatments`]);
    console.log('Ver tratamientos del paciente:', patientId);
  }

  viewPrescriptions(patientId: number) {
    // TODO: Implementar ruta de recetas por paciente
    console.log('Ver recetas del paciente:', patientId);
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
    // TODO: Implementar ruta de historial clínico por paciente
    console.log('Ver historial clínico del paciente:', patientId);
  }
}
