import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Prescription {
  id: number;
  patientId: number;
  patientName: string;
  patientDni: string;
  dentistId: number;
  dentistName: string;
  dentistLicenseNumber: string;
  dentistSpecialty: string;
  prescriptionDate: string;
  observations: string;
  medications: string;
  active: boolean;
  lastUpdatedDatetime: string;
}

@Injectable({
  providedIn: 'root',
})
export class PrescriptionService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8082/api/core/patient';

  /**
   * Obtiene todas las recetas de un paciente
   */
  getPrescriptionsByPatientId(patientId: number): Observable<Prescription[]> {
    return this.http.get<Prescription[]>(`${this.apiUrl}/${patientId}/prescriptions`);
  }

  /**
   * Obtiene una receta específica por ID
   */
  getPrescriptionById(patientId: number, prescriptionId: number): Observable<Prescription> {
    return this.http.get<Prescription>(`${this.apiUrl}/${patientId}/prescriptions/${prescriptionId}`);
  }

  /**
   * Obtiene el conteo de recetas del paciente
   */
  getPrescriptionCount(patientId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/${patientId}/prescriptions/count`);
  }
}

