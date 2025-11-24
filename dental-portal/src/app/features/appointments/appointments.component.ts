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
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AppointmentService } from '../../core/services/appointment.service';
import { DentistService } from '../../core/services/dentist.service';
import { PatientService } from '../../core/services/patient.service';
import { LocalStorageService } from '../../core/services/auth/local-storage.service';
import { AppointmentResponse } from '../dentists/interfaces/appointment.interface';
import { AppointmentDetailDialogComponent } from './appointment-detail-dialog/appointment-detail-dialog.component';

@Component({
  selector: 'app-appointments',
  imports: [
    CommonModule,
    FullCalendarModule, 
    MatButtonModule, 
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule
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
  private dialog = inject(MatDialog);

  currentView: 'dayGridMonth' | 'timeGridWeek' | 'timeGridDay' = 'dayGridMonth';
  isLoading = false;
  userRole: string | null = null;
  userId: string | null = null;
  dentistId: number | null = null;
  patientId: number | null = null;
  currentDate: Date = new Date();
  currentDateLabel: string = '';
  navigationLabel: string = 'período';
  allAppointments: any[] = []; // Almacena todas las citas cargadas una sola vez

  calendarOptions: CalendarOptions = {
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    initialView: 'dayGridMonth',
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
    height: 'auto',
    expandRows: true,
  };

  ngOnInit() {
    this.updateDateLabel();
    this.updateNavigationLabel();
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
      this.userId = userData.id;

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
      this.patientService.getPatientIdByUserId(this.userId.toString()).subscribe({
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

  private fetchAppointments(forceReload: boolean = false) {
    const id = this.userRole === 'DENTIST' ? this.dentistId : this.patientId;
    const role = this.userRole || '';

    if (!id) {
      this.isLoading = false;
      return;
    }

    // Si ya tenemos las citas cargadas y no es una recarga forzada, no hacemos nada
    // FullCalendar se encargará de filtrar las citas según la vista actual
    if (this.allAppointments.length > 0 && !forceReload) {
      return;
    }

    // Cargar todas las citas de 2 años (1 año atrás + 1 año adelante) UNA SOLA VEZ
    this.isLoading = true;
    
    this.appointmentService.getTwoYearAppointments(role, id).subscribe({
      next: (appointments) => {
        // Guardar todas las citas
        this.allAppointments = appointments;
        
        // Mapear a eventos de FullCalendar
        const events = this.mapAppointmentsToEvents(appointments);
        
        // Asignar al calendario (FullCalendar filtrará automáticamente según la vista)
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

  private getWeekStartDate(date: Date): Date {
    const d = new Date(date);
    const day = d.getDay();
    const diff = d.getDate() - day + (day === 0 ? -6 : 1); // Ajustar al lunes
    return new Date(d.setDate(diff));
  }

  private mapAppointmentsToEvents(appointments: any[]): EventInput[] {
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

      // Manejar ambos formatos de fecha/hora
      let startDateTime: string;
      let endDateTime: string;

      if (appointment.startDateTime && appointment.endDateTime) {
        // Formato del endpoint general (startDateTime y endDateTime combinados)
        startDateTime = appointment.startDateTime;
        endDateTime = appointment.endDateTime;
      } else if (appointment.date && appointment.startTime && appointment.endTime) {
        // Formato del endpoint de calendario mensual (date, startTime, endTime separados)
        startDateTime = `${appointment.date}T${appointment.startTime}`;
        endDateTime = `${appointment.date}T${appointment.endTime}`;
      } else {
        console.error('Invalid appointment format:', appointment);
        return null;
      }

      return {
        id: appointment.id.toString(),
        title: title,
        start: startDateTime,
        end: endDateTime,
        backgroundColor: backgroundColor,
        borderColor: borderColor,
        extendedProps: {
          appointment: appointment
        }
      };
    }).filter(event => event !== null) as EventInput[];
  }

  handleEventClick(clickInfo: any) {
    const appointment = clickInfo.event.extendedProps.appointment;
    console.log('Appointment clicked:', appointment);
    
    // Abrir el diálogo con los detalles de la cita
    const dialogRef = this.dialog.open(AppointmentDetailDialogComponent, {
      width: '700px',
      maxWidth: '100vw',
      data: {
        appointment: appointment,
        dentistId: this.dentistId,
        userRole: this.userRole
      },
      panelClass: 'appointment-detail-dialog',
      autoFocus: false
    });

    // Cuando se cierra el diálogo, recargar las citas si se actualizaron
    dialogRef.afterClosed().subscribe(result => {
      if (result && result.updated) {
        // Recargar las citas del calendario
        this.fetchAppointments(true);
      }
    });
  }

  changeView(
    view: 'dayGridMonth' | 'timeGridWeek' | 'timeGridDay',
    calendar: any
  ) {
    try {
      // Actualizar la vista actual
      this.currentView = view;
      
      // Obtener la API del calendario
      const calendarApi = calendar?.getApi();
      
      if (!calendarApi) {
        console.error('No se pudo obtener la API del calendario');
        return;
      }
      
      // Cambiar la vista del calendario
      calendarApi.changeView(view);
      
      // Obtener la fecha actual del calendario después del cambio de vista
      this.currentDate = calendarApi.getDate();
      
      // Actualizar los labels de navegación
      this.updateNavigationLabel();
      this.updateDateLabel();
      
      // No es necesario recargar las citas, FullCalendar filtra automáticamente
    } catch (error) {
      console.error('Error al cambiar de vista:', error);
    }
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

  // Navegación del calendario
  previous(calendar: any) {
    const calendarApi = calendar.getApi();
    calendarApi.prev();
    this.currentDate = calendarApi.getDate();
    this.updateDateLabel();
    // No es necesario recargar, FullCalendar filtra automáticamente
  }

  next(calendar: any) {
    const calendarApi = calendar.getApi();
    calendarApi.next();
    this.currentDate = calendarApi.getDate();
    this.updateDateLabel();
    // No es necesario recargar, FullCalendar filtra automáticamente
  }

  goToToday(calendar: any) {
    const calendarApi = calendar.getApi();
    calendarApi.today();
    this.currentDate = calendarApi.getDate();
    this.updateDateLabel();
    // No es necesario recargar, FullCalendar filtra automáticamente
  }

  private updateDateLabel() {
    const monthNames = [
      'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
      'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
    ];
    const dayNames = [
      'Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'
    ];

    const month = monthNames[this.currentDate.getMonth()];
    const year = this.currentDate.getFullYear();

    if (this.currentView === 'dayGridMonth') {
      // Vista mensual: "Noviembre 2025"
      this.currentDateLabel = `${month} ${year}`;
    } else if (this.currentView === 'timeGridWeek') {
      // Vista semanal: "Semana del 18 al 24 de Noviembre 2025"
      const startDate = this.getWeekStartDate(this.currentDate);
      const endDate = new Date(startDate);
      endDate.setDate(startDate.getDate() + 6);
      
      const startDay = startDate.getDate();
      const endDay = endDate.getDate();
      const startMonth = monthNames[startDate.getMonth()];
      const endMonth = monthNames[endDate.getMonth()];
      
      if (startDate.getMonth() === endDate.getMonth()) {
        this.currentDateLabel = `Semana del ${startDay} al ${endDay} de ${startMonth} ${year}`;
      } else {
        this.currentDateLabel = `Semana del ${startDay} de ${startMonth} al ${endDay} de ${endMonth} ${year}`;
      }
    } else if (this.currentView === 'timeGridDay') {
      // Vista diaria: "Lunes 18 de Noviembre 2025"
      const day = dayNames[this.currentDate.getDay()];
      const date = this.currentDate.getDate();
      this.currentDateLabel = `${day} ${date} de ${month} ${year}`;
    }
  }

  private updateNavigationLabel() {
    if (this.currentView === 'dayGridMonth') {
      this.navigationLabel = 'mes';
    } else if (this.currentView === 'timeGridWeek') {
      this.navigationLabel = 'semana';
    } else if (this.currentView === 'timeGridDay') {
      this.navigationLabel = 'día';
    }
  }
}
