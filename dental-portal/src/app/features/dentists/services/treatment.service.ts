import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TreatmentResponse, TreatmentDetailResponse } from '../interfaces/treatment.interface';

@Injectable({
  providedIn: 'root'
})
export class TreatmentService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/core/dentist';

  // Obtener tratamientos de un paciente específico
  getTreatmentsByPatient(dentistId: number, patientId: number): Observable<TreatmentResponse[]> {
    return this.http.get<TreatmentResponse[]>(`${this.apiUrl}/${dentistId}/patients/${patientId}/treatments`);
  }

  // Obtener detalle completo de un tratamiento
  getTreatmentDetail(dentistId: number, treatmentId: number): Observable<TreatmentDetailResponse> {
    return this.http.get<TreatmentDetailResponse>(`${this.apiUrl}/${dentistId}/treatments/${treatmentId}`);
  }
}
