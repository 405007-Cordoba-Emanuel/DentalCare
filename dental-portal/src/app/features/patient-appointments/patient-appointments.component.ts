import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { LocalStorageService } from '../../core/services/auth/local-storage.service';
import { AppointmentService, Appointment, AppointmentStatus } from '../../core/services/appointment.service';
import { User } from '../../interfaces/user/user.interface';
import { PatientService } from '../../core/services/patient.service';

@Component({
  selector: 'app-patient-appointments',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule
  ],
  templateUrl: './patient-appointments.component.html',
  styleUrl: './patient-appointments.component.css'
})
export class PatientAppointmentsComponent implements OnInit {
  private appointmentService = inject(AppointmentService);
  private localStorage = inject(LocalStorageService);
  private patientService = inject(PatientService);

  user: User | null = null;
  patientId: number | null = null;
  
  nextAppointment: Appointment | null = null;
  pastAppointments: Appointment[] = [];
  
  isLoading = false;
  errorMessage = '';

  ngOnInit() {
    this.loadData();
  }

  async loadData() {
    this.isLoading = true;
    this.errorMessage = '';

    try {
      // Obtener usuario del localStorage
      const userDataString = this.localStorage.getUserData();
      if (!userDataString) {
        this.errorMessage = 'No se encontró información del usuario';
        this.isLoading = false;
        return;
      }

      this.user = JSON.parse(userDataString);

      // Obtener patientId
      if (this.user?.patientId) {
        this.patientId = this.user.patientId;
      } else if (this.user?.id) {
        // Si no tiene patientId, obtenerlo del servicio
        // Convertir string a number
        const userId = parseInt(this.user.id, 10);
        if (isNaN(userId)) {
          this.errorMessage = 'ID de usuario inválido';
          this.isLoading = false;
          return;
        }
        this.patientService.getPatientIdByUserId(userId).subscribe({
          next: (id) => {
            this.patientId = id;
            this.loadAppointments();
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

      this.loadAppointments();
    } catch (error) {
      console.error('Error al cargar datos:', error);
      this.errorMessage = 'Error al cargar los datos';
      this.isLoading = false;
    }
  }

  loadAppointments() {
    if (!this.patientId) {
      this.isLoading = false;
      return;
    }

    let upcomingLoaded = false;
    let pastLoaded = false;

    const checkComplete = () => {
      if (upcomingLoaded && pastLoaded) {
        this.isLoading = false;
      }
    };

    // Cargar próxima cita
    this.appointmentService.getUpcomingAppointmentsByPatientId(this.patientId).subscribe({
      next: (appointments) => {
        this.nextAppointment = appointments.length > 0 ? appointments[0] : null;
        upcomingLoaded = true;
        checkComplete();
      },
      error: (error) => {
        console.error('Error al cargar próximas citas:', error);
        this.errorMessage = 'Error al cargar las próximas citas';
        upcomingLoaded = true;
        checkComplete();
      }
    });

    // Cargar historial de citas
    this.appointmentService.getPastAppointmentsByPatientId(this.patientId).subscribe({
      next: (appointments) => {
        this.pastAppointments = appointments;
        pastLoaded = true;
        checkComplete();
      },
      error: (error) => {
        console.error('Error al cargar historial de citas:', error);
        if (!this.errorMessage) {
          this.errorMessage = 'Error al cargar el historial de citas';
        }
        pastLoaded = true;
        checkComplete();
      }
    });
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    // Parsear manualmente para evitar problemas de timezone
    const parts = dateStr.split('T')[0].split('-');
    const timeParts = dateStr.split('T')[1]?.split(':') || [];
    const year = parseInt(parts[0]);
    const month = parseInt(parts[1]);
    const day = parseInt(parts[2]);
    const hours = timeParts.length > 0 ? parseInt(timeParts[0]) : 0;
    const minutes = timeParts.length > 1 ? parseInt(timeParts[1]) : 0;

    const months = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    const monthName = months[month - 1];

    return `${day} ${monthName} ${year} - ${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
  }

  formatShortDate(dateStr: string): string {
    if (!dateStr) return '';
    // Parsear manualmente para evitar problemas de timezone
    const parts = dateStr.split('T')[0].split('-');
    const year = parseInt(parts[0]);
    const month = parseInt(parts[1]);
    const day = parseInt(parts[2]);

    const months = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    const monthName = months[month - 1];

    return `${day} ${monthName} ${year}`;
  }

  formatDateOnly(dateStr: string): string {
    if (!dateStr) return '';
    // Parsear manualmente para evitar problemas de timezone
    const parts = dateStr.split('T')[0].split('-');
    const year = parseInt(parts[0]);
    const month = parseInt(parts[1]);
    const day = parseInt(parts[2]);

    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  }

  formatTime(dateStr: string): string {
    if (!dateStr) return '';
    const timeParts = dateStr.split('T')[1]?.split(':') || [];
    const hours = timeParts.length > 0 ? parseInt(timeParts[0]) : 0;
    const minutes = timeParts.length > 1 ? parseInt(timeParts[1]) : 0;

    return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
  }

  getStatusLabel(status: AppointmentStatus): string {
    const statusMap: { [key: string]: string } = {
      'PROGRAMADO': 'Programado',
      'CONFIRMADO': 'Confirmado',
      'COMPLETADO': 'Completada',
      'CANCELADO': 'Cancelada',
      'AUSENTE': 'Ausente'
    };
    return statusMap[status] || status;
  }

  getStatusColor(status: AppointmentStatus): string {
    // Retorna la clase CSS personalizada para cada estado
    const colorMap: { [key: string]: string } = {
      'PROGRAMADO': 'status-programado',
      'CONFIRMADO': 'status-confirmado',
      'COMPLETADO': 'status-completado',
      'CANCELADO': 'status-cancelado',
      'AUSENTE': 'status-ausente'
    };
    return colorMap[status] || 'status-programado';
  }
}

