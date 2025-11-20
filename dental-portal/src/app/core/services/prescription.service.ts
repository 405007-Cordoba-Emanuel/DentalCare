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

  // ========== MÉTODOS PARA DENTISTA ==========

  private dentistApiUrl = 'http://localhost:8082/api/core/dentist';

  /**
   * Obtiene todas las recetas de un paciente específico para un dentista
   */
  getPrescriptionsByDentistIdAndPatientId(dentistId: number, patientId: number): Observable<Prescription[]> {
    return this.http.get<Prescription[]>(`${this.dentistApiUrl}/${dentistId}/prescriptions/patient/${patientId}`);
  }

  /**
   * Obtiene una receta específica por ID para un dentista
   */
  getPrescriptionByIdAndDentistId(dentistId: number, prescriptionId: number): Observable<Prescription> {
    return this.http.get<Prescription>(`${this.dentistApiUrl}/${dentistId}/prescriptions/${prescriptionId}`);
  }

  /**
   * Crea una nueva receta
   */
  createPrescription(dentistId: number, prescription: any): Observable<Prescription> {
    return this.http.post<Prescription>(`${this.dentistApiUrl}/${dentistId}/prescriptions`, prescription);
  }

  /**
   * Actualiza una receta existente
   */
  updatePrescription(dentistId: number, prescriptionId: number, prescription: any): Observable<Prescription> {
    return this.http.put<Prescription>(`${this.dentistApiUrl}/${dentistId}/prescriptions/${prescriptionId}`, prescription);
  }

  /**
   * Elimina una receta (soft delete)
   */
  deletePrescription(dentistId: number, prescriptionId: number): Observable<void> {
    return this.http.delete<void>(`${this.dentistApiUrl}/${dentistId}/prescriptions/${prescriptionId}`);
  }


  /**
   * Descarga una receta en formato PDF
   */
  downloadPrescriptionPdf(dentistId: number, prescriptionId: number): Observable<Blob> {
    return this.http.get(`${this.dentistApiUrl}/${dentistId}/prescriptions/${prescriptionId}/download-pdf`, {
      responseType: 'blob'
    });
  }
}

