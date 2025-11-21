import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TreatmentResponse, TreatmentDetailResponse } from '../interfaces/treatment.interface';

@Injectable({
  providedIn: 'root'
})
export class TreatmentService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8082/api/core/dentist';

  // Obtener tratamientos de un paciente específico
  getTreatmentsByPatient(dentistId: number, patientId: number): Observable<TreatmentResponse[]> {
    return this.http.get<TreatmentResponse[]>(`${this.apiUrl}/${dentistId}/patients/${patientId}/treatments`);
  }

  // Obtener detalle completo de un tratamiento
  getTreatmentDetail(dentistId: number, treatmentId: number): Observable<TreatmentDetailResponse> {
    return this.http.get<TreatmentDetailResponse>(`${this.apiUrl}/${dentistId}/treatments/${treatmentId}`);
  }

  // Crear un nuevo tratamiento
  createTreatment(dentistId: number, treatment: any): Observable<TreatmentResponse> {
    return this.http.post<TreatmentResponse>(`${this.apiUrl}/${dentistId}/patients/${treatment.patientId}/treatments`, treatment);
  }

  // Actualizar un tratamiento existente
  updateTreatment(dentistId: number, treatmentId: number, treatment: any): Observable<TreatmentResponse> {
    return this.http.put<TreatmentResponse>(`${this.apiUrl}/${dentistId}/treatments/${treatmentId}`, treatment);
  }

  // Eliminar un tratamiento
  deleteTreatment(dentistId: number, treatmentId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${dentistId}/treatments/${treatmentId}`);
  }
}
