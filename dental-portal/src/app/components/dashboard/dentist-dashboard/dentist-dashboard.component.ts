import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatBadgeModule } from '@angular/material/badge';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-dentist-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatTableModule,
    MatBadgeModule,
    MatChipsModule,
    MatDividerModule
  ],
  templateUrl: './dentist-dashboard.component.html',
  styleUrl: './dentist-dashboard.component.css'
})
export class DentistDashboardComponent {
  constructor(private router: Router) {}

  // Datos mock para el dashboard
  upcomingAppointments = [
    {
      id: 1,
      patientName: 'María González',
      time: '09:00 AM',
      type: 'Limpieza',
      status: 'Confirmada'
    },
    {
      id: 2,
      patientName: 'Carlos Rodríguez',
      time: '10:30 AM',
      type: 'Consulta',
      status: 'Pendiente'
    },
    {
      id: 3,
      patientName: 'Ana Martínez',
      time: '02:00 PM',
      type: 'Tratamiento',
      status: 'Confirmada'
    }
  ];

  recentPatients = [
    { name: 'María González', lastVisit: '2024-01-15', nextAppointment: '2024-02-01' },
    { name: 'Carlos Rodríguez', lastVisit: '2024-01-10', nextAppointment: '2024-01-25' },
    { name: 'Ana Martínez', lastVisit: '2024-01-12', nextAppointment: '2024-01-30' }
  ];

  stats = {
    totalPatients: 156,
    appointmentsToday: 8,
    pendingReports: 3,
    monthlyRevenue: 12500
  };

  createNewAppointment() {
    // Lógica para crear nueva cita
    console.log('Crear nueva cita');
  }

  viewPatientDetails(patientId: number) {
    // Lógica para ver detalles del paciente
    console.log('Ver paciente:', patientId);
  }

  logout() {
    this.router.navigate(['/']);
  }
}
