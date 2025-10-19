import { Component, inject } from '@angular/core';
import { CalendarOptions } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { FullCalendarModule } from '@fullcalendar/angular';
import esLocale from '@fullcalendar/core/locales/es';

@Component({
  selector: 'app-appointments',
  imports: [FullCalendarModule],
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
export class AppointmentsComponent {
  currentView: 'dayGridMonth' | 'timeGridWeek' | 'timeGridDay' = 'dayGridMonth';

  calendarOptions: CalendarOptions = {
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    initialView: this.currentView,
    headerToolbar: false,
    slotMinTime: '09:00:00',     // Inicia a las 9 AM
    slotMaxTime: '20:00:00',     // Termina a las 8 PM
    slotLabelFormat: {           // Formato de las etiquetas de hora
      hour: '2-digit',
      minute: '2-digit',
      hour12: false              // Formato 24 horas (cambia a true para AM/PM)
    },
    allDaySlot: false,
    locale: esLocale,
    events: [
      {
        title: 'Consulta con Juan Pérez',
        start: '2025-10-21T10:00:00',
        end: '2025-10-21T11:00:00',
      },
      {
        title: 'Limpieza dental - María',
        start: '2025-10-21T14:00:00',
        end: '2025-10-21T15:00:00',
      },
      {
        title: 'Limpieza dental - María',
        start: '2025-10-21T14:00:00',
        end: '2025-10-21T15:00:00',
      },
      {
        title: 'Limpieza dental - María',
        start: '2025-10-21T14:00:00',
        end: '2025-10-21T15:00:00',
      },
      {
        title: 'Limpieza dental - María',
        start: '2025-10-21T14:00:00',
        end: '2025-10-21T15:00:00',
      },
      // 7
      {
        title: 'Limpieza dental - María',
        start: '2025-10-07T14:00:00',
        end: '2025-10-07T15:00:00',
      },
      {
        title: 'Limpieza dental - María',
        start: '2025-10-07T14:00:00',
        end: '2025-10-07T15:00:00',
      },
      {
        title: 'Limpieza dental - María',
        start: '2025-10-07T14:00:00',
        end: '2025-10-07T15:00:00',
      },
      // 14
      {
        title: 'Limpieza dental - María',
        start: '2025-10-14T14:00:00',
        end: '2025-10-14T15:00:00',
      },
      {
        title: 'Limpieza dental - María',
        start: '2025-10-14T14:00:00',
        end: '2025-10-14T15:00:00',
      },
      {
        title: 'Limpieza dental - María',
        start: '2025-10-14T14:00:00',
        end: '2025-10-14T15:00:00',
      },
      {
        title: 'Limpieza dental - María',
        start: '2025-10-14T14:00:00',
        end: '2025-10-14T15:00:00',
      },
      {
        title: 'Limpieza dental - María',
        start: '2025-10-14T14:00:00',
        end: '2025-10-14T15:00:00',
      },
      {
        title: 'Limpieza dental - María',
        start: '2025-10-14T14:00:00',
        end: '2025-10-14T15:00:00',
      },
      
    ],
  };

  changeView(
    view: 'dayGridMonth' | 'timeGridWeek' | 'timeGridDay',
    calendar: any
  ) {
    this.currentView = view;
    calendar.getApi().changeView(view);
  }
}
