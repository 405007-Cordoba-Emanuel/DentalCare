import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export enum AppointmentStatus {
  PROGRAMADO = 'PROGRAMADO',
  CONFIRMADO = 'CONFIRMADO',
  COMPLETADO = 'COMPLETADO',
  CANCELADO = 'CANCELADO',
  AUSENTE = 'AUSENTE'
}

export interface Appointment {
  id: number;
  patientId: number;
  patientName: string;
  patientDni: string;
  dentistId: number;
  dentistName: string;
  dentistLicenseNumber: string;
  dentistSpecialty: string;
  startDateTime: string;
  endDateTime: string;
  durationMinutes: number;
  status: AppointmentStatus;
  reason: string;
  notes: string;
  active: boolean;
  createdDatetime: string;
  lastUpdatedDatetime: string;
}

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8082/api/core/patient';

  getAppointmentsByPatientId(patientId: number): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.apiUrl}/${patientId}/appointments`);
  }

  getUpcomingAppointmentsByPatientId(patientId: number): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.apiUrl}/${patientId}/appointments/upcoming`);
  }

  getPastAppointmentsByPatientId(patientId: number): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.apiUrl}/${patientId}/appointments/past`);
  }

  getAppointmentById(patientId: number, appointmentId: number): Observable<Appointment> {
    return this.http.get<Appointment>(`${this.apiUrl}/${patientId}/appointments/${appointmentId}`);
  }
}

