import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AppointmentResponse } from '../../features/dentists/interfaces/appointment.interface';

export interface Patient {
  id: number;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  address: string;
  dni: string;
  active: boolean;
}

export interface PatientUpdateRequest {
  dni: string;
  active?: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class PatientService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8082/api/core/patient';

  getPatientById(patientId: number): Observable<Patient> {
    return this.http.get<Patient>(`${this.apiUrl}/getById/${patientId}`);
  }

  updatePatient(patientId: number, updateData: PatientUpdateRequest): Observable<Patient> {
    return this.http.put<Patient>(`${this.apiUrl}/update/${patientId}`, updateData);
  }

  getPatientIdByUserId(userId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/user-id/${userId}`);
  }

  assignDentistToPatient(patientId: number, dentistId: number): Observable<Patient> {
    return this.http.put<Patient>(`${this.apiUrl}/${patientId}/assign-dentist/${dentistId}`, {});
  }

  // Obtener todas las appointments de un paciente
  getAppointmentsByPatientId(patientId: number): Observable<AppointmentResponse[]> {
    return this.http.get<AppointmentResponse[]>(`${this.apiUrl}/${patientId}/appointments`);
  }

  // Obtener appointments activas (excluyendo canceladas) de un paciente
  getActiveAppointmentsByPatientId(patientId: number): Observable<AppointmentResponse[]> {
    return this.http.get<AppointmentResponse[]>(`${this.apiUrl}/${patientId}/appointments/active`);
  }

  // Obtener próximas appointments del paciente
  getUpcomingAppointmentsByPatientId(patientId: number): Observable<AppointmentResponse[]> {
    return this.http.get<AppointmentResponse[]>(`${this.apiUrl}/${patientId}/appointments/upcoming`);
  }

  // Obtener appointments pasadas del paciente
  getPastAppointmentsByPatientId(patientId: number): Observable<AppointmentResponse[]> {
    return this.http.get<AppointmentResponse[]>(`${this.apiUrl}/${patientId}/appointments/past`);
  }
}

