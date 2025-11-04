import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DentistResponse, DentistUpdateRequest } from '../interfaces/dentist.interface';
import { DentistPatientsResponse, PatientInfo, PatientRequest } from '../interfaces/patient.interface';
import { AppointmentRequest, AppointmentResponse } from '../interfaces/appointment.interface';

@Injectable({
  providedIn: 'root'
})
export class DentistService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8082/api/core/dentist';

  // Obtener datos del odontólogo por ID
  getDentistById(id: number): Observable<DentistResponse> {
    return this.http.get<DentistResponse>(`${this.apiUrl}/getById/${id}`);
  }

  // Actualizar datos del odontólogo
  updateDentist(id: number, updateData: DentistUpdateRequest): Observable<DentistResponse> {
    return this.http.put<DentistResponse>(`${this.apiUrl}/update/${id}`, updateData);
  }

  // Obtener todos los pacientes del odontólogo
  getPatientsByDentistId(dentistId: number): Observable<DentistPatientsResponse> {
    return this.http.get<DentistPatientsResponse>(`${this.apiUrl}/${dentistId}/patients`);
  }

  // Obtener solo pacientes activos del odontólogo
  getActivePatientsByDentistId(dentistId: number): Observable<DentistPatientsResponse> {
    return this.http.get<DentistPatientsResponse>(`${this.apiUrl}/${dentistId}/patients/active`);
  }

  createAppointment(dentistId: number, appointment: AppointmentRequest): Observable<AppointmentResponse> {
    return this.http.post<AppointmentResponse>(`${this.apiUrl}/${dentistId}/appointments`, appointment);
  }

  createPatient(dentistId: number, patient: PatientRequest): Observable<PatientInfo> {
    return this.http.post<PatientInfo>(`${this.apiUrl}/${dentistId}/patients`, patient);
  }
}
