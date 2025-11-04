import { Component } from '@angular/core';
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
import { IconComponent } from '../../../shared/icon/icon.component';

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
    IconComponent
  ],
  templateUrl: './patient-dashboard.component.html',
  styleUrl: './patient-dashboard.component.css'
})
export class PatientDashboardComponent {
  constructor(private router: Router) {}

  // Datos mock para el dashboard del paciente
  patientInfo = {
    name: 'María González',
    age: 32,
    lastVisit: '2024-01-15',
    nextAppointment: '2024-02-01',
    treatmentProgress: 75
  };

  upcomingAppointments = [
    {
      id: 1,
      date: '2024-02-01',
      time: '10:00 AM',
      type: 'Limpieza',
      dentist: 'Dr. Carlos Rodríguez',
      status: 'Confirmada'
    },
    {
      id: 2,
      date: '2024-02-15',
      time: '02:30 PM',
      type: 'Consulta de Seguimiento',
      dentist: 'Dr. Carlos Rodríguez',
      status: 'Pendiente'
    }
  ];

  treatmentHistory = [
    {
      date: '2024-01-15',
      procedure: 'Limpieza Dental',
      dentist: 'Dr. Carlos Rodríguez',
      cost: 80,
      status: 'Completado'
    },
    {
      date: '2024-01-10',
      procedure: 'Empaste',
      dentist: 'Dr. Carlos Rodríguez',
      cost: 120,
      status: 'Completado'
    },
    {
      date: '2024-01-05',
      procedure: 'Consulta Inicial',
      dentist: 'Dr. Carlos Rodríguez',
      cost: 50,
      status: 'Completado'
    },
    {
      date: '2024-01-20',
      procedure: 'Radiografía',
      dentist: 'Dr. Carlos Rodríguez',
      cost: 60,
      status: 'Pendiente'
    },
    {
      date: '2024-01-25',
      procedure: 'Extracción Molar',
      dentist: 'Dr. Carlos Rodríguez',
      cost: 200,
      status: 'Programado'
    }
  ];

  recentMessages = [
    {
      from: 'Dr. Carlos Rodríguez',
      message: 'Hola María, ¿cómo te sientes después de la limpieza?',
      time: 'Hace 2 horas',
      unread: true
    },
    {
      from: 'Sistema',
      message: 'Recordatorio: Tu próxima cita es el 1 de febrero a las 10:00 AM',
      time: 'Hace 1 día',
      unread: false
    }
  ];

  scheduleAppointment() {
    // Lógica para agendar cita
    console.log('Agendar cita');
  }

  viewTreatmentDetails(treatmentId: number) {
    // Lógica para ver detalles del tratamiento
    console.log('Ver tratamiento:', treatmentId);
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
}
