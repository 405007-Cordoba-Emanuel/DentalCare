import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DentistResponse, DentistUpdateRequest } from '../../features/dentists/interfaces/dentist.interface';
import { DentistPatientsResponse, PatientInfo, PatientRequest, PatientResponse } from '../../features/dentists/interfaces/patient.interface';
import { AppointmentRequest, AppointmentResponse } from '../../features/dentists/interfaces/appointment.interface';
import { PagedResponse } from '../../features/dentists/interfaces/paged-response.interface';

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

  // Obtener ID de dentista por userId
  getDentistIdByUserId(userId: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/user-id/${userId}`);
  }

  createAppointment(dentistId: number, appointment: AppointmentRequest): Observable<AppointmentResponse> {
    return this.http.post<AppointmentResponse>(`${this.apiUrl}/${dentistId}/appointments`, appointment);
  }

  createPatient(dentistId: number, patient: PatientRequest): Observable<PatientInfo> {
    return this.http.post<PatientInfo>(`${this.apiUrl}/${dentistId}/patients`, patient);
  }

  getAvailablePatients(): Observable<PatientResponse[]> {
    return this.http.get<PatientResponse[]>(`${this.apiUrl}/available-patients`);
  }

  getAvailablePatientsPaged(page: number, size: number, sortBy: string, sortDirection: string): Observable<PagedResponse<PatientResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);
    
    return this.http.get<PagedResponse<PatientResponse>>(`${this.apiUrl}/available-patients/paged`, { params });
  }
}
