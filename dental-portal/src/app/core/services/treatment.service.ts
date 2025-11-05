import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TreatmentResponse } from '../../features/dentists/interfaces/treatment.interface';

@Injectable({
  providedIn: 'root'
})
export class TreatmentService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8082/api/core/patient';

  getTreatmentsByPatientId(patientId: number): Observable<TreatmentResponse[]> {
    return this.http.get<TreatmentResponse[]>(`${this.apiUrl}/${patientId}/treatments`);
  }

  getTreatmentById(patientId: number, treatmentId: number): Observable<TreatmentResponse> {
    return this.http.get<TreatmentResponse>(`${this.apiUrl}/${patientId}/treatments/${treatmentId}`);
  }
}
