import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TreatmentResponse } from '../../features/dentists/interfaces/treatment.interface';
import { ApiConfig } from '../config/api.config';

@Injectable({
  providedIn: 'root'
})
export class TreatmentService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfig);
  private apiUrl = this.apiConfig.corePatientUrl;

  getTreatmentsByPatientId(patientId: number): Observable<TreatmentResponse[]> {
    return this.http.get<TreatmentResponse[]>(`${this.apiUrl}/${patientId}/treatments`);
  }

  getTreatmentById(patientId: number, treatmentId: number): Observable<TreatmentResponse> {
    return this.http.get<TreatmentResponse>(`${this.apiUrl}/${patientId}/treatments/${treatmentId}`);
  }
}
