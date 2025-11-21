import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatBadgeModule } from '@angular/material/badge';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { BadgeComponent } from '../../../shared/badge/badge.component';
import { AppointmentService, Appointment, AppointmentStatus } from '../../../core/services/appointment.service';
import { TreatmentService } from '../../../core/services/treatment.service';
import { PatientService, Patient } from '../../../core/services/patient.service';
import { LocalStorageService } from '../../../core/services/auth/local-storage.service';
import { User } from '../../../interfaces/user/user.interface';
import { TreatmentResponse } from '../../../features/dentists/interfaces/treatment.interface';

interface AppointmentDisplay {
  id: number;
  date: string;
  time: string;
  type: string;
  dentist: string;
  status: string;
}

interface TreatmentDisplay {
  id: number;
  date: string;
  procedure: string;
  dentist: string;
  cost: number;
  status: string;
}

interface PatientInfo {
  name: string;
  age: number;
  lastVisit: string;
  nextAppointment: string;
  treatmentProgress: number;
}

@Component({
  selector: 'app-patient-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatTableModule,
    MatBadgeModule,
    MatChipsModule,
    MatDividerModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    BadgeComponent,
  ],
  templateUrl: './patient-dashboard.component.html'
})
export class PatientDashboardComponent implements OnInit {
  private router = inject(Router);
  private appointmentService = inject(AppointmentService);
  private treatmentService = inject(TreatmentService);
  private patientService = inject(PatientService);
  private localStorage = inject(LocalStorageService);

  user: User | null = null;
  patientId: number | null = null;
  patient: Patient | null = null;
  
  isLoading = true;
  errorMessage = '';

  patientInfo: PatientInfo = {
    name: '',
    age: 0,
    lastVisit: '',
    nextAppointment: '',
    treatmentProgress: 0
  };

  upcomingAppointments: AppointmentDisplay[] = [];
  treatmentHistory: TreatmentDisplay[] = [];
  currentTreatment: TreatmentResponse | null = null;

  recentMessages = [
    {
      from: 'Sistema',
      message: 'Ejemplo mockeado',
      time: 'Hace 1 día',
      unread: false
    }
  ];

  unreadMessagesCount = 0; // Por ahora mockeado, se puede conectar con un servicio de mensajes
  appointmentsDropdownOpen = false;

  ngOnInit() {
    this.loadData();
  }

  private loadData() {
    this.isLoading = true;
    this.errorMessage = '';

    // Obtener usuario del localStorage
    const userDataString = this.localStorage.getUserData();
    if (!userDataString) {
      this.errorMessage = 'No se encontró información del usuario';
      this.isLoading = false;
      return;
    }

    try {
      this.user = JSON.parse(userDataString);

      // Obtener patientId
      if (this.user?.patientId) {
        this.patientId = this.user.patientId;
        this.loadAllData();
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
            if (this.user) {
              this.user.patientId = id;
              this.localStorage.setUserData(this.user);
            }
            this.loadAllData();
          },
          error: (error) => {
            console.error('Error al obtener patientId:', error);
            this.errorMessage = 'Error al obtener información del paciente';
            this.isLoading = false;
          }
        });
      } else {
        this.errorMessage = 'No se encontró información del paciente';
        this.isLoading = false;
      }
    } catch (error) {
      console.error('Error al cargar datos:', error);
      this.errorMessage = 'Error al cargar los datos';
      this.isLoading = false;
    }
  }

  private loadAllData() {
    if (!this.patientId) {
      this.isLoading = false;
      return;
    }

    let patientLoaded = false;
    let appointmentsLoaded = false;
    let pastAppointmentsLoaded = false;
    let treatmentsLoaded = false;

    const checkComplete = () => {
      if (patientLoaded && appointmentsLoaded && pastAppointmentsLoaded && treatmentsLoaded) {
        this.isLoading = false;
      }
    };

    // Cargar información del paciente
    this.patientService.getPatientById(this.patientId).subscribe({
      next: (patient) => {
        this.patient = patient;
        this.updatePatientInfo();
        patientLoaded = true;
        checkComplete();
      },
      error: (error) => {
        console.error('Error al cargar información del paciente:', error);
        patientLoaded = true;
        checkComplete();
      }
    });

    // Cargar próximas citas
    this.appointmentService.getUpcomingAppointmentsByPatientId(this.patientId).subscribe({
      next: (appointments) => {
        this.upcomingAppointments = appointments.map(app => this.mapAppointmentToDisplay(app));
        this.updateNextAppointment();
        appointmentsLoaded = true;
        checkComplete();
      },
      error: (error) => {
        console.error('Error al cargar próximas citas:', error);
        appointmentsLoaded = true;
        checkComplete();
      }
    });

    // Cargar citas pasadas para obtener la última visita
    this.appointmentService.getPastAppointmentsByPatientId(this.patientId).subscribe({
      next: (appointments) => {
        if (appointments.length > 0) {
          // Ordenar por fecha descendente y tomar la más reciente
          const sorted = appointments.sort((a, b) => 
            new Date(b.startDateTime || '').getTime() - new Date(a.startDateTime || '').getTime()
          );
          const lastAppointment = sorted[0];
          this.patientInfo.lastVisit = lastAppointment.startDateTime?.split('T')[0] || '';
        }
        pastAppointmentsLoaded = true;
        checkComplete();
      },
      error: (error) => {
        console.error('Error al cargar citas pasadas:', error);
        pastAppointmentsLoaded = true;
        checkComplete();
      }
    });

    // Cargar historial de tratamientos
    this.treatmentService.getTreatmentsByPatientId(this.patientId).subscribe({
      next: (treatments) => {
        this.treatmentHistory = treatments.map(treatment => this.mapTreatmentToDisplay(treatment));
        this.updateTreatmentProgress(treatments);
        // Obtener el tratamiento en curso más reciente
        this.currentTreatment = this.getCurrentTreatment(treatments);
        treatmentsLoaded = true;
        checkComplete();
      },
      error: (error) => {
        console.error('Error al cargar tratamientos:', error);
        treatmentsLoaded = true;
        checkComplete();
      }
    });
  }

  private mapAppointmentToDisplay(appointment: Appointment): AppointmentDisplay {
    const time = this.formatTime(appointment.startDateTime || '00:00');
    const dateStr = appointment.startDateTime?.split('T')[0] || '';
    
    return {
      id: appointment.id,
      date: dateStr,
      time: time,
      type: appointment.reason || 'Consulta',
      dentist: appointment.dentistName || 'Sin asignar',
      status: this.getStatusLabel(appointment.status)
    };
  }

  private mapTreatmentToDisplay(treatment: TreatmentResponse): TreatmentDisplay {
    const date = treatment.startDate ? treatment.startDate.split('T')[0] : '';
    
    return {
      id: treatment.id,
      date: date,
      procedure: treatment.name || 'Tratamiento',
      dentist: treatment.dentistName || 'Sin asignar',
      cost: 0, // La API no devuelve costo, se puede calcular o dejar en 0
      status: this.getTreatmentStatusLabel(treatment.status)
    };
  }

  private updatePatientInfo() {
    if (this.user) {
      const fullName = `${this.user.firstName} ${this.user.lastName}`;
      const age = this.user.birthDate ? this.calculateAge(this.user.birthDate) : 0;
      
      this.patientInfo.name = fullName;
      this.patientInfo.age = age;
    }
  }

  private updateNextAppointment() {
    if (this.upcomingAppointments.length > 0) {
      const nextAppointment = this.upcomingAppointments[0];
      this.patientInfo.nextAppointment = nextAppointment.date;
    } else {
      this.patientInfo.nextAppointment = '';
    }
  }

  private updateTreatmentProgress(treatments: TreatmentResponse[]) {
    if (treatments.length === 0) {
      this.patientInfo.treatmentProgress = 0;
      return;
    }

    // Calcular el progreso promedio de todos los tratamientos activos
    const activeTreatments = treatments.filter(t => t.active && t.status !== 'COMPLETADO');
    if (activeTreatments.length === 0) {
      // Si no hay tratamientos activos, usar el promedio de todos
      const totalProgress = treatments.reduce((sum, t) => sum + (t.progressPercentage || 0), 0);
      this.patientInfo.treatmentProgress = Math.round(totalProgress / treatments.length);
    } else {
      const totalProgress = activeTreatments.reduce((sum, t) => sum + (t.progressPercentage || 0), 0);
      this.patientInfo.treatmentProgress = Math.round(totalProgress / activeTreatments.length);
    }
  }

  private calculateAge(birthDate: string): number {
    const birth = new Date(birthDate);
    const today = new Date();
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
      age--;
    }
    return age;
  }

  private formatTime(dateStr: string): string {
    if (!dateStr) return '';
    const timeParts = dateStr.split('T')[1]?.split(':') || [];
    const hours = timeParts.length > 0 ? parseInt(timeParts[0]) : 0;
    const minutes = timeParts.length > 1 ? parseInt(timeParts[1]) : 0;
    const period = hours >= 12 ? 'PM' : 'AM';
    const displayHours = hours > 12 ? hours - 12 : (hours === 0 ? 12 : hours);
    return `${String(displayHours).padStart(2, '0')}:${String(minutes).padStart(2, '0')} ${period}`;
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const parts = dateStr.split('T')[0].split('-');
    const year = parseInt(parts[0]);
    const month = parseInt(parts[1]) - 1;
    const day = parseInt(parts[2]);
    const date = new Date(year, month, day);
    return date.toLocaleDateString('es-ES', { year: 'numeric', month: 'long', day: 'numeric' });
  }

  formatDay(dateStr: string): string {
    if (!dateStr) return '';
    const parts = dateStr.split('T')[0].split('-');
    const day = parseInt(parts[2]);
    return String(day);
  }

  formatMonth(dateStr: string): string {
    if (!dateStr) return '';
    const parts = dateStr.split('T')[0].split('-');
    const month = parseInt(parts[1]) - 1;
    const months = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    return months[month] || '';
  }

  formatShortDate(dateStr: string): string {
    if (!dateStr) return '';
    const parts = dateStr.split('T')[0].split('-');
    const year = parseInt(parts[0]);
    const month = parseInt(parts[1]);
    const day = parseInt(parts[2]);
    const months = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    const monthName = months[month - 1] || '';
    return `${day} ${monthName} ${year}`;
  }

  private getStatusLabel(status: string | AppointmentStatus): string {
    const statusMap: { [key: string]: string } = {
      'PROGRAMADO': 'Programado',
      'CONFIRMADO': 'Confirmada',
      'COMPLETADO': 'Completada',
      'CANCELADO': 'Cancelada',
      'AUSENTE': 'Ausente'
    };
    return statusMap[status] || status;
  }

  getTreatmentStatusLabel(status: string): string {
    const statusUpper = status?.toUpperCase() || '';
    if (statusUpper === 'COMPLETADO') return 'Completado';
    if (statusUpper === 'EN_CURSO' || statusUpper === 'EN CURSO') return 'En Curso';
    if (statusUpper === 'ABANDONADO') return 'Abandonado';
    if (statusUpper === 'PROGRAMADO') return 'Programado';
    return status || 'Sin estado';
  }

  scheduleAppointment() {
    // Lógica para agendar cita
    console.log('Agendar cita');
  }

  viewTreatmentDetails(treatmentId: number) {
    // Lógica para ver detalles del tratamiento
    this.router.navigate(['/patient/treatments']);
  }

  sendMessage() {
    // Lógica para enviar mensaje
    console.log('Enviar mensaje');
  }

  logout() {
    this.router.navigate(['/']);
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'Completado':
        return 'primary';
      case 'Pendiente':
        return 'warn';
      case 'Programado':
        return 'accent';
      default:
        return 'primary';
    }
  }

  getBadgeVariant(status: string): 'ausente' | 'en-curso' | 'completado' | 'abandonado' | 'default' {
    switch (status) {
      case 'Confirmada':
      case 'Programado':
      case 'En Curso':
        return 'en-curso';
      case 'Completado':
      case 'Completada':
        return 'completado';
      case 'Pendiente':
        return 'default';
      case 'Ausente':
        return 'ausente';
      case 'Cancelada':
      case 'Abandonado':
        return 'abandonado';
      default:
        return 'default';
    }
  }

  getProgressColor(percentage: number): string {
    if (percentage >= 80) return 'primary';
    if (percentage >= 50) return 'accent';
    return 'warn';
  }

  getLastTreatmentInProgress(): TreatmentDisplay | null {
    const inProgressTreatments = this.treatmentHistory.filter(
      treatment => treatment.status === 'En Curso' || treatment.status === 'Programado'
    );
    if (inProgressTreatments.length === 0) {
      return null;
    }
    // Ordenar por fecha descendente y tomar el más reciente
    return inProgressTreatments.sort((a, b) => 
      new Date(b.date).getTime() - new Date(a.date).getTime()
    )[0];
  }

  toggleAppointmentsDropdown(): void {
    this.appointmentsDropdownOpen = !this.appointmentsDropdownOpen;
  }

  getCurrentTreatment(treatments: TreatmentResponse[]): TreatmentResponse | null {
    const inProgressTreatments = treatments.filter(
      t => t.active && (t.status === 'EN_CURSO' || t.status === 'EN CURSO')
    );
    if (inProgressTreatments.length === 0) {
      return null;
    }
    // Ordenar por fecha descendente y tomar el más reciente
    return inProgressTreatments.sort((a, b) => 
      new Date(b.startDate).getTime() - new Date(a.startDate).getTime()
    )[0];
  }

  navigateToMessages() {
    // TODO: Navegar a mensajes cuando esté implementado
    console.log('Navegar a mensajes');
  }

  navigateToTreatments() {
    this.router.navigate(['/patient/treatments']);
  }

  navigateToPrescriptions() {
    this.router.navigate(['/patient/prescriptions']);
  }
}
