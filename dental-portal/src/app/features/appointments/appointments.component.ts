import { Component, inject, OnInit } from '@angular/core';
import { CalendarOptions, EventInput } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { FullCalendarModule } from '@fullcalendar/angular';
import esLocale from '@fullcalendar/core/locales/es';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AppointmentService } from '../../core/services/appointment.service';
import { DentistService } from '../../core/services/dentist.service';
import { PatientService } from '../../core/services/patient.service';
import { LocalStorageService } from '../../core/services/auth/local-storage.service';
import { AppointmentResponse } from '../dentists/interfaces/appointment.interface';

@Component({
  selector: 'app-appointments',
  imports: [
    CommonModule,
    FullCalendarModule, 
    MatButtonModule, 
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './appointments.component.html',
  styles: [
    `
      /* 🌤️ Forzar tema claro en FullCalendar */
      :host ::ng-deep .fc {
        background-color: #ffffff !important;
        color: #1f2937 !important; /* text-gray-800 */
        border-radius: 1rem;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
        padding: 1rem;
      }

      /* Header (días, nombres, etc.) */
      :host ::ng-deep .fc-theme-standard th {
        background-color: #f3f4f6 !important; /* gray-100 */
        color: #374151 !important; /* gray-700 */
        border: 1px solid #e5e7eb !important; /* gray-200 */
        font-weight: 600;
      }

      /* Celdas */
      :host ::ng-deep .fc-theme-standard td {
        background-color: #ffffff !important;
        border: 1px solid #e5e7eb !important;
      }

      /* Día actual */
      :host ::ng-deep .fc-day-today {
        background-color: #dbeafe !important; /* blue-100 */
      }

      /* Eventos */
      :host ::ng-deep .fc-event {
        background-color: #2563eb !important; /* blue-600 */
        border: none !important;
        color: #fff !important;
        border-radius: 0.5rem !important;
        padding: 4px 6px !important;
        font-size: 0.875rem !important;
        transition: all 0.2s;
      }

      :host ::ng-deep .fc-event:hover {
        background-color: #1d4ed8 !important; /* blue-700 */
        transform: scale(1.02);
      }

      /* Sábado y domingo */
      :host ::ng-deep .fc-day-sat,
      :host ::ng-deep .fc-day-sun {
        background-color: #f9fafb !important;
      }
    `,
  ],
})
export class AppointmentsComponent implements OnInit {
  private router = inject(Router);
  private appointmentService = inject(AppointmentService);
  private dentistService = inject(DentistService);
  private patientService = inject(PatientService);
  private localStorage = inject(LocalStorageService);
  private snackBar = inject(MatSnackBar);

  currentView: 'dayGridMonth' | 'timeGridWeek' | 'timeGridDay' = 'dayGridMonth';
  isLoading = false;
  userRole: string | null = null;
  userId: number | null = null;
  dentistId: number | null = null;
  patientId: number | null = null;

  calendarOptions: CalendarOptions = {
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    initialView: this.currentView,
    headerToolbar: false,
    slotMinTime: '09:00:00',
    slotMaxTime: '20:00:00',
    slotLabelFormat: {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    },
    allDaySlot: false,
    locale: esLocale,
    events: [],
    eventClick: this.handleEventClick.bind(this),
  };

  ngOnInit() {
    this.loadUserData();
  }

  private loadUserData() {
    const userDataString = this.localStorage.getUserData();
    if (!userDataString) {
      this.snackBar.open('No se encontró información del usuario', 'Cerrar', {
        duration: 3000,
        panelClass: ['error-snackbar']
      });
      return;
    }

    try {
      const userData = JSON.parse(userDataString);
      this.userRole = userData.role;
      this.userId = parseInt(userData.id, 10);

      if (this.userRole === 'DENTIST') {
        this.loadDentistAppointments(userData);
      } else if (this.userRole === 'PATIENT') {
        this.loadPatientAppointments(userData);
      } else {
        this.snackBar.open('Rol de usuario no válido', 'Cerrar', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    } catch (error) {
      console.error('Error parsing user data:', error);
      this.snackBar.open('Error al cargar datos del usuario', 'Cerrar', {
        duration: 3000,
        panelClass: ['error-snackbar']
      });
    }
  }

  private loadDentistAppointments(userData: any) {
    this.dentistId = userData.dentistId;

    if (this.dentistId) {
      this.fetchAppointments();
    } else if (this.userId) {
      // Obtener dentistId desde el backend si no está en localStorage
      this.dentistService.getDentistIdByUserId(this.userId.toString()).subscribe({
        next: (dentistId) => {
          this.dentistId = dentistId;
          userData.dentistId = dentistId;
          this.localStorage.setUserData(userData);
          this.fetchAppointments();
        },
        error: (error) => {
          console.error('Error fetching dentistId:', error);
          this.snackBar.open('Error al obtener ID del dentista', 'Cerrar', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
      });
    }
  }

  private loadPatientAppointments(userData: any) {
    this.patientId = userData.patientId;

    if (this.patientId) {
      this.fetchAppointments();
    } else if (this.userId) {
      // Obtener patientId desde el backend si no está en localStorage
      this.patientService.getPatientIdByUserId(this.userId).subscribe({
        next: (patientId) => {
          this.patientId = patientId;
          userData.patientId = patientId;
          this.localStorage.setUserData(userData);
          this.fetchAppointments();
        },
        error: (error) => {
          console.error('Error fetching patientId:', error);
          this.snackBar.open('Error al obtener ID del paciente', 'Cerrar', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
      });
    } else {
      this.snackBar.open('No se encontró ID del paciente', 'Cerrar', {
        duration: 3000,
        panelClass: ['error-snackbar']
      });
    }
  }

  private fetchAppointments() {
    this.isLoading = true;

    const id = this.userRole === 'DENTIST' ? this.dentistId : this.patientId;
    const role = this.userRole || '';

    if (!id) {
      this.isLoading = false;
      return;
    }

    this.appointmentService.getAppointmentsByRoleAndId(role, id, false).subscribe({
      next: (appointments) => {
        const events = this.mapAppointmentsToEvents(appointments);
        this.calendarOptions.events = events;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error fetching appointments:', error);
        this.snackBar.open('Error al cargar las citas', 'Cerrar', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
        this.isLoading = false;
      }
    });
  }

  private mapAppointmentsToEvents(appointments: AppointmentResponse[]): EventInput[] {
    return appointments.map(appointment => {
      // Determinar el título según el rol del usuario
      let title = '';
      if (this.userRole === 'DENTIST') {
        title = `${appointment.patientName} - ${appointment.reason || 'Consulta'}`;
      } else {
        title = `${appointment.dentistName} - ${appointment.reason || 'Consulta'}`;
      }

      // Determinar el color según el estado
      let backgroundColor = '#2563eb'; // blue-600 por defecto
      let borderColor = '#2563eb';

      switch (appointment.status) {
        case 'PROGRAMADO':
          backgroundColor = '#3b82f6'; // blue-500
          borderColor = '#3b82f6';
          break;
        case 'CONFIRMADO':
          backgroundColor = '#10b981'; // green-500
          borderColor = '#10b981';
          break;
        case 'COMPLETADO':
          backgroundColor = '#6b7280'; // gray-500
          borderColor = '#6b7280';
          break;
        case 'AUSENTE':
          backgroundColor = '#f59e0b'; // amber-500
          borderColor = '#f59e0b';
          break;
      }

      return {
        id: appointment.id.toString(),
        title: title,
        start: appointment.startDateTime,
        end: appointment.endDateTime,
        backgroundColor: backgroundColor,
        borderColor: borderColor,
        extendedProps: {
          appointment: appointment
        }
      };
    });
  }

  handleEventClick(clickInfo: any) {
    const appointment = clickInfo.event.extendedProps.appointment;
    console.log('Appointment clicked:', appointment);
    // Aquí puedes abrir un modal o navegar a los detalles de la cita
  }

  changeView(
    view: 'dayGridMonth' | 'timeGridWeek' | 'timeGridDay',
    calendar: any
  ) {
    this.currentView = view;
    calendar.getApi().changeView(view);
  }

  navigateToCreateAppointment() {
    if (this.userRole === 'DENTIST') {
      this.router.navigate(['/dentist/appointments/create']);
    } else {
      this.snackBar.open('Solo los dentistas pueden crear citas', 'Cerrar', {
        duration: 3000,
        panelClass: ['error-snackbar']
      });
    }
  }
}
