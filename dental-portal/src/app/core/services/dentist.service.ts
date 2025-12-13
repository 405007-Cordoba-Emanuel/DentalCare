import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DentistResponse, DentistUpdateRequest } from '../../features/dentists/interfaces/dentist.interface';
import { DentistPatientsResponse, PatientInfo, PatientRequest, PatientResponse } from '../../features/dentists/interfaces/patient.interface';
import { AppointmentRequest, AppointmentResponse, AppointmentUpdateRequest } from '../../features/dentists/interfaces/appointment.interface';
import { PagedResponse } from '../../features/dentists/interfaces/paged-response.interface';
import { ApiConfig } from '../config/api.config';

@Injectable({
  providedIn: 'root'
})
export class DentistService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfig);
  private apiUrl = this.apiConfig.coreDentistUrl;

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

  // Obtener todas las appointments de un dentista
  getAppointmentsByDentistId(dentistId: number): Observable<AppointmentResponse[]> {
    return this.http.get<AppointmentResponse[]>(`${this.apiUrl}/${dentistId}/appointments`);
  }

  // Obtener appointments activas (excluyendo canceladas) de un dentista
  getActiveAppointmentsByDentistId(dentistId: number): Observable<AppointmentResponse[]> {
    return this.http.get<AppointmentResponse[]>(`${this.apiUrl}/${dentistId}/appointments/active`);
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

  // Obtener citas por mes
  getMonthlyAppointments(dentistId: number, year: number, month: number): Observable<AppointmentResponse[]> {
    const params = new HttpParams()
      .set('year', year.toString())
      .set('month', month.toString());
    
    return this.http.get<AppointmentResponse[]>(`${this.apiUrl}/${dentistId}/appointments/month`, { params });
  }

  // Obtener citas por semana
  getWeeklyAppointments(dentistId: number, startDate: string): Observable<AppointmentResponse[]> {
    const params = new HttpParams()
      .set('startDate', startDate);
    
    return this.http.get<AppointmentResponse[]>(`${this.apiUrl}/${dentistId}/appointments/week`, { params });
  }

  // Obtener citas por día
  getDailyAppointments(dentistId: number, date: string): Observable<AppointmentResponse[]> {
    const params = new HttpParams()
      .set('date', date);
    
    return this.http.get<AppointmentResponse[]>(`${this.apiUrl}/${dentistId}/appointments/day`, { params });
  }

  // Verificar conflicto de horario
  checkTimeConflict(dentistId: number, startTime: string, endTime: string): Observable<boolean> {
    const params = new HttpParams()
      .set('startTime', startTime)
      .set('endTime', endTime);
    
    return this.http.get<boolean>(`${this.apiUrl}/${dentistId}/appointments/conflict-check`, { params });
  }

  // Obtener citas de 2 años (1 año atrás + 1 año adelante)
  getTwoYearAppointments(dentistId: number): Observable<AppointmentResponse[]> {
    return this.http.get<AppointmentResponse[]>(`${this.apiUrl}/${dentistId}/appointments/two-year-range`);
  }

  // Obtener una cita específica por ID
  getAppointmentById(dentistId: number, appointmentId: number): Observable<AppointmentResponse> {
    return this.http.get<AppointmentResponse>(`${this.apiUrl}/${dentistId}/appointments/${appointmentId}`);
  }

  // Actualizar una cita
  updateAppointment(dentistId: number, appointmentId: number, appointment: AppointmentUpdateRequest): Observable<AppointmentResponse> {
    return this.http.put<AppointmentResponse>(`${this.apiUrl}/${dentistId}/appointments/${appointmentId}`, appointment);
  }

  // Actualizar el estado de una cita
  updateAppointmentStatus(dentistId: number, appointmentId: number, status: string): Observable<AppointmentResponse> {
    const params = new HttpParams().set('status', status);
    return this.http.put<AppointmentResponse>(
      `${this.apiUrl}/${dentistId}/appointments/${appointmentId}/status`,
      null,
      { params }
    );
  }

  // Cancelar una cita
  cancelAppointment(dentistId: number, appointmentId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${dentistId}/appointments/${appointmentId}`);
  }
}
